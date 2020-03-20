package org.tts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tts.config.DataSourceConfig;

@SpringBootApplication
public class Sbml4jApplication {
	@Autowired
	DataSourceConfig dataSourceConfig;

	public static void main(String[] args) {
		SpringApplication.run(Sbml4jApplication.class, args);
	}
	
}
