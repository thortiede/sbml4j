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
package org.tts.service.warehouse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.Organism;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.repository.warehouse.DatabaseNodeRepository;
import org.tts.service.GraphBaseEntityService;

/**
 * Service to handle creation and querying of <a href="#{@link}">{@link DatabaseNode}</a> entities
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class DatabaseNodeService {

	@Autowired
	DatabaseNodeRepository databaseNodeRepository;

	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	/**
	 * Get the <a href="#{@link}">{@link DatabaseNode}</a> for the input parameters
	 * 
	 * @param source The source name of the <a href="#{@link}">{@link DatabaseNode}</a> 
	 * @param sourceVersion The version of the <a href="#{@link}">{@link DatabaseNode}</a> 
	 * @param org The <a href="#{@link}">{@link Organism}</a> associated with this <a href="#{@link}">{@link DatabaseNode}</a> 
	 * @return The <a href="#{@link}">{@link DatabaseNode}</a> matching the input parameters
	 */
	public DatabaseNode createDatabaseNode(String source, String sourceVersion, Organism org) {
		DatabaseNode database = new DatabaseNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(database);
		database.setOrganism(org);
		database.setSource(source);
		database.setSourceVersion(sourceVersion);
		// database.setMatchingAttributes(matchingAttributes);

		return this.databaseNodeRepository.save(database);
	}
	
	/**
	 * Get a <a href="#{@link}">{@link DatabaseNode}</a> by the entityUUID
	 * 
	 * @param databaseEntityUUID The UUID of the <a href="#{@link}">{@link DatabaseNode}</a>
	 * @return The <a href="#{@link}">{@link DatabaseNode}</a> with entityUUID databaseEntityUUID
	 */
	public DatabaseNode findByEntityUUID(String databaseEntityUUID) {
		return this.databaseNodeRepository.findByEntityUUID(databaseEntityUUID);
	}
	
	/**
	 * Get the <a href="#{@link}">{@link DatabaseNode}</a> for the input parameters
	 * 
	 * @param source The source name of the <a href="#{@link}">{@link DatabaseNode}</a> 
	 * @param sourceVersion The version of the <a href="#{@link}">{@link DatabaseNode}</a> 
	 * @param org three letter organism code of the <a href="#{@link}">{@link DatabaseNode}</a> 
	 * @return The <a href="#{@link}">{@link DatabaseNode}</a> matching the input parameters
	 */
	public DatabaseNode getDatabaseNode(String source, String sourceVersion, String org) {
		
		List<DatabaseNode> databaseNodeList = this.databaseNodeRepository.findBySourceAndSourceVersion(source,
				sourceVersion);
		if (databaseNodeList.isEmpty()) {
			return null;
		} else if (databaseNodeList.size() == 1) {
			return databaseNodeList.get(0);
		} else {
			for (DatabaseNode database : databaseNodeList) {
				if (database.getOrganism().getOrgCode().equals(org)) {
					return database;
				}
			}
			return null;
		}
	}

	
}