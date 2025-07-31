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
    }

    /*
       Initialise the Kafka Service
    */
    public void start() throws Exception {
        startZookeeper();
        Thread.sleep(2000);
        startKafka();
        Thread.sleep(2000);
        //createProducer();
    }

    private void createProducer(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
        logger.info("Kafka producer created");

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

    private void producerfunction(String topic, String key, String value) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
        logger.info("Kafka producer created");

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        try {
            Future<RecordMetadata> future = producer.send(record); // Use Future
            RecordMetadata metadata = future.get(5, TimeUnit.SECONDS); // Get the result from the Future
            System.out.println("Message sent to topic: " + metadata.topic() +
                    " partition: " + metadata.partition() +
                    " offset: " + metadata.offset());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void send(String topic, String key, String value){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
        logger.info("Kafka producer created");

        if (producer == null) {
            createProducer();
        }

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

    public void createTopic(String topicName, int partitions, short replicationFactor) {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic topic = new NewTopic(topicName, partitions, replicationFactor);
            adminClient.createTopics(Collections.singletonList(topic));
            System.out.println("Topic '" + topicName + "' created successfully");
        } catch (Exception e) {
            System.err.println("Failed to create topic: " + e.getMessage());
        }
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
