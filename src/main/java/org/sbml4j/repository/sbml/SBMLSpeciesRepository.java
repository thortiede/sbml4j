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
package org.sbml4j.repository.sbml;

import java.util.List;

import org.sbml4j.model.sbml.SBMLSpecies;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SBMLSpeciesRepository extends Neo4jRepository<SBMLSpecies, Long> {
	
	public List<SBMLSpecies> findBysBaseName(String sBaseName);

	public SBMLSpecies findBysBaseId(String sBaseId);
	
	public SBMLSpecies findByEntityUUID(String entityUUID);
	
	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[b:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE b.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND b.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "AND e.primaryName = $name "
			+ "AND e.databaseFromUri = $databaseFromUri "
			+ "RETURN s")
	public Iterable<SBMLSpecies> findByBQConnectionTo(String name, String databaseFromUri);

	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[b:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE b.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND b.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "AND e.uri = $uri "
			+ "RETURN s")
	public Iterable<SBMLSpecies> findByBQConnectionWithUri(String uri);

	@Query("MATCH "
			+ "(c:SBMLCompartment)"
			+ "<-[:CONTAINED_IN]-"
			+ "(s:SBMLSpecies)"
			+ "-[b:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE b.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND b.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "AND e.uri in $uriList "
			+ "RETURN c, s")
	public Iterable<SBMLSpecies> findByBQConnectionWithUriList(List<String> uriList);

	
	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[b:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "-[KNOWNAS]->"
			+ "(n:NameNode) "
			+ "WHERE b.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND b.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "AND n.name = $name "
			+ "RETURN s")
	public Iterable<SBMLSpecies> findByExternalResourceSecondaryName(String name);
	
	@Query("MATCH "
			+ "(g:SBMLSpeciesGroup)"
			+ "-[h:HAS_GROUP_MEMBER]->"
			+ "(s:SBMLSpecies) "
			+ "WHERE g.entityUUID = $groupEntityUUID "
			+ "RETURN s")
	public Iterable<SBMLSpecies> getSBMLSpeciesOfGroup(String groupEntityUUID);

	
	/*@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[w:Warehouse]-"
			+ "(p:PathwayNode) "
			+ "WHERE p.entityUUID = $pathwayEntityUUID "
			+ "AND NOT EXISTS {"
			+ " MATCH (s)-[c]-(e:GraphBaseEntity) "
			+ "WHERE type(c) <> warehouseGraphEdge"
			+ "")
	public Iterable<SBMLSpecies> getAllUnconnectedSpeciesOfPathway(String pathwayEntityUUID);
	*/
}
