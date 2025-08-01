package com.jamesli.kafka;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.errors.InterruptException;


import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KafkaManager {
    private static final Logger logger = LoggerFactory.getLogger(KafkaManager.class);
    private KafkaServer kafkaServer;
    private Process zookeeperProcess;
    private Process kafkaProcess;
    private KafkaProducer<String, String> producer;
    private ZooKeeperServerMain zooKeeperServer;
    private Thread zooKeeperThread;
    private final Properties producerProps;

    private final String kafkaPort;
    private final String zooKeeperPort;

    private File logDir;
    private File zooKeeperDir;

    private String kafkaHome;

    public KafkaManager(String kafkaPort, String zooKeeperPort) {
        this.kafkaPort = kafkaPort;
        this.zooKeeperPort = zooKeeperPort;
        this.logDir = new File("logs/kafka");
        this.zooKeeperDir = new File("logs/zookeeper");
        this.kafkaHome = "/Library/Java/JavaVirtualMachines/kafka_2.12-3.9.0";
        this.producerProps = createProducerProperties();
    }

    /*
       Initialise the Kafka Service
    */
    public void start() throws Exception {
        startZookeeper();
        Thread.sleep(2000);
        startKafka();
        Thread.sleep(2000);
        this.producer = createProducer();
    }

    /*
        Initialise the Zookeeper
     */
    private void startZookeeper() throws Exception {
        String kafkaHome = "/Library/Java/JavaVirtualMachines/kafka_2.12-3.9.0";
        String zookeeperScript = kafkaHome + "/bin/zookeeper-server-start.sh";
        String zookeeperConfig = kafkaHome + "/config/zookeeper.properties";

        ProcessBuilder processBuilder = new ProcessBuilder(zookeeperScript, zookeeperConfig);
        processBuilder.directory(new File(kafkaHome));

        try {
            zookeeperProcess = processBuilder.start();
            logger.info("Zookeeper started successfully.");
        } catch (IOException e) {
            logger.error("Failed to start Zookeeper: " + e.getMessage(), e);
            throw new RuntimeException("Failed to start Zookeeper", e);
        }

    }

    /*
    Start the Kafka with properties
    */
    private void startKafka() throws Exception {
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        String kafkaHome = "/Library/Java/JavaVirtualMachines/kafka_2.12-3.9.0";
        String kafkaScript = kafkaHome + "/bin/kafka-server-start.sh";
        String kafkaConfig = kafkaHome + "/config/server.properties";

        ProcessBuilder processBuilder = new ProcessBuilder(kafkaScript, kafkaConfig);
        processBuilder.directory(new File(kafkaHome));

        try {
            zookeeperProcess = processBuilder.start();
            logger.info("Kafka started successfully.");
        } catch (IOException e) {
            logger.error("Failed to start Kafka: " + e.getMessage(), e);
            throw new RuntimeException("Failed to start Kafka", e);
        }
    }

    private KafkaProducer<String, String> createProducer() {
        return new KafkaProducer<>(producerProps);
    }

    private Properties createProducerProperties(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put("metadata.max.age.ms", "300000"); // 5 minutes
        //props.put("compression.type", "gzip");
        logger.info("Kafka producer created");
        return props;
    }

    public void send(String topic, String key, String value){
        if (producer == null) {
            createProducer();
        }
        try {
            // Create a new record
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            // Send the record
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Message sent to topic: " + metadata.topic() + " partition: " + metadata.partition() + " offset: " + metadata.offset());
                } else {
                    logger.error("Error while producing message to topic: " + topic, exception);
                }
            });

            // Flush the producer to ensure all messages are sent
            producer.flush();
        }catch (InterruptException e){
            System.err.println("Interrupted while sending message to Kafka: " + e.getMessage());
        }
    }

    public boolean isKafkaAvailable(){
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + this.kafkaPort);
        AdminClient adminClient = AdminClient.create(props);

        try {
            adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            adminClient.close();
        }
        return false;

    }

    public void stop(){
        if (kafkaServer != null) {
            kafkaServer.shutdown();
            kafkaServer.awaitShutdown();
            logger.info("Kafka server stopped");
        }

        if (zooKeeperThread != null) {
            zooKeeperThread.interrupt();
            logger.info("ZooKeeper stopped");
        }

        if (logDir != null && logDir.exists()) {
            deleteDirectory(logDir);
        }

        if (zooKeeperDir != null && zooKeeperDir.exists()) {
            deleteDirectory(zooKeeperDir);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public String getBootstrapServers() {
        return "localhost:" + this.kafkaPort;
    }

    public void restart() {
        stop();
        try{
            start();
        }catch(Exception e){
            logger.error("Failed to restart Kafka and Zookeeper", e);
        }
    }
}
