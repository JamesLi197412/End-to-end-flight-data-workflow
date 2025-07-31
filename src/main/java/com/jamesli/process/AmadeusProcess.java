package com.jamesli.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamesli.kafka.KafkaManager;
import com.jamesli.pojo.amadeus.FlightOffers;
import com.jamesli.pojo.amadeus.flightoffers.FlightOffer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class AmadeusProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmadeusProcess.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String s3Bucket;
    private final String s3Region;
    private KafkaManager kafka;
    private final String s3accessKey;
    private final String s3secretKey;
    private final String apiKey;
    private final String apiSecret;
    private final String baseUrl = "https://api.amadeus.com/v2";
    private final String tokenUrl = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private static final CloseableHttpClient httpClient;
    private static final String topic = "flight-offers";
    private final String topicName = "amadeus_topic";

    static{
        // Configure timeouts
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(5000))                // Connection timeout
                .setResponseTimeout(Timeout.ofMilliseconds(30000))              // Response timeout
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))      // Connection manager timeout
                .build();

        // Configure connection pool
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);                    // Maximum total connections
        connectionManager.setDefaultMaxPerRoute(20);           // Maximum connections per route

        httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

        // Register a shutdown hook to ensure the client is closed when the JVM exists
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            try{
                httpClient.close();
                System.out.println("Apache HttpClient closed gracefully.");
            }catch (IOException e){
                System.out.println("Error closing Httpclient" + e.getMessage());
            }
        }));

    }

    public AmadeusProcess(JSONObject amadeusProps, String s3Bucket, KafkaManager kafka, JSONObject awsProps) {
        this.apiKey = amadeusProps.getString("amadeus_api_key");
        this.apiSecret = amadeusProps.getString("amadeus_api_secret");
        this.s3Bucket = s3Bucket;
        this.kafka = kafka;
        this.s3Region = awsProps.getString("aws_region");
        this.s3accessKey = awsProps.getString("aws_access_key_id");
        this.s3secretKey = awsProps.getString("aws_secret_access_key");
    }

    private String getAccessToken(String clientId, String clientSecret) throws IOException, Exception {
        String accessToken = null;

        org.apache.hc.client5.http.classic.methods.HttpPost httpPost = new HttpPost(tokenUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        // Prepare form parameters
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "client_credentials"));
        formParams.add(new BasicNameValuePair("client_id", clientId));
        formParams.add(new BasicNameValuePair("client_secret", clientSecret));

        // Create URL encoded form entity
        UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
        httpPost.setEntity(requestEntity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (response.getCode() == 200) {
                accessToken = rootNode.get("access_token").asText();
                int expiresIn = rootNode.get("expires_in").asInt();
                String tokenType = rootNode.has("token_type") ? rootNode.get("token_type").asText() : "Bearer";
            }

        } catch (IOException e) {
            System.err.println("IOException occurred while getting access token: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return accessToken;
    }
    /*
        Flight Offers Search
     */
    private String callAPI(String accessToken) throws IOException, ParseException, URISyntaxException, Exception{
        String url = "https://test.api.amadeus.com/v2/shopping/flight-offers";
        String data = null;
        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.addParameter("originLocationCode","SYD");
        uriBuilder.addParameter("destinationLocationCode","BKK");
        uriBuilder.addParameter("departureDate","2025-07-16");
        uriBuilder.addParameter("adults","1");

        HttpGet request = new HttpGet(uriBuilder.build());

        request.setHeader("Authorization", "Bearer " + accessToken);

        try(CloseableHttpResponse response = httpClient.execute(request)){
            data = responseEvaulation(response);
            return data;
        }catch(IOException e) {
            e.printStackTrace();
        }
        return data;

    }

    private void amadeusProducer(String jsonData) throws InterruptedException, ExecutionException, TimeoutException {
        kafka.send(topicName, null, jsonData);
    }

    private String responseEvaulation(CloseableHttpResponse response) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;
        // process the data
        if (response.getCode() == 200){
            String responseBody = EntityUtils.toString(response.getEntity());
            FlightOffers info = mapper.readValue(responseBody, FlightOffers.class);
            List<FlightOffer> flights = info.getData();

            for (FlightOffer flight:flights){
                try{
                    jsonData = OBJECT_MAPPER.writeValueAsString(flight);
                    return jsonData;
                    //amadeusProducer(jsonData);
                }catch(Exception e){
                    System.out.println("Failed");
                    //LOGGER.log(Level.SEVERE, "Error while producing to Kafka: " + e.getMessage(), e);
                }
            }
        }else{
            System.out.println("System respond is not good");
        }
        return jsonData;
    }

    private static StructType getFlightOfferSchema() {
        // Define the nested schemas first
        StructType departureSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("iataCode", DataTypes.StringType, true),
                DataTypes.createStructField("terminal", DataTypes.StringType, true),
                DataTypes.createStructField("at", DataTypes.TimestampType, true)
        });

        StructType arrivalSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("iataCode", DataTypes.StringType, true),
                DataTypes.createStructField("terminal", DataTypes.StringType, true),
                DataTypes.createStructField("at", DataTypes.TimestampType, true)
        });

        StructType aircraftSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("code", DataTypes.StringType, true)
        });

        StructType operatingSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("carrierCode", DataTypes.StringType, true)
        });

        StructType segmentSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("departure", departureSchema, true),
                DataTypes.createStructField("arrival", arrivalSchema, true),
                DataTypes.createStructField("carrierCode", DataTypes.StringType, true),
                DataTypes.createStructField("number", DataTypes.StringType, true),
                DataTypes.createStructField("aircraft", aircraftSchema, true),
                DataTypes.createStructField("operating", operatingSchema, true),
                DataTypes.createStructField("duration", DataTypes.StringType, true),
                DataTypes.createStructField("id", DataTypes.StringType, true),
                DataTypes.createStructField("numberOfStops", DataTypes.IntegerType, true),
                DataTypes.createStructField("blacklistedInEU", DataTypes.BooleanType, true)
        });

        StructType itinerarySchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("duration", DataTypes.StringType, true),
                DataTypes.createStructField("segments", DataTypes.createArrayType(segmentSchema), true)
        });

        StructType feeSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("amount", DataTypes.StringType, true),
                DataTypes.createStructField("type", DataTypes.StringType, true)
        });

        StructType priceSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("currency", DataTypes.StringType, true),
                DataTypes.createStructField("total", DataTypes.StringType, true),
                DataTypes.createStructField("base", DataTypes.StringType, true),
                DataTypes.createStructField("fees", DataTypes.createArrayType(feeSchema), true),
                DataTypes.createStructField("grandTotal", DataTypes.StringType, true)
        });

        StructType pricingOptionsSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("fareType", DataTypes.createArrayType(DataTypes.StringType), true),
                DataTypes.createStructField("includedCheckedBagsOnly", DataTypes.BooleanType, true)
        });

        StructType includedCheckedBagsSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("weight", DataTypes.IntegerType, true),
                DataTypes.createStructField("weightUnit", DataTypes.StringType, true)
        });

        StructType fareDetailsBySegmentSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("segmentId", DataTypes.StringType, true),
                DataTypes.createStructField("cabin", DataTypes.StringType, true),
                DataTypes.createStructField("fareBasis", DataTypes.StringType, true),
                DataTypes.createStructField("class", DataTypes.StringType, true),
                DataTypes.createStructField("includedCheckedBags", includedCheckedBagsSchema, true)
        });

        StructType travelerPricingSchema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("travelerId", DataTypes.StringType, true),
                DataTypes.createStructField("fareOption", DataTypes.StringType, true),
                DataTypes.createStructField("travelerType", DataTypes.StringType, true),
                DataTypes.createStructField("price", priceSchema, true),
                DataTypes.createStructField("fareDetailsBySegment", DataTypes.createArrayType(fareDetailsBySegmentSchema), true)
        });

        // Define the main schema
        return DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("type", DataTypes.StringType, true),
                DataTypes.createStructField("id", DataTypes.StringType, true),
                DataTypes.createStructField("source", DataTypes.StringType, true),
                DataTypes.createStructField("instantTicketingRequired", DataTypes.BooleanType, true),
                DataTypes.createStructField("nonHomogeneous", DataTypes.BooleanType, true),
                DataTypes.createStructField("oneWay", DataTypes.BooleanType, true),
                DataTypes.createStructField("lastTicketingDate", DataTypes.DateType, true),
                DataTypes.createStructField("numberOfBookableSeats", DataTypes.IntegerType, true),
                DataTypes.createStructField("itineraries", DataTypes.createArrayType(itinerarySchema), true),
                DataTypes.createStructField("price", priceSchema, true),
                DataTypes.createStructField("pricingOptions", pricingOptionsSchema, true),
                DataTypes.createStructField("validatingAirlineCodes", DataTypes.createArrayType(DataTypes.StringType), true),
                DataTypes.createStructField("travelerPricings", DataTypes.createArrayType(travelerPricingSchema), true)
        });
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

    private void amadeusSpark(String topicName, String kafkaServers) throws InterruptedException{
        SparkSession spark = createSparkSession(this.s3accessKey, this.s3secretKey);

        Dataset<Row> df = spark.read()
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServers)
                .option("subscribe", topicName)
                .option("startingOffsets","earliest")
                .option("endingOffsets", "latest")
                .load()
                .selectExpr("CAST(value AS STRING) AS value") // Cast value to string and alias it
                .as(Encoders.bean(Row.class)); // Use Encoders.bean to handle Row

        Dataset<Row> data = df.select(functions.from_json(functions.col("value"), getFlightOfferSchema()).as("data"))
                .select("data.*");

        data.show();

        String outputPath = "s3a://flightintel/amadeus/";
        data.write().mode("overwrite").parquet(outputPath);
        spark.stop();
    }

    public void amadeusPipeline(String kafkaServers) throws IOException, Exception{
        String token = getAccessToken(this.apiKey, this.apiSecret);
        String data = callAPI(token);
        amadeusProducer(data);
        amadeusSpark(topicName, kafkaServers);
    }
}
