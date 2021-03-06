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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.api.PathwayInventoryItem;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
@Api(value = "Sbml", description = "the Sbml API")
public interface SbmlApi {

    Logger log = LoggerFactory.getLogger(SbmlApi.class);

    default Optional<ObjectMapper> getObjectMapper(){
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest(){
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Upload SBML Model to create a Pathway", nickname = "uploadSBML", notes = "", response = PathwayInventoryItem.class, responseContainer = "List", tags={ "sbml", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Pathway from model created", response = PathwayInventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/sbml",
        produces = { "application/json" }, 
        consumes = { "multipart/form-data" },
        method = RequestMethod.POST)
    default ResponseEntity<Map<String,PathwayInventoryItem>> uploadSBML(@ApiParam(value = "The xml-file containing the sbml model" ,required=true )  @Valid @RequestBody MultipartFile[] files
,@ApiParam(value = "The user which requests the creation" ,required=true) @RequestHeader(value="user", required=true) String user
,@NotNull @ApiParam(value = "The three-letter organism code", required = true) @Valid @RequestParam(value = "organism", required = true) String organism
,@NotNull @ApiParam(value = "The name of the source this SBML originates from", required = true) @Valid @RequestParam(value = "source", required = true) String source
,@NotNull @ApiParam(value = "The version of the source this SBML originates from", required = true) @Valid @RequestParam(value = "version", required = true) String version
) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                	Map<String, PathwayInventoryItem> defaultReturnMap = new HashMap<>();
                	defaultReturnMap.put("default", getObjectMapper().get().readValue("[ {\n  \"transitionTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Pathway\",\n  \"organismCode\" : \"hsa\",\n  \"numberOfTransitions\" : 6,\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\",\n  \"pathwayId\" : \"path_hsa05225\",\n  \"compartments\" : [ \"Cytosol\", \"Cytosol\" ]\n}, {\n  \"transitionTypes\" : [ \"stimulation\", \"stimulation\" ],\n  \"name\" : \"Example Pathway\",\n  \"organismCode\" : \"hsa\",\n  \"numberOfTransitions\" : 6,\n  \"links\" : [ {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  }, {\n    \"rel\" : \"graphml\",\n    \"href\" : \"http://example.com:8080/sbml4j/networks/d25f4b9-8dd5-4bc3-9d04-9af418302244?format=graphml\"\n  } ],\n  \"numberOfNodes\" : 0,\n  \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",\n  \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ],\n  \"networkMappingType\" : \"REGULATORY\",\n  \"pathwayId\" : \"path_hsa05225\",\n  \"compartments\" : [ \"Cytosol\", \"Cytosol\" ]\n} ]", PathwayInventoryItem.class) );
                    return new ResponseEntity<>(defaultReturnMap, HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default SbmlApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
