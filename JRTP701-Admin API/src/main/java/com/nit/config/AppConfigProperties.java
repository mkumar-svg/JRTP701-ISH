package com.nit.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix="plan.module")
@EnableAutoConfiguration
public class AppConfigProperties {
	private Map<String, String> messages;
}
