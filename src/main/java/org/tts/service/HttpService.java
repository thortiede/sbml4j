package org.tts.service;

import java.util.List;

import org.tts.model.common.ExternalResourceEntity;

public interface HttpService {

	public List<String> getGeneNamesFromKeggURL(String resource);

	public ExternalResourceEntity setCompoundAnnotationFromResource(String resource, ExternalResourceEntity entity);	
}
