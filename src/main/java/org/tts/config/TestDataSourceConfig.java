package org.tts.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestDataSourceConfig implements DataSourceConfig {

	@Override
	public void setup() {
		System.out.println("Setting up datasource for TEST environment. ");
	}

}
