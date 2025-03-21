package io.github.jeanbottein.concierge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConciergeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConciergeApplication.class, args);
	}

}
