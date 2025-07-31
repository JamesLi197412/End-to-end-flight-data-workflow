# End-to-End Flight Data Engineering Workflow

## Overview

This project demonstrates a complete data engineering workflow for processing real-time flight data. It involves calling APIs from two different sources in parallel, processing the data through Apache Kafka and Apache Spark, and finally exporting the processed data to Amazon S3 for storage and further analysis.

## Features

- **Parallel API Calls**: Efficiently fetches data from multiple APIs concurrently.
- **Kafka for Messaging**: Uses Apache Kafka for reliable and scalable message queuing.
- **Spark for Big Data Processing**: Leverages Apache Spark for high-performance data processing.
- **S3 for Storage**: Stores processed data in Amazon S3 for durability and scalability.
- **Dynamic Thread Pool**: Adjusts the number of threads based on workload for optimal resource utilization.
- **API Rate Limiting and Caching**: Manages API rate limits and caches responses to reduce the number of calls.
- **Monitoring and Logging**: Integrates monitoring and logging with tools like Prometheus and Grafana.

## Prerequisites

- Java 17
- Maven
- Apache Kafka
- Apache Spark
- Amazon S3 (or Minio for local testing)
- PostgreSQL (optional, for additional data storage)

## Getting Started

### 1. Clone the Repository


### 2. Set Up Environment Variables

Create a `.zshrc` or `.env` file in your home directory with the following variables:

* export AMADEUS_API_KEY=your_amadeus_api_key
* export AVIATION_API_KEY=your_aviation_api_key
* export AWS_ACCESS_KEY_ID=your_aws_access_key_id
* export AWS_SECRET_ACCESS_KEY=your_aws_secret_access_key


## Configuration
- : Configures and manages Kafka. **KafkaManager.java**
- : Handles the Amadeus API calls and data processing. **AmadeusProcess.java**
- : Handles the Aviation API calls and data processing. **AviationProcess.java**
- : Contains constant values used throughout the project. **Constants.java**
