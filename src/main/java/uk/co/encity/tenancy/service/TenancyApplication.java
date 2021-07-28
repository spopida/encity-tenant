package uk.co.encity.tenancy.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// Stop Spring from trying to auto-configure MongoDB.  This might change, but for now we're doing it 'manually'
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class TenancyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenancyApplication.class, args);
	}

}
