package org.tts.service;

import java.util.List;

public interface HttpService {

	public List<String> getGeneNamesFromKeggURL(String resource);
}
