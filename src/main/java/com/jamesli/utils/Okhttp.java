package com.jamesli.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Okhttp {
    private static final Logger LOGGER = Logger.getLogger(Okhttp.class.getName());

    private final OkHttpClient client;
    private final ThreadPoolExecutor customThreadPool;

    public Okhttp() {
        this.customThreadPool = createCustomThreadPool();

        // Create custom dispatcher with our thread pool
        Dispatcher dispatcher = new Dispatcher(customThreadPool);
        dispatcher.setMaxRequests(64); // Max concurrent requests
        dispatcher.setMaxRequestsPerHost(8); // Max concurrent requests per host

        // Build OkHttpClient with custom configuration
        this.client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true)
                .cache(null) // Disable cache for this example
                .build();

        LOGGER.log(Level.INFO, "Okhttp initialized with custom configurations.");
    }

    /**
     * Creates a custom thread pool for HTTP requests
     */
    private ThreadPoolExecutor createCustomThreadPool() {
        int corePoolSize = 5;      // Minimum threads
        int maxPoolSize = 20;      // Maximum threads
        long keepAliveTime = 60L;  // Thread idle time before termination

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "OkHttp-Custom-" + threadNumber.getAndIncrement());
                thread.setDaemon(false); // Non-daemon threads
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), // Unbounded queue
                threadFactory
        );
    }

    // Performs a synchronous GET request
    public String performSyncGet(String url, String apiKey, String flightDate, int offset) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("access_key", apiKey);
        // urlBuilder.addQueryParameter("limit", "100");
        urlBuilder.addQueryParameter("flight_status", "scheduled");
        //urlBuilder.addQueryParameter("flight_date", "2025-07-10");
        urlBuilder.addQueryParameter("offset", Integer.toString(offset));
        url = urlBuilder.build().toString();
        System.out.println(url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "CustomOkHttpClient/1.0")
                .addHeader("Accept", "application/json")
                .build();

        try(Response response = client.newCall(request).execute()){
            if (!response.isSuccessful()){
                throw new IOException("Unexpected response code:" +response.code());
            }

            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    /**
     * Shutdown the client and thread pool
     */
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        customThreadPool.shutdown();
        try {
            if (!customThreadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                customThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            customThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("OkHttp client shutdown completed");
    }
}
