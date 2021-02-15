package org.tts.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

@Service
public class CsvService implements RowProcessor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	Map<String, List< Map<String, String> > > geneToAnnotationMap;
	
	public Map<String, List< Map<String, String> > > parseCsv(MultipartFile file) throws IOException {
		
		 
		
		logger.debug("Parsing file " + file.getOriginalFilename());
		
		CsvParserSettings settings = new CsvParserSettings();
		
		//turns on automatic detection of line separators, column separators, quotes & quote escapes
		settings.detectFormatAutomatically();

		settings.setHeaderExtractionEnabled(true);
		settings.setRowProcessor(this);
		
		CsvParser parser = new CsvParser(settings);

		List<String[]> rows;
		parser.parse(getReader(file));

		//Let's see the detected format:
		CsvFormat detectedFormat = parser.getDetectedFormat();
		logger.debug(detectedFormat.toString());
		
		return this.geneToAnnotationMap;
		
		
	}
	
	private Reader getReader(MultipartFile file) throws IOException {
		return new InputStreamReader(file.getInputStream());
	}

	@Override
	public void processStarted(ParsingContext context) {
		logger.debug("Reached processStarted");
		geneToAnnotationMap = new HashMap<>();
		
	}

	@Override
	public void rowProcessed(String[] row, ParsingContext context) {
		if (!this.geneToAnnotationMap.containsKey(row[0])) {
			this.geneToAnnotationMap.put(row[0], new ArrayList<>());
		}
		List<Map<String, String> > annotationMap = geneToAnnotationMap.get(row[0]);
		Map<String, String> entryMap = new HashMap<>();
		for (int i=1; i != row.length; i++) {
			if (!(row[i] == null)) {
				entryMap.put(context.headers()[i], row[i]);
			}
		}
		annotationMap.add(entryMap);
	}

	@Override
	public void processEnded(ParsingContext context) {
		logger.debug("Reached processEnded");
				
	}
}
