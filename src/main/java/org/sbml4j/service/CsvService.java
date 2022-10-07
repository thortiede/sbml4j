/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

@Service
public class CsvService implements RowProcessor {

	@Autowired
	ConfigService configService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	Map<String, List< Map<String, String> > > geneToAnnotationMap;
	
	private int matchRowNum;
	private String matchingColumn;
	private boolean isMatchingRowDetermined = false;
	
	/**
	 * Read the contents of the provided file into a HashMap
	 * @param file The File to read
	 * @return The HashMap containing the data mapped to the configured matching column
	 * @throws IOException When the file is not readable
	 */
	public Map<String, List< Map<String, String> > > parseCsv(MultipartFile file, String matchingColumn) throws IOException {
		// TODO:Funnel in the column number that holds the matching gene symbol
		// Then use it in rowProcessed to match the correct column instead of 0
		 this.matchingColumn = matchingColumn;
		
		logger.debug("Parsing file " + file.getOriginalFilename());
		
		CsvParserSettings settings = new CsvParserSettings();
		
		//turns on automatic detection of line separators, column separators, quotes & quote escapes
		settings.detectFormatAutomatically();

		settings.setHeaderExtractionEnabled(true);
		settings.setRowProcessor(this);
		
		CsvParser parser = new CsvParser(settings);

		parser.parse(getReader(file));

		//Let's see the detected format:
		CsvFormat detectedFormat = parser.getDetectedFormat();
		logger.debug(detectedFormat.toString());
		
		return this.geneToAnnotationMap;
		
		
	}
	
	
	@Override
	public void processStarted(ParsingContext context) {
		logger.debug("parsing process started");
		geneToAnnotationMap = new HashMap<>();
		// are there matching column names configured?
		if (!this.configService.areMatchingColumnsConfigured()) {
			logger.warn("No matching column configured for gene symbol matching. Falling back to first column. Please configure at least one element in the 'sbml4j.csv.matching-column-name' config option.");
			this.matchRowNum = 0;
			this.isMatchingRowDetermined = true;
		}
		
	}

	@Override
	public void rowProcessed(String[] row, ParsingContext context) {
		if (!this.isMatchingRowDetermined) {
			String[] headers = context.headers();
			this.matchRowNum = Arrays.asList(headers).indexOf(this.matchingColumn);
			if (this.matchRowNum == -1) {
				logger.debug("Failed to find provided (or default) column " + this.matchingColumn + ". Trying configured columns.");
				for (int i = 0; i!= headers.length; i++) {
					
					if (this.configService.isInMatchingColums(headers[i])) {
						logger.debug("Using configured column " + headers[i] + " for matching row values.");
						this.matchRowNum = i;
						this.isMatchingRowDetermined = true;
						break;
					}
				}
				if (!this.isMatchingRowDetermined) {
					// we could not find a column name matching any of the configured names
					logger.warn("Unable to determine matching row. Falling back to first column");
					this.matchRowNum = 0;
					this.isMatchingRowDetermined = true;
				}
			} else {
				logger.debug("Using provided column " + this.matchingColumn + " for matching values row values.");
				this.isMatchingRowDetermined = true;
			}
		}
		if (row[this.matchRowNum] == null) {
			// the matching column is not populated in this row
			return;
		}
		if (!this.geneToAnnotationMap.containsKey(row[this.matchRowNum])) {
			this.geneToAnnotationMap.put(row[this.matchRowNum], new ArrayList<>());
		}
		List<Map<String, String> > annotationMap = geneToAnnotationMap.get(row[this.matchRowNum]);
		Map<String, String> entryMap = new HashMap<>();
		for (int i=0; i != row.length; i++) {
			if (i != this.matchRowNum && !(row[i] == null)) {
				entryMap.put(context.headers()[i], row[i]);
			}
		}
		annotationMap.add(entryMap);
	}

	@Override
	public void processEnded(ParsingContext context) {
		logger.debug("parsing process finished");
				
	}
	
	private Reader getReader(MultipartFile file) throws IOException {
		return new InputStreamReader(file.getInputStream());
	}

}
