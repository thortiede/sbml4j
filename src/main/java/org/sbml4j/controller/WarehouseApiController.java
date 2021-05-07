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

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.sbml4j.api.WarehouseApi;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.sbml4j.service.warehouse.DatabaseNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Controller class handling requests on warehouse specific items (i.e. DatabaseNode)
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class WarehouseApiController implements WarehouseApi {

	@Autowired
	DatabaseNodeService databaseNodeService;
	
	/**
	 * Get the UUID of the <a href="#{@link}">{@link DatabaseNode}</a> for the given parameters
	 * 
	 * @param organism The three letter organism code that database is for
	 * @param source The name of the database
	 * @param version The version of the database
	 * @return The UUID of the <a href="#{@link}">{@link DatabaseNode}</a>
	 */
	@Override
	public ResponseEntity<UUID> getDatabaseUUID(@NotNull @Valid String organism, @NotNull @Valid String source,
		@NotNull @Valid String version) {

		DatabaseNode database = this.databaseNodeService.getDatabaseNode(source, version, organism);
		if (database != null) {
			return new ResponseEntity<UUID>(UUID.fromString(database.getEntityUUID()), HttpStatus.OK);
		} else {
			return new ResponseEntity<UUID>(HttpStatus.NOT_FOUND);
		}
	}
}
