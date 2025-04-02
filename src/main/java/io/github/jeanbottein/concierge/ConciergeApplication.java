package io.github.jeanbottein.concierge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.modulith.Modulith;

@SpringBootApplication
@EnableCaching
@Modulith
public class ConciergeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConciergeApplication.class, args);
	}

}
