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

package org.tts.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.config.SBML4jConfig;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.AnnotationName;
import org.tts.repository.common.GraphBaseEntityRepository;

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
	 * Find a <a href="#{@link}">{@link GraphBaseEntity}</a> by its entityUUID attribute
	 * 
	 * @param entityUUID The entityUUID of the entity to find
	 * @return The <a href="#{@link}">{@link GraphBaseEntity}</a> found for the entityUUID attribute.
	 */
	public GraphBaseEntity findByEntityUUID(String entityUUID) {
		return graphBaseEntityRepository.findByEntityUUID(entityUUID);
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
			String newAnnotationValue = (String) annotationValue;
			if(appendExisting) {
				String separator = ", "; // this is the default separator
				if (entity.getAnnotation().containsKey(annotationName)) {
					if (this.sbml4jConfig.getAnnotationConfigProperties().isSetSeparator(AnnotationName.get(annotationName))) {
						separator = sbml4jConfig.getAnnotationConfigProperties().getSeparator(AnnotationName.get(annotationName));
					}
					String existingAnnotationString = (String) entity.getAnnotation().get(annotationName);
					newAnnotationValue = existingAnnotationString + separator + (String) annotationValue;
				}
			}
			entity.addAnnotation(annotationName, newAnnotationValue);
			entity.addAnnotationType(annotationName, annotationType);
		} else {
			// differnt type than string, nothing we can append to at this point
			if(entity.getAnnotation().get(annotationName) == null || !this.sbml4jConfig.getAnnotationConfigProperties().isKeepFirst()) {
				entity.addAnnotation(annotationName, annotationValue);
				entity.addAnnotationType(annotationName, annotationType);
			}
		}
		return entity;
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
	}
	
	/**
	 * Reset the id and version of the <a href="#{@link}">{@link GraphBaseEntity}</a> and assign new entityUUID
	 * 
	 * @param target the <a href="#{@link}">{@link GraphBaseEntity}</a> for which the basic properties (entityUUID, id, version) are to be reset.
	 */
	public void resetGraphBaseEntityProperties(GraphBaseEntity target) {
		target.setEntityUUID(this.getNewUUID());
		target.setId(null);
		target.setVersion(null);
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
