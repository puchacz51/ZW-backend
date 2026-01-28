package pl.pbs.zwbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration class to enable async processing for notifications and audit logs
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
