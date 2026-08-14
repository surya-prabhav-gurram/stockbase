package com.stockbase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables @Scheduled tasks (e.g. the low-stock notification sweep). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
