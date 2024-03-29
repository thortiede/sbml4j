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
 *
 * NOTE: This class is auto generated by OpenAPI Generator
 * (https://openapi-generator.tech) (5.0.1). https://openapi-generator.tech Do
 * not edit the class manually.
 */
package org.sbml4j.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.sbml4j.model.api.network.NetworkInventoryItem;
import org.sbml4j.model.api.pathway.PathwayCollectionCreationItem;
import org.sbml4j.model.api.pathway.PathwayInventoryItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-03-26T12:09:15.257519+01:00[Europe/Berlin]")
@Validated
@Api(value = "Pathways", description = "the Pathways API")
public interface PathwaysApi {

  default Optional<NativeWebRequest> getRequest() { return Optional.empty(); }

  /**
   * POST /pathwayCollection : Create a collectionPathway from the submitted
   * pathways
   *
   * @param pathwayCollectionCreationItem List of pathwayUUIDs and a
   *     databaseUUID to create the collection for (required)
   * @param user The user which requests the creation, the configured public
   *     user will be used if omitted (optional)
   * @return CollectionPathway created (status code 201)
   *         or Invalid input (status code 400)
   */
  @ApiOperation(
      value = "Create a collectionPathway from the submitted pathways",
      nickname = "createPathwayCollection", notes = "", response = String.class,
      tags =
          {
              "pathways",
          })
  @ApiResponses(value =
                {
                  @ApiResponse(code = 201,
                               message = "CollectionPathway created",
                               response = String.class)
                  ,
                      @ApiResponse(code = 400, message = "Invalid input")
                })
  @PostMapping(value = "/pathwayCollection", produces = {"application/json"},
               consumes = {"application/json"})
  default ResponseEntity<String>
  createPathwayCollection(
      @ApiParam(
          value =
              "List of pathwayUUIDs and a databaseUUID to create the collection for",
          required = true) @Valid
      @RequestBody PathwayCollectionCreationItem pathwayCollectionCreationItem,
      @ApiParam(
          value =
              "The user which requests the creation, the configured public user will be used if omitted")
      @RequestHeader(value = "user", required = false) String user) {
    getRequest().ifPresent(request -> {
      for (MediaType mediaType :
           MediaType.parseMediaTypes(request.getHeader("Accept"))) {
        if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
          String exampleString = "\"0d25f4b9-8dd5-4bc3-9d04-9af418302244\"";
          ApiUtil.setExampleResponse(request, "application/json",
                                     exampleString);
          break;
        }
      }
    });
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * GET /pathwayUUIDs : List UUIDs of available pathways
   *
   * @param user The user which requests listing of their pathways, the
   *     configured public user will be used if omitted (optional)
   * @param hideCollections Do hide Collection Pathways in the output (optional,
   *     default to false)
   * @return Bad Request (status code 400)
   *         or successful operation (status code 200)
   */
  @ApiOperation(value = "List UUIDs of available pathways",
                nickname = "listAllPathwayUUIDs", notes = "",
                response = String.class, responseContainer = "List",
                tags =
                    {
                        "pathways",
                    })
  @ApiResponses(value =
                {
                  @ApiResponse(code = 400, message = "Bad Request")
                  , @ApiResponse(code = 200, message = "successful operation",
                                 response = String.class,
                                 responseContainer = "List")
                })
  @GetMapping(value = "/pathwayUUIDs", produces = {"application/json"})
  default ResponseEntity<List<String>>
  listAllPathwayUUIDs(
      @ApiParam(
          value =
              "The user which requests listing of their pathways, the configured public user will be used if omitted")
      @RequestHeader(value = "user", required = false) String user,
      @ApiParam(value = "Do hide Collection Pathways in the output",
                defaultValue = "false") @Valid
      @RequestParam(value = "hideCollections", required = false,
                    defaultValue = "false") Boolean hideCollections) {
    getRequest().ifPresent(request -> {
      for (MediaType mediaType :
           MediaType.parseMediaTypes(request.getHeader("Accept"))) {
        if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
          String exampleString = "\"0d25f4b9-8dd5-4bc3-9d04-9af418302244\"";
          ApiUtil.setExampleResponse(request, "application/json",
                                     exampleString);
          break;
        }
      }
    });
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * GET /pathways : List available pathways
   *
   * @param user The user which requests listing of their pathways, the
   *     configured public user will be used if omitted (optional)
   * @param hideCollections Do hide Collection Pathways in the output (optional,
   *     default to false)
   * @return Bad Request (status code 400)
   *         or successful operation (status code 200)
   */
  @ApiOperation(value = "List available pathways", nickname = "listAllPathways",
                notes = "", response = PathwayInventoryItem.class,
                responseContainer = "List",
                tags =
                    {
                        "pathways",
                    })
  @ApiResponses(value =
                {
                  @ApiResponse(code = 400, message = "Bad Request")
                  , @ApiResponse(code = 200, message = "successful operation",
                                 response = PathwayInventoryItem.class,
                                 responseContainer = "List")
                })
  @GetMapping(value = "/pathways", produces = {"application/json"})
  default ResponseEntity<List<PathwayInventoryItem>>
  listAllPathways(
      @ApiParam(
          value =
              "The user which requests listing of their pathways, the configured public user will be used if omitted")
      @RequestHeader(value = "user", required = false) String user,
      @ApiParam(value = "Do hide Collection Pathways in the output",
                defaultValue = "false") @Valid
      @RequestParam(value = "hideCollections", required = false,
                    defaultValue = "false") Boolean hideCollections) {
    getRequest().ifPresent(request -> {
      for (MediaType mediaType :
           MediaType.parseMediaTypes(request.getHeader("Accept"))) {
        if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
          String exampleString =
              "{ \"transitionTypes\" : [ \"stimulation\", \"stimulation\" ], \"name\" : \"Example Pathway\", \"organismCode\" : \"hsa\", \"numberOfTransitions\" : 6, \"numberOfNodes\" : 0, \"numberOfReactions\" : 1, \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"nodeTypes\" : [ \"polypeptide chain\", \"polypeptide chain\" ], \"pathwayId\" : \"path_hsa05225\", \"compartments\" : [ \"Cytosol\", \"Cytosol\" ] }";
          ApiUtil.setExampleResponse(request, "application/json",
                                     exampleString);
          break;
        }
      }
    });
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * POST /mapping/{UUID} : Map pathwayContents on a new network representation
   *
   * @param UUID The UUID of the pathway to be mapped (required)
   * @param mappingType The type of mapping to create (required)
   * @param user The user which requests the creation, the configured public
   *     user will be used if omitted (optional)
   * @param networkname An optional name for the newly generated network, if
   *     omitted a default name will be generated in the format
   *     orgCode-mappingType-source-version (optional)
   * @param prefixName Flag whether to prefix the given networkname on the
   *     generated default-name (orgCode-mappingType-source-version) (true) or
   *     use networkname as sole name for the created network (false) (optional,
   *     default to false)
   * @param suffixName Flag whether to suffix the given networkname on the
   *     generated default-name (orgCode-mappingType-source-version) (true) or
   *     use networkname as sole name for the created network (false) (optional,
   *     default to false)
   * @return Bad Request (status code 400)
   *         or Mapping for Pathway created (status code 201)
   */
  @ApiOperation(value = "Map pathwayContents on a new network representation",
                nickname = "mapPathway", notes = "",
                response = NetworkInventoryItem.class,
                tags =
                    {
                        "pathways",
                    })
  @ApiResponses(value =
                {
                  @ApiResponse(code = 400, message = "Bad Request")
                  , @ApiResponse(code = 201,
                                 message = "Mapping for Pathway created",
                                 response = NetworkInventoryItem.class)
                })
  @PostMapping(value = "/mapping/{UUID}", produces = {"application/json"})
  default ResponseEntity<NetworkInventoryItem>
  mapPathway(
      @ApiParam(value = "The UUID of the pathway to be mapped",
                required = true) @PathVariable("UUID") UUID UUID,
      @NotNull
      @ApiParam(value = "The type of mapping to create", required = true,
                allowableValues =
                    "METABOLIC, PPI, REGULATORY, SIGNALLING, PATHWAYMAPPING")
      @Valid @RequestParam(value = "mappingType",
                           required = true) String mappingType,
      @ApiParam(
          value =
              "The user which requests the creation, the configured public user will be used if omitted")
      @RequestHeader(value = "user", required = false) String user,
      @ApiParam(
          value =
              "An optional name for the newly generated network, if omitted a default name will be generated in the format orgCode-mappingType-source-version")
      @Valid @RequestParam(value = "networkname",
                           required = false) String networkname,
      @ApiParam(
          value =
              "Flag whether to prefix the given networkname on the generated default-name (orgCode-mappingType-source-version) (true) or use networkname as sole name for the created network (false)",
          defaultValue = "false") @Valid
      @RequestParam(value = "prefixName", required = false,
                    defaultValue = "false") Boolean prefixName,
      @ApiParam(
          value =
              "Flag whether to suffix the given networkname on the generated default-name (orgCode-mappingType-source-version) (true) or use networkname as sole name for the created network (false)",
          defaultValue = "false") @Valid
      @RequestParam(value = "suffixName", required = false,
                    defaultValue = "false") Boolean suffixName) {
    getRequest().ifPresent(request -> {
      for (MediaType mediaType :
           MediaType.parseMediaTypes(request.getHeader("Accept"))) {
        if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
          String exampleString =
              "{ \"name\" : \"Example Mapping\", \"organismCode\" : \"hsa\", \"UUID\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\" }";
          ApiUtil.setExampleResponse(request, "application/json",
                                     exampleString);
          break;
        }
      }
    });
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }
}
