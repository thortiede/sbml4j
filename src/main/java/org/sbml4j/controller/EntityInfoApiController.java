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
package org.sbml4j.controller;

import java.util.ArrayList;
import java.util.List;

import org.sbml4j.api.EntityInfoApi;
import org.sbml4j.model.api.entityInfo.EntityInfoItem;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.SBML.EntityInfoService;
import org.sbml4j.service.networks.NetworkResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Controller class for handling Requests to the Analysis API
 * @author Thorsten Tiede
 *
 * @since 0.1
 */

@Controller
public class EntityInfoApiController implements EntityInfoApi {
	
	
	@Autowired
	ConfigService configService;
	
	@Autowired
	EntityInfoService entityInfoService;
	
	@Autowired
	NetworkResourceService networkResourceService;
	
	
	/**
	 * Get a <a href="#{@link}">{@link EntityInfoItem}</a> for a single geneSymbol
	 * 
	 * @param geneSymbol The symbol for which to get the item
	 * @return ResponseEntity holding the generated geneAnalysisItem
	 */
	public ResponseEntity<List<EntityInfoItem>> getEntityInfo(String geneSymbol) {
		List<EntityInfoItem> entityInfoItems;
		try {
			entityInfoItems = this.entityInfoService.getGeneAnalysis(geneSymbol);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
		return new ResponseEntity<List<EntityInfoItem>>(entityInfoItems, HttpStatus.OK);
	}
	
	/**
	 * Get a list of <a href="#{@link}">{@link EntityInfoItem}</a> for symbols in geneSymbols
	 * 
	 * @param geneSymbols List of symbols to get <a href="#{@link}">{@link EntityInfoItem}</a>  for
	 * @return ResponseEntity with a List of <a href="#{@link}">{@link EntityInfoItem}</a> 
	 */
	public ResponseEntity<List<EntityInfoItem>> getGeneSetAnalysis(List<String> geneSymbols) {
		List<EntityInfoItem> entityInfoItems = new ArrayList<>();
		for (String geneSymbol : geneSymbols) {
			entityInfoItems.addAll(this.entityInfoService.getGeneAnalysis(geneSymbol));
		}
		return new ResponseEntity<List<EntityInfoItem>>(entityInfoItems, HttpStatus.OK);
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
	/*public ResponseEntity<Resource> getGeneSet(@NotNull @Valid List<String> geneSymbols, String user, @Valid UUID UUID,
			@Valid Boolean directed) {
		
		try {
			this.configService.getUserNameAttributedToNetwork(UUID.toString(), user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest().header("reason", e.getMessage() != null ? e.getMessage() : "User " +user + " not authorized").build();
		}
		Resource resource = this.networkResourceService.getNetwork(UUID.toString(), geneSymbols, directed);
		if(resource == null) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			String contentType = "application/octet-stream";
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"geneset.graphml\"").body(resource) ;
		} 		
	}*/
}
