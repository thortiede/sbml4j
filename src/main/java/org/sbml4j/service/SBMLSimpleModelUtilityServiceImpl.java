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
package org.sbml4j.service;

import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentalizedSBase;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml4j.model.base.GraphBaseEntity;
import org.sbml4j.model.full.SBMLDocumentEntity;
import org.sbml4j.model.full.SBMLModelEntity;
import org.sbml4j.model.sbml.SBMLCompartment;
import org.sbml4j.model.sbml.SBMLCompartmentalizedSBaseEntity;
import org.sbml4j.model.sbml.SBMLSBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpecies;
import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SBMLSimpleModelUtilityServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/******************************************************************************************************************************
	 * 													Setter for SBML Core
	 ******************************************************************************************************************************/
				
		/**
		 * Set Properties of SBMLDocument entity (or any entity derived from it)
		 * from a jSBML SBMLDocument entity
		 * @param source The SBMLDocument entity from jSBML ({@link org.sbml.jsbml.SBMLDocument}) from which the attributes are to be taken
		 * @param target The SBMLDocumentEntity entity to which the attributes are to be applied to
		 * @param filename The original filename where the document was taken from
		 */
		void setSBMLDocumentProperties(SBMLDocument source, SBMLDocumentEntity target,
				String filename) {
			target.setSbmlFileName(filename);
			target.setSbmlLevel(source.getLevel());
			target.setSbmlVersion(source.getVersion());
			target.setSbmlXmlNamespace(source.getNamespace());
		}

		/**
		 * Set Properties of SBMLModelEntity (or any entity derived from it)
		 * from a jSBML Model Entity
		 * @param model The Model entity from jSBML ({@link org.sbml.jsbml.Model}) from which the attributes are to be taken
		 * @param sbmlDocument The SBMLDocumentEntity entity which encloses this SBMLModel, it will be referenced in {@linkplain sbmlModelEntity}
		 * @param sbmlModelEntity The SBMLModelEntity to which the attributes are to be applied to
		 */
		void setModelProperties(Model model, SBMLDocumentEntity sbmlDocument, SBMLModelEntity sbmlModelEntity) {
			/**
			 * The following string attributes contain IDRefs to 
			 * UnitDefinitions.
			 * TODO: actually link to the UnitDefinition-Object here.
			 * Since current queries don't use them, we skip them for now
			 * and only save the ids to the graph
			 * Once we load the UnitDefinitions
			 * a) manual linking is possible
			 * b) linking here on creation or on a second pass becomes possible.
			 */
			sbmlModelEntity.setSubstanceUnits(model.getSubstanceUnits());
			sbmlModelEntity.setTimeUnits(model.getTimeUnits());
			sbmlModelEntity.setVolumeUnits(model.getVolumeUnits());
			sbmlModelEntity.setAreaUnits(model.getAreaUnits());
			sbmlModelEntity.setLengthUnits(model.getLengthUnits());
			sbmlModelEntity.setConversionFactor(model.getConversionFactor());
			sbmlModelEntity.setEnclosingSBMLEntity(sbmlDocument);
		}
	
	/**
	 * Set Properties of SBMLCompartment entity (or any entity derived from it)
	 * from a jSBML Compartment entity
	 * @param source The Compartment entity from jSBML ({@link org.sbml.jsbml.Compartment}) from which the attributes are to be taken
	 * @param target The SBMLComartment entity to which the attributes are to be applied to
	 */
	void setCompartmentProperties(Compartment source, SBMLCompartment target, DatabaseNode database) {
		target.setSpatialDimensions(source.getSpatialDimensions());
		target.setSize(source.getSize());
		target.setConstant(source.getConstant());
		target.setUnits(source.getUnits()); // at some point we want to link to a unit definiton here
		target.setDatabase(database);
	}
	
	/**
	 * Set Properties of GraphBaseEntity.
	 * This is only the EntityUUID so far and no source entity is needed to copy properties from.
	 * @param target the GraphBaseEntity for which the basic properties (entityUUID) are to be set.
	 */
	@Deprecated
	void setGraphBaseEntityProperties(GraphBaseEntity target) {
		target.setEntityUUID(UUID.randomUUID().toString());
		target.setActive(true);
	}
	
	/**
	 * Reset the id and version of the entity and assign new entityUUID
	 * @param target the GraphBaseEntity for which the basic properties (entityUUID, id, version) are to be reset.
	 */
	@Deprecated
	void resetGraphBaseEntityProperties(GraphBaseEntity target) {
		target.setEntityUUID(UUID.randomUUID().toString());
		target.setId(null);
		target.setVersion(null);
	}
	/**
	 * Reset the id and version of the entity and assign an entityUUID
	 * @param uuid The uuid to be assigned to target
	 * @param target the GraphBaseEntity for which the basic properties (entityUUID, id, version) are to be reset.
	 */
	void resetGraphBaseEntityProperties(String uuid, GraphBaseEntity target) {
		target.setEntityUUID(uuid);
		target.setId(null);
		target.setVersion(null);
	}
	
	/**
	 * Set Properties of SBMLSBaseEntity (or any entity derived from it)
	 * from a jSBML SBase entity
	 * @param sbmlSimpleModelServiceImpl TODO
	 * @param source The SBase entity from jSBML ({@link org.sbml.jsbml.SBase}) from which the attributes are to be taken
	 * @param target The SBMLSBaseEntity entity to which the attributes are to be applied to
	 */
	void setSbaseProperties(SBase source, SBMLSBaseEntity target) {
		target.setsBaseId(source.getId());
		target.setsBaseName(source.getName());
		try {
			target.setsBaseNotes(source.getNotesString());
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.logger.debug("Unable to generate Notes String for Entity " + source.getName());
		}
		target.setsBaseMetaId(source.getMetaId());
		target.setsBaseSboTerm(source.getSBOTermID());
		if(source.getNumCVTerms() > 0) {
			target.setCvTermList(source.getCVTerms());
		}
	}
	
	/**
	 * Set Properties of SBMLCompartmentalizedSBaseEntity entity (or any entity derived from it)
	 * from a jSBML CompartmentalizedSBase entity
	 * @param sbmlSimpleModelServiceImpl TODO
	 * @param source The CompartmentalizedSBase entity from jSBML ({@link org.sbml.jsbml.CompartmentalizedSBase}) from which the attributes are to be taken
	 * @param target The SBMLCompartmentalizedSBaseEntity entity to which the attributes are to be applied to
	 * @param compartmentLookupMap A Map to lookup the correct SBMLCompartment which this CompartmentalizedSBase is located in
	 */
	void setCompartmentalizedSbaseProperties(CompartmentalizedSBase source, SBMLCompartmentalizedSBaseEntity target, Map<String, SBMLCompartment> compartmentLookupMap) {
		target.setCompartmentMandatory(source.isCompartmentMandatory());
		if(compartmentLookupMap.containsKey(source.getCompartment())) {
			target.setCompartment(compartmentLookupMap.get(source.getCompartment()));
		} else {
			this.logger.debug("No matching compartment found for compartment " + source.getCompartment() + " in " + source.getName());
		}
	}
	
	/**
	 * Set Properties of SBMLSpecies entity (or any entity derived from it)
	 * from a jSBML Species entity
	 * @param source The Species Entity from jSBML ({@link org.sbml.jsbml.Species)} from which the attributes are to be taken
	 * @param target The SBMLSpecies Entity to which the attributes are to be applied
	 */
	void setSpeciesProperties(Species source, SBMLSpecies target) {
		target.setInitialAmount(source.getInitialAmount());
		target.setInitialConcentration(source.getInitialConcentration());
		target.setBoundaryCondition(source.getBoundaryCondition());
		target.setHasOnlySubstanceUnits(source.hasOnlySubstanceUnits());
		target.setConstant(source.isConstant());	
	}
	
	/**
	 * Set Properties of SBMLSimpleReaction
	 * from a jSBML Reaction entity 
	 * @param source The Reaction entity from jsbml ({@link org.sbml.jsbml.Reaction}) from which the attributes are to be taken
	 * @param target The SBMLSimpleReaction entity to which the attributes are to be applied to
	 */
	void setSimpleReactionProperties(Reaction source, SBMLSimpleReaction target) {
		if (source.isSetReversible()) target.setReversible(source.isReversible());
	}
	
	
	/******************************************************************************************************************************
	 * 													Setter for Extensions
	 ******************************************************************************************************************************/
		
	/******************************************************************************************************************************
	 * 													Setter for Extension QUAL
	 ******************************************************************************************************************************/
	
	/**
	 * Set Properties of SBMLQualSpecies entity (or any entity derived from it)
	 * from a jSBML-Qual-Extension QualitativeSpecies entity
	 * @param source The QualitativeSpecies entity from jSBML-Qual-Extension ({@link org.sbml.sbml.ext.qual.QualitativeSpecies}) from which the attributes are to be taken
	 * @param target The SBMLQualSpecies entity to which the attributes are to be applied to
	 */
	void setQualSpeciesProperties(QualitativeSpecies source, SBMLQualSpecies target) {
		target.setConstant(source.getConstant());
		/** 
		 * initialLevel and maxLevel are not always set, so the jsbml entity has methods for checking if they are
		 * Consider adding those to our Entities as well.
		 */
		
		if (source.isSetInitialLevel()) target.setInitialLevel(source.getInitialLevel());
		if (source.isSetMaxLevel()) target.setMaxLevel(source.getMaxLevel());
	}

}
