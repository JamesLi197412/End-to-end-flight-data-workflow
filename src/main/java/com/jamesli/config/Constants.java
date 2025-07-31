package com.jamesli.config;

public final class Constants {
    public final class Constants {
        private Constants() {}
        // -- kafka related constants
        public static final String kafkaPort = "9092";
        public static final String zooKeeperPort = "2181";

        // -- AWS S3 Related Constants
        public static final String S3_BUCKET_RAW_DATA = "flight-data-bucket";
        public static final String S3_BUCKET_PROCESSED_DATA = "flight-processed-data-buck";
        public static final String S3_PATH_PREFIX_DATE_FORMAT = "yyyy/MM/dd/";
        public static final String S3BUCKET = "flightintel";

    }
}
