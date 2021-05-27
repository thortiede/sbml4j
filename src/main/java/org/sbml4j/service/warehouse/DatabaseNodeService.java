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
package org.sbml4j.service.warehouse;

import java.util.List;

import org.sbml4j.model.common.Organism;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.sbml4j.repository.warehouse.DatabaseNodeRepository;
import org.sbml4j.service.GraphBaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
	/**
	 * Find all <a href="#{@link}">{@link DatabaseNode}</a> from which the Pathways with given entityUUIDs are derived from
	 * @param pathwayUUIDList The list of Strings representing the entityUUIDs of the <a href="#{@link}">{@link PathwayNode}</a>s
	 * @return List of <a href="#{@link}">{@link DatabaseNode}</a>s from which the pathways are derived
	 */
	public List<DatabaseNode> findByPathwayList(List<String> pathwayUUIDList) {
		return this.databaseNodeRepository.findByPathwayList(pathwayUUIDList);
	}
}
