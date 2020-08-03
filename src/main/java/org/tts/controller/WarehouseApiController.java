/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.controller;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.WarehouseApi;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.service.WarehouseGraphService;

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
	WarehouseGraphService warehouseGraphService;
	
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

		DatabaseNode database = this.warehouseGraphService.getDatabaseNode(source, version, organism);
		if (database != null) {
			return new ResponseEntity<UUID>(UUID.fromString(database.getEntityUUID()), HttpStatus.OK);
		} else {
			return new ResponseEntity<UUID>(HttpStatus.NOT_FOUND);
		}
	}
}
