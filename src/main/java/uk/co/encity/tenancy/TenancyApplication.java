package uk.co.encity.tenancy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication
// Stop Spring from trying to auto-configure MongoDB.  This might change, but for now we're doing it 'manually'
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class TenancyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenancyApplication.class, args);
	}

}
