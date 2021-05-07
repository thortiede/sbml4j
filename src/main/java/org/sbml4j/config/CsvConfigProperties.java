package org.tts.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.csv")
public class CsvConfigProperties {
	private List<String> matchingColumnName;

	public List<String> getMatchingColumnName() {
		return matchingColumnName;
	}

	public void setMatchingColumnName(List<String> matchingColumnName) {
		this.matchingColumnName = matchingColumnName;
	}
	

}
