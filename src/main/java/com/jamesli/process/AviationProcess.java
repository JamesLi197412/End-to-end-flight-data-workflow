package com.jamesli.process;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jamesli.kafka.KafkaManager;
import com.jamesli.utils.Okhttp;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesli.pojo.aviation.Flight;
import com.jamesli.pojo.aviation.FlightApiResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class AviationProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(AviationProcess.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String s3Bucket;
    private final String s3Region;
    private final String s3accessKey;
    private final String s3secretKey;
    private KafkaManager kafka;
    private final String aviationUrl = "https://api.aviationstack.com/v1/flights";
    private String apiKey;


    public AviationProcess(JSONObject aviationProps, String s3Bucket, KafkaManager kafka, JSONObject awsProps) {
        this.apiKey = aviationProps.getString("aviation_apikey");
        this.s3accessKey = awsProps.getString("aws_access_key_id");
        this.s3secretKey = awsProps.getString("aws_secret_access_key");
        this.s3Region = awsProps.getString("aws_region");
        this.s3Bucket = s3Bucket;
        this.kafka = kafka;

    }

    /*
        Implementation to fetch data from the API

     */
    private List<Flight> receiveAPI(String formattedDate){
        Okhttp aviationProcess = new Okhttp();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // Add these configurations for better null handling
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
            mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

            String aviationResponse = aviationProcess.performSyncGet(aviationUrl, this.apiKey, formattedDate, 1);

            FlightApiResponse info = mapper.readValue(aviationResponse, FlightApiResponse.class);
            List<Flight> flights = info.getData();
            return flights;
        }catch (Exception e){
            System.out.println("error");
            e.printStackTrace();
        }
        return null;
    }

    /*
        Implementation to send data to Kafka
     */
    private void aviationProducer(String jsonData) throws InterruptedException, ExecutionException, TimeoutException {
        kafka.send("aviation_topic", null, jsonData);
    }

    private static StructType getFlightDataSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("flight_date", DataTypes.StringType, true),
                DataTypes.createStructField("flight_status", DataTypes.StringType, true),
                DataTypes.createStructField("departure", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("airport", DataTypes.StringType, true),
                        DataTypes.createStructField("timezone", DataTypes.StringType, true),
                        DataTypes.createStructField("iata", DataTypes.StringType, true),
                        DataTypes.createStructField("icao", DataTypes.StringType, true),
                        DataTypes.createStructField("terminal", DataTypes.StringType, true),
                        DataTypes.createStructField("gate", DataTypes.StringType, true),
                        DataTypes.createStructField("delay", DataTypes.IntegerType, true),
                        DataTypes.createStructField("scheduled", DataTypes.TimestampType, true),
                        DataTypes.createStructField("estimated", DataTypes.TimestampType, true),
                        DataTypes.createStructField("actual", DataTypes.TimestampType, true),
                        DataTypes.createStructField("estimated_runway", DataTypes.TimestampType, true),
                        DataTypes.createStructField("actual_runway", DataTypes.TimestampType, true)
                }), true),
                DataTypes.createStructField("arrival", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("airport", DataTypes.StringType, true),
                        DataTypes.createStructField("timezone", DataTypes.StringType, true),
                        DataTypes.createStructField("iata", DataTypes.StringType, true),
                        DataTypes.createStructField("icao", DataTypes.StringType, true),
                        DataTypes.createStructField("terminal", DataTypes.StringType, true),
                        DataTypes.createStructField("gate", DataTypes.StringType, true),
                        DataTypes.createStructField("baggage", DataTypes.StringType, true),
                        DataTypes.createStructField("delay", DataTypes.IntegerType, true),
                        DataTypes.createStructField("scheduled", DataTypes.TimestampType, true),
                        DataTypes.createStructField("estimated", DataTypes.TimestampType, true),
                        DataTypes.createStructField("actual", DataTypes.TimestampType, true),
                        DataTypes.createStructField("estimated_runway", DataTypes.TimestampType, true),
                        DataTypes.createStructField("actual_runway", DataTypes.TimestampType, true)
                }), true),
                DataTypes.createStructField("airline", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("name", DataTypes.StringType, true),
                        DataTypes.createStructField("iata", DataTypes.StringType, true),
                        DataTypes.createStructField("icao", DataTypes.StringType, true)
                }), true),
                DataTypes.createStructField("flight", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("number", DataTypes.StringType, true),
                        DataTypes.createStructField("iata", DataTypes.StringType, true),
                        DataTypes.createStructField("icao", DataTypes.StringType, true),
                        DataTypes.createStructField("codeshared", DataTypes.StringType, true)
                }), true),
                DataTypes.createStructField("aircraft", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("registration", DataTypes.StringType, true),
                        DataTypes.createStructField("iata", DataTypes.StringType, true),
                        DataTypes.createStructField("icao", DataTypes.StringType, true),
                        DataTypes.createStructField("icao24", DataTypes.StringType, true)
                }), true),
                DataTypes.createStructField("live", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("updated", DataTypes.TimestampType, true),
                        DataTypes.createStructField("latitude", DataTypes.DoubleType, true),
                        DataTypes.createStructField("longitude", DataTypes.DoubleType, true),
                        DataTypes.createStructField("altitude", DataTypes.DoubleType, true),
                        DataTypes.createStructField("direction", DataTypes.DoubleType, true),
                        DataTypes.createStructField("speed_horizontal", DataTypes.DoubleType, true),
                        DataTypes.createStructField("speed_vertical", DataTypes.DoubleType, true),
                        DataTypes.createStructField("is_ground", DataTypes.BooleanType, true)
                }), true)
        });
    }

    public void fetchDataAndProcess(String formattedDate, String kafkaServers) throws InterruptedException, ExecutionException, TimeoutException, StreamingQueryException {
        // Fetch Data Via API
        List<Flight> flights = receiveAPI(formattedDate);

        // send data to Kafka
        for (Flight flight:flights){
            try{
                String jsonData = OBJECT_MAPPER.writeValueAsString(flight);
                aviationProducer(jsonData);
            }catch(Exception e){
                System.out.println("Failed");
                //LOGGER.log(Level.SEVERE, "Error while producing to Kafka: " + e.getMessage(), e);
            }
        }
        // Process data with Spark and store them in S3
        processKafkatoS3("aviation_topic", kafkaServers);
    }


    private void processKafkatoS3(String topicName, String kafkaServers) throws InterruptedException,
            ExecutionException, TimeoutException, StreamingQueryException {
        // Create and configure the Spark session
        SparkSession spark = createSparkSession(this.s3accessKey, this.s3secretKey);

        Dataset<Row> df = spark.read()
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServers)
                .option("subscribe", topicName)
                .option("startingOffsets", "earliest")
                .option("endingOffsets", "latest")
                .load()
                .selectExpr("CAST(value AS STRING) AS value") // Cast value to string and alias it
                .as(Encoders.bean(Row.class)); // Use Encoders.bean to handle Row

        Dataset<Row> data = df.select(functions.from_json(functions.col("value"), getFlightDataSchema()).as("data"))
                .select("data.*");

        data.show();

        String outputPath = "s3a://flightintel/aviation/";
        data.write().mode("overwrite").parquet(outputPath);
        spark.stop();
    }

    // Extracted method to create and configure the Spark session
    private SparkSession createSparkSession(String accessKey, String secretKey) {
        return SparkSession.builder()
                .appName("AviationDataProcessing")
                .config("spark.hadoop.fs.s3a.access.key", accessKey)
                .config("spark.hadoop.fs.s3a.secret.key", secretKey)
                .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                .master("local[*]")
                .getOrCreate();
    }

}
