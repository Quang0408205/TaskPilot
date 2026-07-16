package com.example.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Redis is selected by SPRING_CACHE_TYPE=redis in Docker; local development uses simple in-memory cache. */
@Configuration @EnableCaching
public class CacheConfig { }
