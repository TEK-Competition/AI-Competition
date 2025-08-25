package com.laijiaxiang.supreme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class SupremeCommonConfig {
    @Bean
    public ExecutorService getExecutorService() {
        int nThreads = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(nThreads, 2 * nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(nThreads));
    }
}
