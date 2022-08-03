package org.sbml4j;
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
//package org.sbml4j;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.sbml4j.config.DataSourceConfig;
//import org.sbml4j.controller.WarehouseController;
//import org.sbml4j.model.api.Input.FilterOptions;
//import org.sbml4j.model.queryResult.NetworkInventoryItem;
//import org.sbml4j.model.queryResult.NetworkInventoryItemDetail;
//import org.sbml4j.model.queryResult.PathwayInventoryItem;
//import org.sbml4j.model.base.GraphEnum.MappingStep;
//import org.sbml4j.property.FileStorageProperties;
//
//@SpringBootApplication
//@EnableConfigurationProperties({
//			FileStorageProperties.class
//})
//public class Sbml4jProfilerApplication {
//
//	DataSourceConfig dataSourceConfig;
//	WarehouseController warehouseController;
//	
//	@Autowired
//	public Sbml4jProfilerApplication(DataSourceConfig dataSourceConfig,
//									 WarehouseController warehouseController
//									 ) {
//		super();
//		this.dataSourceConfig = dataSourceConfig;
//		this.warehouseController = warehouseController;
//	}
//	
//	public static void main(String[] args) {
//		SpringApplication.run(Sbml4jProfilerApplication.class, args);
//	}
//	
//	@Bean
//    public CommandLineRunner profilerRunner(ApplicationContext ctx) {
//        String user = "Testuser";
//    /*    ResponseEntity<List<PathwayInventoryItem>> allPathwaysForUserResponse = this.warehouseController.listAllPathways("Thor");
//        if(allPathwaysForUserResponse.getStatusCode().equals(HttpStatus.OK)) {
//        	System.out.println("Number of pathways: " + allPathwaysForUserResponse.getBody().size());
//        	for(PathwayInventoryItem item : allPathwaysForUserResponse.getBody()) {
//        		if(!this.warehouseController.mappingForPathwayExists(item.getEntityUUID())) {
//        			// found a pathway for which no mapping is created yet, create mapping
//        			System.out.println("Found pathway with uuid: " + item.getEntityUUID());
//        		} else {
//        			System.out.println("Pathway " + item.getEntityUUID() + " already has a mapping, continue");
//        		}
//        	}
//        } else {
//        	System.out.println("Status of listAllPathways is " + allPathwaysForUserResponse.getStatusCode().toString());
//        }	
//        */	
//        ResponseEntity<List<NetworkInventoryItem>> allNetworksForUserResponse = this.warehouseController.listAllNetworks(user);
//        if(allNetworksForUserResponse.getStatusCode().equals(HttpStatus.OK)) {
//        	System.out.println("Number of networks: " + allNetworksForUserResponse.getBody().size());
//        } else {
//        	System.out.println("Status of listAllNetworks is " + allNetworksForUserResponse.getStatusCode().toString());
//        }
//        	
//        String parentUUID = "c7539963-4aed-4eb1-a200-92cc7bf39bd3";
//        ResponseEntity<FilterOptions> optionsResponse = this.warehouseController.getNetworkFilterOptions(parentUUID);
//        if (optionsResponse.getStatusCode().equals(HttpStatus.OK)) {
//        	//FilterOptions options = optionsResponse.getBody();
//        	//ResponseEntity<NetworkInventoryItemDetail> newMapDetailResponse = this.warehouseController.createMappingFromMappingWithOptions(user, parentUUID, MappingStep.COPY, options);
//        	//if(newMapDetailResponse.getStatusCode().equals(HttpStatus.OK)) {
//        	//	NetworkInventoryItemDetail newMapDetail = newMapDetailResponse.getBody();
//        	//	String newMapEntityUUID = newMapDetail.getEntityUUID();
//        		// get Filter Options
//        		//optionsResponse = this.warehouseController.getNetworkFilterOptions(parentUUID);
//        		// then create context for each node
//        		//if (optionsResponse.getStatusCode().equals(HttpStatus.OK)) {
//        			List<String> nodeSymbolsList = optionsResponse.getBody().getNodeSymbols();
//        			System.out.println("NodeSymbols: " + nodeSymbolsList.toString());
//        			
//        			for (String nodeSymbol : nodeSymbolsList) {
//        				if (nodeSymbol != null) {
////        					List<String> newNodeSymbols = new ArrayList<>();
////        					newNodeSymbols.add(nodeSymbol);
////        					options.setNodeSymbols(newNodeSymbols);
////        					options.setMinSize(1);
////        					options.setMaxSize(2);
//        					
//        					//ResponseEntity<NetworkInventoryItemDetail> contextDetailResponse = this.warehouseController.createMappingFromMappingWithOptions(user, parentUUID, MappingStep.CONTEXT, options);
//        					ResponseEntity<Resource> contextDetailResponse = this.warehouseController.getContext(user, parentUUID, nodeSymbol, 1, 2, false, "graphml");
//        					if(contextDetailResponse.getStatusCode().equals(HttpStatus.OK)) {
//        						System.out.println("Successfully read network context for Symbol " + nodeSymbol + " in mapping " + parentUUID + " resulting in new Mapping with uuid: " + contextDetailResponse.getBody().getFilename());
//        					} else {
//        						System.out.println("Failed to create network context for symbol " + nodeSymbol + " in mapping " + parentUUID + " with error " + contextDetailResponse.getStatusCode().toString());
//        					}
//        		        	
//        				}
//        			}
//		} else {
//			System.out.println("Failed to get filterOptions for " + parentUUID);
//		}
//		return null;
//       
//	}
//	
//}
