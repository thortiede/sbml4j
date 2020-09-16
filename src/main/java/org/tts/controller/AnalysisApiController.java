/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.AnalysisApi;
import org.tts.model.api.GeneAnalysisItem;
import org.tts.service.networks.NetworkResourceService;

/**
 * Controller class for handling Requests to the Analysis API
 * @author Thorsten Tiede
 *
 * @since 0.1
 */

@Controller
public class AnalysisApiController implements AnalysisApi {
	
	@Autowired
	NetworkResourceService networkResourceService;
	
	
	/**
	 * Get a <a href="#{@link}">{@link GeneAnalysisItem}</a> for a single geneSymbol
	 * 
	 * @param geneSymbol The symbol for which to get the item
	 * @return ResponseEntity holding the generated geneAnalysisItem
	 */
	public ResponseEntity<GeneAnalysisItem> getGeneAnalysis(String geneSymbol) {
		GeneAnalysisItem geneAnalysisItem = new GeneAnalysisItem();
		geneAnalysisItem.setGene(geneSymbol);
		return new ResponseEntity<GeneAnalysisItem>(geneAnalysisItem, HttpStatus.NOT_IMPLEMENTED);
	}
	
	/**
	 * Get a list of <a href="#{@link}">{@link GeneAnalysisItem}</a> for symbols in geneSymbols
	 * 
	 * @param geneSymbols List of symbols to get <a href="#{@link}">{@link GeneAnalysisItem}</a>  for
	 * @return ResponseEntity with a List of <a href="#{@link}">{@link GeneAnalysisItem}</a> 
	 */
	public ResponseEntity<List<GeneAnalysisItem>> getGeneSetAnalysis(List<String> geneSymbols) {
		List<GeneAnalysisItem> geneAnalysisItems = new ArrayList<>();
		for (String geneSymbol : geneSymbols) {
			GeneAnalysisItem geneAnalysisItem = new GeneAnalysisItem();
			geneAnalysisItem.setGene(geneSymbol);
			geneAnalysisItems.add(geneAnalysisItem);
		}
		return new ResponseEntity<List<GeneAnalysisItem>>(geneAnalysisItems, HttpStatus.NOT_IMPLEMENTED);
	}
	
	/**
	 * Get geneSet for a list of geneSymbols in a network
	 * 
	 * @param user User requesting the geneSet (Needs to match the user for the network)
	 * @param geneSymbols List of geneSymbols to get a geneSet for
	 * @param UUID UUID of the network to extract the geneSet from
	 * @param directed boolean indicating whether the graphML-Graph should be directed
	 * @return ResponseEntity with GraphML-formatted Resource containing the geneSet
	 * 	
	 */
	public ResponseEntity<Resource> getGeneSet(	  String user
												, List<String> geneSymbols
												, UUID UUID
												, boolean directed) {
		
		Resource resource = this.networkResourceService.getNetwork(UUID.toString(), geneSymbols, directed);
		if(resource == null) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			String contentType = "application/octet-stream";
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"geneset.graphml\"").body(resource) ;
		} 		
	}
}
