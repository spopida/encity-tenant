package uk.co.encity.tenancy.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "tenancy")
@Getter @Setter
public class ConfigProperties {
    private int expiryHours;
}
