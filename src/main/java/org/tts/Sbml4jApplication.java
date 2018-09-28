package org.tts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.tts.property.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
			FileStorageProperties.class
})
public class Sbml4jApplication {

	public static void main(String[] args) {
		SpringApplication.run(Sbml4jApplication.class, args);
	}
}
