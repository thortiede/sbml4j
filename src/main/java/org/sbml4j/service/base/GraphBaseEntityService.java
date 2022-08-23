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
package org.sbml4j.service.base;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.base.GraphBaseEntity;
import org.sbml4j.model.base.GraphEnum.AnnotationName;
import org.sbml4j.repository.base.GraphBaseEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling loading, manipulating and persisting of <a href="#{@link}">{@link GraphBaseEntity}</a>
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class GraphBaseEntityService {

	@Autowired
	GraphBaseEntityRepository graphBaseEntityRepository;

	@Autowired
	SBML4jConfig sbml4jConfig;
	
	
	/**
	 * Add annotation to a <a href="#{@link}">{@link GraphBaseEntity}</a>
	 * 
	 * Adds an entry to Annotation and a matching entry to AnnotationType
	 * 
	 * @param entity The <a href="#{@link}">{@link GraphBaseEntity}</a> to add the annotation to
	 * @param annotationName The name of the annotation element
	 * @param annotationType The type of the annotation element
	 * @param annotationValue The value of the annotation element
	 * @param appendExisting Append the annotation if an annotation with the same name already exists (true), or overwrite otherwise (false)
	 * @return The changed <a href="#{@link}">{@link GraphBaseEntity}</a> with the new annotation (not persisted in the database yet)
	 */
	public GraphBaseEntity addAnnotation(GraphBaseEntity entity, String annotationName, String annotationType, Object annotationValue,
			boolean appendExisting) {
		if(annotationType.equals("string")) {
			String newAnnotationValue = annotationValue.toString();
			if(appendExisting) {
				String separator = ", "; // this is the default separator
				if (entity.getAnnotation().containsKey(annotationName)) {
					if (this.sbml4jConfig.getAnnotationConfigProperties().isSetSeparator(AnnotationName.get(annotationName))) {
						separator = sbml4jConfig.getAnnotationConfigProperties().getSeparator(AnnotationName.get(annotationName));
					}
					String existingAnnotationString = (String) entity.getAnnotation().get(annotationName);
					if (existingAnnotationString.contains((String) annotationValue)) { // this might be expensive but prevents duplicates
						return entity;
					} else {
						newAnnotationValue = existingAnnotationString + separator + (String) annotationValue;
					}
				}
			}
			entity.addAnnotation(annotationName, newAnnotationValue);
			entity.addAnnotationType(annotationName, annotationType);
		} else {
			// differnt type than string, nothing we can append to at this point
			if(entity.getAnnotation().get(annotationName) == null || !this.sbml4jConfig.getAnnotationConfigProperties().isKeepFirst()) {
				if("integer".equals(annotationType)) {
					// The neo4j database cannot handle the Java Type Integer.
					// Those values need to be converted to Long
					if (annotationValue instanceof Long) {
						entity.addAnnotation(annotationName, annotationValue);
					} else {
						entity.addAnnotation(annotationName, Long.valueOf((String) annotationValue));
					}
				} else {
					entity.addAnnotation(annotationName, annotationValue);
				}
				entity.addAnnotationType(annotationName, annotationType);
				
			}
		}
		return entity;
	}
	
	/**
	 * Delete a GraphBaseEntity
	 * 
	 * @param entity The <a href="#{@link}">{@link GraphBaseEntity}</a> to be deleted
	 * @return true if deleting was successful (if the entity is not present in database), false otherwise
	 */
	public boolean deleteEntity(GraphBaseEntity entity) {
		if (entity == null) {
			return false;
		}
		this.graphBaseEntityRepository.delete(entity);
		if (this.graphBaseEntityRepository.findByEntityUUID(entity.getEntityUUID()) == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Find All <a href="#{@link}">{@link GraphBaseEntity}</a> entities.
	 * 
	 * @return Iterable with all <a href="#{@link}">{@link GraphBaseEntity}</a> in the database
	 */
	public Iterable<GraphBaseEntity> findAll() {
		return graphBaseEntityRepository.findAll();
	}

	/**
	 * Find a <a href="#{@link}">{@link GraphBaseEntity}</a> by its entityUUID attribute
	 * 
	 * @param entityUUID The entityUUID of the entity to find
	 * @return The <a href="#{@link}">{@link GraphBaseEntity}</a> found for the entityUUID attribute.
	 */
	public GraphBaseEntity findByEntityUUID(String entityUUID) {
		return graphBaseEntityRepository.findByEntityUUID(entityUUID);
	}

	/**
	 * Save a single <a href="#{@link}">{@link GraphBaseEntity}</a> in the database
	 * 
	 * @param newEntity The <a href="#{@link}">{@link GraphBaseEntity}</a> to save
	 * @param depth The save-depth to use
	 * @return The persisted <a href="#{@link}">{@link GraphBaseEntity}</a>
	 */
	public GraphBaseEntity persistEntity(GraphBaseEntity newEntity, int depth) {
		return this.graphBaseEntityRepository.save(newEntity, depth);
	}

	/**
	 * Reset the id and version of the <a href="#{@link}">{@link GraphBaseEntity}</a> and assign new entityUUID
	 * 
	 * @param target the <a href="#{@link}">{@link GraphBaseEntity}</a> for which the basic properties (entityUUID, id, version) are to be reset.
	 */
	public void resetGraphBaseEntityProperties(GraphBaseEntity target) {
		target.setEntityUUID(this.getNewUUID());
		target.setId(null);
		target.setCreateDate(Date.from(Instant.now()));
		/**
		 * TODO: Uncomment this line when the version field is reintroduced
		 */
		//target.setVersion(null);
	}
	
	/**
	 * Set Properties of <a href="#{@link}">{@link GraphBaseEntity}</a>.
	 * This is only the EntityUUID so far and no source entity is needed to copy properties from.
	 * 
	 * @param target the <a href="#{@link}">{@link GraphBaseEntity}</a> for which the basic properties (entityUUID) are to be set.
	 */
	public void setGraphBaseEntityProperties(GraphBaseEntity target) {
		target.setEntityUUID(this.getNewUUID());
		target.setActive(true);
		target.setCreateDate(Date.from(Instant.now()));
	}
	
	/**
	 * This is the one point where a new entityUUID is generated.
	 * 
	 * The current method is just random. Change this here, if another generationMethod is needed.
	 * @return String representing the newly generated UUID
	 */
	private String getNewUUID() {
		return UUID.randomUUID().toString();
	}
	
}
