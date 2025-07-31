package com.jamesli;


import com.jamesli.config.Constants;
import com.jamesli.kafka.KafkaManager;
import com.jamesli.process.AmadeusProcess;
import com.jamesli.process.AviationProcess;
import com.jamesli.utils.GetPrefixEnvrionmentVariable;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FlightPipeline {
    public static void main(String[] args) throws Exception {
        System.setProperty("jdk.module.addExports", "java.base/sun.nio.ch=ALL-UNNAMED");
        System.setProperty("jdk.module.main.class", "com.jamesli.aws.s3.SparkApp");

        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(myFormatObj);

        String homeDir = System.getProperty("user.home");
        String prefix = "amadeus";
        GetPrefixEnvrionmentVariable env = new GetPrefixEnvrionmentVariable(prefix);
        JSONObject amadeusProps = env.readProperties(homeDir + "/.zshrc", "amadeus");
        JSONObject aviationProps = env.readProperties(homeDir + "/.zshrc", "aviation");
        JSONObject awsProps = env.readProperties(homeDir + "/.zshrc", "AWS");
        System.out.println(awsProps);

        // initialise the Kafka
        KafkaManager kafkaManager = new KafkaManager(Constants.kafkaPort, Constants.zooKeeperPort);
        kafkaManager.start();

        // Create an ExecutorService with a fixed thread pool
        String[] apiNames = {"aviation", "amadeus"};
        ExecutorService executorService = Executors.newFixedThreadPool(apiNames.length);

        AmadeusProcess amadeus = new AmadeusProcess(amadeusProps, Constants.S3BUCKET, kafkaManager, awsProps);
        AviationProcess avation = new AviationProcess(aviationProps, Constants.S3BUCKET, kafkaManager, awsProps);

        try{
            Thread.sleep(2000);

            // Submit AviationProcess task
            executorService.submit(() -> {
                try {
                    avation.fetchDataAndProcess(formattedDate, kafkaManager.getBootstrapServers());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Submit AmadeusProcess task
            executorService.submit(() -> {
                try {
                    amadeus.amadeusPipeline(kafkaManager.getBootstrapServers());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            // Shutdown the ExecutorService
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            kafkaManager.stop();
        }
    }
}