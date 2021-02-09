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

/*
 * NOTE: This class is auto generated by the swagger code generator program (3.0.20).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.tts.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.NetworkOptions;
import org.tts.model.api.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
@Api(value = "Networks", description = "the Networks API")
public interface NetworksApi {

    Logger log = LoggerFactory.getLogger(NetworksApi.class);

    default Optional<ObjectMapper> getObjectMapper(){
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest(){
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Add annotation to network nodes and/or relationships. Results in the creation of a new network entity, keeping the entity with UUID unchanged. ", nickname = "addAnnotationToNetwork", notes = "", response = NetworkInventoryItem.class, responseContainer = "List", authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "network created", response = NetworkInventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid input") })
    @RequestMapping(value = "/networks/{UUID}/annotation",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<NetworkInventoryItem> addAnnotationToNetwork(@ApiParam(value = "The fields of the AnnotationItem are used to create annotations in a network as follows:"
    		+ " "
    		+ "Must provide"
    		+ "- either all fields associated with nodes (nodeAnnotation, nodeAnnotationName, nodeAnnotationType)"
    		+ "- or all fields associated wth relations (relationAnnotation, relationAnnotationName, relationAnnotationType)"
    		+ "- or both."
    		+ " "
    		+ "Fields related to nodes:"
    		+ " "
    		+ "- nodeAnnotationName denotes the name under which the annotations in the nodeAnnotation field of the request body are attached to nodes"
    		+ "- nodeAnnotationType is used as type with which the annotations in the nodeAnnotation field of the request body are attached to nodes. Is also used to read out the annotation in said format when extracting network"
    		+ "- nodeAnnotation is a dictionary of \"node-symbol\": \"annotation\" - pairs where node-symbol must match one of the node symbols in the network (see field nodeSymbols in FilterOptions); the annotation is added to the node under the name defined in nodeAnnotationName using the type in nodeAnnotationType"
    		+ " "
    		+ " "
    		+ "Fields related to relations:"
    		+ "- relationAnnotationName denotes the name under which the annotations in the relationAnnotation field of the request body are attached to relations"
    		+ " "
    		+ "- relationAnnotationType is used as type with which the annotations in the relationAnnotation field of the request body are attached to relations. Is also used to read out the annotation in said format when extracting network"
    		+ "- relationAnnotation is a dictionary of \"relation-symbol\": \"annotation\" - pairs where realation-symbol must match one of the relation symbols in the network (see field relationSymbols in FilterOptions); the annotation is added to the relation under the name defined in relationAnnotationName using the type in relationAnnotationType" ,required=true )  @Valid @RequestBody AnnotationItem body
,@ApiParam(value = "The user which requests the creation" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the parent network to derive a new network from",required=true) @PathVariable("UUID") UUID uuid
,@ApiParam(value = "Flag whether to derive a new network and add annotation to it (true) or add annotation to existing network without deriving subnetwork (false)") @RequestParam(value = "derive", required = false, defaultValue="true") boolean derive
,@ApiParam(value = "A network name or name prefix for the annotated network") @RequestParam(value = "networkname", required = false) String networkname
,@ApiParam(value = "Flag whether to prefix the given networkname on the original network name (true) or use networkname as sole name for the created network (if network is derived) ") @RequestParam(value = "prefixName", required = false, defaultValue="false") boolean prefixName
,@ApiParam(value = "Flag whether to prefix the given networkname on the original network name (true) or use networkname as sole name for the created network (if network is not derived) ") @RequestParam(value = "prefixName", required = false, defaultValue="false") boolean prefixName
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}, {\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}", NetworkInventoryItem.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @ApiOperation(value = "Provide a csv-file with Drivergene information", tags={ "networks" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created network with Drivergene information added.", response = NetworkInventoryItem.class),
        
        @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/networks/{UUID}/csv",
        produces = { "application/json" }, 
        consumes = { "multipart/form-data" }, 
        method = RequestMethod.POST)
    ResponseEntity<NetworkInventoryItem> addCsvDataToNetwork(@ApiParam(value = "The csv-file containing the Drivergene information" ,required=true )  @Valid @RequestBody MultipartFile[] data,
    															 @RequestHeader(value="user", required=true) String user,
    															 @PathVariable("UUID") UUID UUID,
    															 @RequestParam(value = "type", required = true) String type,
    															 @Valid @RequestParam(value = "networkname", required = false) String networkname,
    															 @Valid @RequestParam(value = "prefixName", required = false) boolean prefixName
    															 );
    

    @ApiOperation(value = "Provide a URL to a MyDrug Neo4j Database and add Drug nodes and Drug-targets->Gene Relationships to a network", nickname = "addMyDrugRelations", notes = "", response = NetworkInventoryItem.class, tags={ "networks","mydrug", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created network with MyDrug nodes and relations added.", response = NetworkInventoryItem.class),
        @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/networks/{UUID}/myDrug",
        produces = { "application/json" }, 
        method = RequestMethod.POST)
    default ResponseEntity<NetworkInventoryItem> addMyDrugRelations(@ApiParam(value = "The user which requests the creation" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the network that the myDrug Data should be added to. A copy will be created.",required=true) @PathVariable("UUID") UUID UUID
,@NotNull @ApiParam(value = "A base url of a MyDrug Database including the port number", required = true) @Valid @RequestParam(value = "myDrugURL", required = true) String myDrugURL
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}", NetworkInventoryItem.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Create a copy of the network with uuid UUID", nickname = "copyNetwork", notes = "", response = NetworkInventoryItem.class, responseContainer = "List", authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "network copied", response = NetworkInventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid input"),
    	@ApiResponse(code = 403, message = "The current user is forbidden from accessing this data") })
    @RequestMapping(value = "/networks/{UUID}",
        produces = { "application/json" }, 
        method = RequestMethod.POST)
    default ResponseEntity<NetworkInventoryItem> copyNetwork(@ApiParam(value = "The user which requests the creation" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the parent network to be copied",required=true) @PathVariable("UUID") UUID UUID
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<NetworkInventoryItem>(getObjectMapper().get().readValue("{\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}, {\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n} ", NetworkInventoryItem.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Request the deletion of a network", nickname = "deleteNetwork", notes = "", authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Network deleted."),
        @ApiResponse(code = 403, message = "The current user is forbidden from accessing this data"),
        @ApiResponse(code = 404, message = "The network you tried to delete does not exist") })
    @RequestMapping(value = "/networks/{UUID}",
        method = RequestMethod.DELETE)
    default ResponseEntity<Void> deleteNetwork(@ApiParam(value = "The user which requests deletion of their network" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the network to delete",required=true) @PathVariable("UUID") UUID UUID
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Filter a network according to the request body options. Results in the creation of a new network entity, keeping the entity with UUID unchanged.", nickname = "filterNetwork", notes = "", response = NetworkInventoryItem.class, responseContainer = "List", authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "network created", response = NetworkInventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid input") })
    @RequestMapping(value = "/networks/{UUID}/filter",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<NetworkInventoryItem> filterNetwork(@ApiParam(value = "The filterOptions to be used in the step upon creation."
    		+ " "
    		+ "- NodeSymbols: Only nodes listed here can be part of the network (and if they match given nodeTypes)"
    		+ "- NodeTypes: Only nodes having one of the listed nodesTypes here can be part of the network (and if they are contained in NodeSymbols)"
    		+ "- RelationSymbols: Only relations listed here can be part of the network (and if they match given relationTypes)"
    		+ "- RelationTypes: Only edges having one of the listed relationTypes here can be part of the network" ,required=true )  @Valid @RequestBody FilterOptions body
,@ApiParam(value = "The user which requests the creation" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the parent network to derive a new network from",required=true) @PathVariable("UUID") UUID UUID
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}, {\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}", NetworkInventoryItem.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Get network context of one or multiple genes in the network with uuid {UUID}", nickname = "getContext", notes = "", response = Resource.class, authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = Resource.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 403, message = "The current user is forbidden from accessing this data") })
    @RequestMapping(value = "/networks/{UUID}/context",
        produces = { "application/octet-stream" }, 
        method = RequestMethod.GET)
    default ResponseEntity<Resource> getContext(@ApiParam(value = "" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the network that serves as a basis for this context",required=true) @PathVariable("UUID") UUID UUID
,@NotNull @ApiParam(value = "", required = true) @Valid @RequestParam(value = "genes", required = true) String genes
,@ApiParam(value = "The minimum depth of the context search", defaultValue = "1") @Valid @RequestParam(value = "minSize", required = false, defaultValue="1") Integer minSize
,@ApiParam(value = "The maximum depth of the context search", defaultValue = "3") @Valid @RequestParam(value = "maxSize", required = false, defaultValue="3") Integer maxSize
,@ApiParam(value = "find nodes of this type and stop the expansion there", defaultValue = "false") @Valid @RequestParam(value = "terminateAt", required = false, defaultValue="false") String terminateAt
,@ApiParam(value = "The direction of the context expansion (upstream, downstream, both)", allowableValues = "upstream, downstream, both", defaultValue = "both") @Valid @RequestParam(value = "direction", required = false, defaultValue="both") String direction
,@ApiParam(value = "Denotes whether the return network graph is directed", defaultValue = "false") @Valid @RequestParam(value = "directed", required = false, defaultValue="false") boolean directed
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("\"\"", Resource.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Retrieve the contents of a network", nickname = "getNetwork", notes = "", response = Resource.class, authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = Resource.class),
        @ApiResponse(code = 204, message = "Network is empty or not available"),
        @ApiResponse(code = 403, message = "The current user is forbidden from accessing this data") })
    @RequestMapping(value = "/networks/{UUID}",
        produces = { "application/octet-stream" }, 
        method = RequestMethod.GET)
    default ResponseEntity<Resource> getNetwork(@ApiParam(value = "The user which requests the creation" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the network to get",required=true) @PathVariable("UUID") UUID UUID
,@ApiParam(value = "Boolean flag wether the resulting network-graph should be directed ", defaultValue = "false") @Valid @RequestParam(value = "directed", required = false, defaultValue="false") boolean directed
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("\"\"", Resource.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Get available options for network", nickname = "getNetworkOptions", notes = "", response = NetworkOptions.class, authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = NetworkOptions.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 403, message = "The current user is forbidden from accessing this data") })
    @RequestMapping(value = "/networks/{UUID}/options",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<NetworkOptions> getNetworkOptions(@ApiParam(value = "" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The network UUID for which the networkOptions are to be fetched",required=true) @PathVariable("UUID") UUID UUID
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{\n  \"annotation\" : {\n    \"nodeAnnotationType\" : \"boolean\",\n    \"relationAnnotationType\" : \"int\",\n    \"relationAnnotation\" : {\n      \"BRAF-stimulation->BRCA\" : 1,\n      \"BRCA-inhibition->BRAF\" : 5\n    },\n    \"nodeAnnotationName\" : \"DriverGene\",\n    \"relationAnnotationName\" : \"traversalScore\",\n    \"nodeAnnotation\" : {\n      \"BRAF\" : true,\n      \"BRCA\" : false\n    }\n  },\n  \"filter\" : {\n    \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n    \"nodeSymbols\" : [ \"BRCA1\", \"BRCA1\" ],\n    \"relationSymbols\" : [ \"BRCA1-stimulation->BRAF\", \"BRCA1-stimulation->BRAF\" ],\n    \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ]\n  }\n}", NetworkOptions.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "List available networks", nickname = "listAllNetworks", notes = "", response = NetworkInventoryItem.class, responseContainer = "List", authorizations = {
        @Authorization(value = "network_auth", scopes = { 
            @AuthorizationScope(scope = "write:networks", description = "modify networks in your account"),
            @AuthorizationScope(scope = "read:networks", description = "read your networks")
            })    }, tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = NetworkInventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/networks",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<List<NetworkInventoryItem>> listAllNetworks(@ApiParam(value = "The user which requests listing of their networks" ,required=true) @RequestHeader(value="user", required=true) String user
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("[ {\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}, {\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Create network context of one or multiple genes in the network with uuid {UUID}", nickname = "postContext", notes = "", response = NetworkInventoryItem.class, responseContainer = "List", tags={ "networks", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "context network created", response = NetworkInventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 403, message = "The current user is forbidden from accessing this data") })
    @RequestMapping(value = "/networks/{UUID}/context",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<NetworkInventoryItem> postContext(@ApiParam(value = "The genes for which the context should be generated" ,required=true )  @Valid @RequestBody NodeList body
,@ApiParam(value = "" ,required=true) @RequestHeader(value="user", required=true) String user
,@ApiParam(value = "The UUID of the network that serves as a basis for this context",required=true) @PathVariable("UUID") UUID UUID
,@ApiParam(value = "The minimum depth of the context search", defaultValue = "1") @Valid @RequestParam(value = "minSize", required = false, defaultValue="1") Integer minSize
,@ApiParam(value = "The maximum depth of the context search", defaultValue = "3") @Valid @RequestParam(value = "maxSize", required = false, defaultValue="3") Integer maxSize
,@ApiParam(value = "find nodes of this type and stop the expansion there", defaultValue = "false") @Valid @RequestParam(value = "terminateAt", required = false, defaultValue="false") String terminateAt
,@ApiParam(value = "The direction of the context expansion (upstream, downstream, both)", allowableValues = "upstream, downstream, both", defaultValue = "both") @Valid @RequestParam(value = "direction", required = false, defaultValue="both") String direction
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}, {\n  \"relationTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Network\",\n  \"organismCode\" : \"hsa\",\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"numberOfRelations\" : 6,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\"\n}", NetworkInventoryItem.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default NetworksApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
