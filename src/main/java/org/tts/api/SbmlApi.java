/**
 * NOTE: This class is auto generated by OpenAPI Generator
 * (https://openapi-generator.tech) (5.0.1). https://openapi-generator.tech Do
 * not edit the class manually.
 */
package org.tts.api;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.api.PathwayInventoryItem;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T14:01:17.376485+01:00[Europe/Berlin]")
@Validated
@Api(value = "Sbml", description = "the Sbml API")
public interface SbmlApi {

  default Optional<NativeWebRequest> getRequest() { return Optional.empty(); }

  /**
   * POST /sbml : Upload SBML Model to create a Pathway
   *
   * @param organism The three-letter organism code (required)
   * @param source The name of the source this SBML originates from (required)
   * @param version The version of the source this SBML originates from
   *     (required)
   * @param user The user which requests the creation, the configured public
   *     user will be used if omitted (optional)
   * @param files  (optional)
   * @return Pathways from models created (status code 201)
   *         or Bad Request (status code 400)
   */
  @ApiOperation(value = "Upload SBML Model to create a Pathway",
                nickname = "uploadSBML", notes = "",
                response = PathwayInventoryItem.class,
                responseContainer = "List",
                tags =
                    {
                        "sbml",
                    })
  @ApiResponses(value =
                {
                  @ApiResponse(code = 201,
                               message = "Pathways from models created",
                               response = PathwayInventoryItem.class,
                               responseContainer = "List")
                  ,
                      @ApiResponse(code = 400, message = "Bad Request")
                })
  @PostMapping(value = "/sbml", produces = {"application/json"},
               consumes = {"multipart/form-data"})
  default ResponseEntity<List<PathwayInventoryItem>>
  uploadSBML(
      @NotNull @ApiParam(value = "The three-letter organism code",
                         required = true) @Valid
      @RequestParam(value = "organism", required = true) String organism,
      @NotNull
      @ApiParam(value = "The name of the source this SBML originates from",
                required = true) @Valid
      @RequestParam(value = "source", required = true) String source,
      @NotNull
      @ApiParam(value = "The version of the source this SBML originates from",
                required = true) @Valid
      @RequestParam(value = "version", required = true) String version,
      @ApiParam(
          value =
              "The user which requests the creation, the configured public user will be used if omitted")
      @RequestHeader(value = "user", required = false) String user,
      @ApiParam(value = "") @Valid @RequestPart(value = "files",
                                                required = false)
      List<MultipartFile> files) {
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
}
