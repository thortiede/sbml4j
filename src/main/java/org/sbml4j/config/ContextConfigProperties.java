package org.sbml4j.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.context")
public class ContextConfigProperties {
	private int minSize;
	private int maxSize;
	private String terminateAt;
	private String direction;
	
	public int getMinSize() {
		return minSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public String getTerminateAt() {
		return terminateAt;
	}
	public void setTerminateAt(String terminateAt) {
		this.terminateAt = terminateAt;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
}
