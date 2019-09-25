package org.tts;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tts.config.DataSourceConfig;
import org.tts.controller.WarehouseController;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.NetworkInventoryItemDetail;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.property.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
			FileStorageProperties.class
})
public class Sbml4jProfilerApplication {

	DataSourceConfig dataSourceConfig;
	WarehouseController warehouseController;
	
	@Autowired
	public Sbml4jProfilerApplication(DataSourceConfig dataSourceConfig,
									 WarehouseController warehouseController
									 ) {
		super();
		this.dataSourceConfig = dataSourceConfig;
		this.warehouseController = warehouseController;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Sbml4jProfilerApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner profilerRunner(ApplicationContext ctx) {
        String user = "Testuser";
    /*    ResponseEntity<List<PathwayInventoryItem>> allPathwaysForUserResponse = this.warehouseController.listAllPathways("Thor");
        if(allPathwaysForUserResponse.getStatusCode().equals(HttpStatus.OK)) {
        	System.out.println("Number of pathways: " + allPathwaysForUserResponse.getBody().size());
        	for(PathwayInventoryItem item : allPathwaysForUserResponse.getBody()) {
        		if(!this.warehouseController.mappingForPathwayExists(item.getEntityUUID())) {
        			// found a pathway for which no mapping is created yet, create mapping
        			System.out.println("Found pathway with uuid: " + item.getEntityUUID());
        		} else {
        			System.out.println("Pathway " + item.getEntityUUID() + " already has a mapping, continue");
        		}
        	}
        } else {
        	System.out.println("Status of listAllPathways is " + allPathwaysForUserResponse.getStatusCode().toString());
        }	
        */	
        ResponseEntity<List<NetworkInventoryItem>> allNetworksForUserResponse = this.warehouseController.listAllNetworks(user);
        if(allNetworksForUserResponse.getStatusCode().equals(HttpStatus.OK)) {
        	System.out.println("Number of networks: " + allNetworksForUserResponse.getBody().size());
        } else {
        	System.out.println("Status of listAllNetworks is " + allNetworksForUserResponse.getStatusCode().toString());
        }
        	
        String parentUUID = "118b8969-449a-4d8d-b7af-ebbe35ea8a7e";
        ResponseEntity<FilterOptions> optionsResponse = this.warehouseController.getNetworkFilterOptions(parentUUID);
        if (optionsResponse.getStatusCode().equals(HttpStatus.OK)) {
        	FilterOptions options = optionsResponse.getBody();
        	ResponseEntity<NetworkInventoryItemDetail> newMapDetailResponse = this.warehouseController.createMappingFromMappingWithOptions(user, parentUUID, MappingStep.COPY, options);
        	if(newMapDetailResponse.getStatusCode().equals(HttpStatus.OK)) {
        		NetworkInventoryItemDetail newMapDetail = newMapDetailResponse.getBody();
        		String newMapEntityUUID = newMapDetail.getEntityUUID();
        		// get Filter Options
        		optionsResponse = this.warehouseController.getNetworkFilterOptions(newMapEntityUUID);
        		// then create context for each node
        		if (optionsResponse.getStatusCode().equals(HttpStatus.OK)) {
        			List<String> nodeSymbolsList = optionsResponse.getBody().getNodeSymbols();
        			
        			for (String nodeSymbol : nodeSymbolsList) {
        				if (nodeSymbol != null) {
        					List<String> newNodeSymbols = new ArrayList<>();
        					newNodeSymbols.add(nodeSymbol);
        					options.setNodeSymbols(newNodeSymbols);
        					options.setMinSize(1);
        					options.setMaxSize(2);
        					
        					ResponseEntity<NetworkInventoryItemDetail> contextDetailResponse = this.warehouseController.createMappingFromMappingWithOptions(user, parentUUID, MappingStep.CONTEXT, options);
        					if(contextDetailResponse.getStatusCode().equals(HttpStatus.OK)) {
        						System.out.println("Successfully created network context for Symbol " + nodeSymbol + " in mapping " + newMapEntityUUID + " resulting in new Mapping with uuid: " + contextDetailResponse.getBody().getEntityUUID());
        					} else {
        						System.out.println("Failed to create network context for symbol " + nodeSymbol + " in mapping " + newMapEntityUUID + " with error " + contextDetailResponse.getStatusCode().toString());
        					}
        		        	
        				}
        			}
        		} else {
        			System.out.println("Failed to get filterOptions for " + newMapEntityUUID);
        		}
        	} else {
        		System.out.println("Failed to copy mapping " + parentUUID + " with options " + options.toString());
        	}
        } else {
        	System.out.println("Failed to get filterOptions for parent " + parentUUID);
        }   
		return null;
       
	}
	
}
