package org.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vcf.config")
public class VcfConfigProperties {
	private boolean useSharedPathwaySearch;

	public boolean isUseSharedPathwaySearch() {
		return useSharedPathwaySearch;
	}

	public void setUseSharedPathwaySearch(boolean useSharedPathwaySearch) {
		this.useSharedPathwaySearch = useSharedPathwaySearch;
	}
	
	
	
}
