package com.jamesli;


import com.jamesli.utils.GetPrefixEnvrionmentVariable;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlightPipeline {
    public static void main(String[] args) {
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


    }
}