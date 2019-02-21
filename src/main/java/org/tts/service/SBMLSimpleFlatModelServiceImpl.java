package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.xml.XMLNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.GraphBaseEntity;
import org.tts.model.HelperQualSpeciesReturn;
import org.tts.model.SBMLCompartment;
import org.tts.model.SBMLQualSpecies;
import org.tts.model.SBMLQualSpeciesGroup;
import org.tts.model.SBMLSBaseEntity;
import org.tts.model.SBMLSimpleTransition;
import org.tts.model.SBMLSpecies;
import org.tts.model.SBMLSpeciesGroup;
import org.tts.repository.SBMLQualSpeciesRepository;
import org.tts.repository.SBMLSBaseEntityRepository;
import org.tts.repository.SBMLSimpleTransitionRepository;
import org.tts.repository.SBMLSpeciesRepository;

@Service
public class SBMLSimpleFlatModelServiceImpl implements SBMLService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	SBMLSpeciesRepository sbmlSpeciesRepository;
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	
	private List<SBMLCompartment> getCompartmentList(Model model) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
			SBMLCompartment sbmlCompartment = new SBMLCompartment();
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBySBaseId(compartment.getId(), 2);
				if (existingCompartment != null 
						&& existingCompartment.getsBaseName().equals(compartment.getName())
						&& existingCompartment.getSize() == compartment.getSize()
						&& existingCompartment.getSpatialDimensions() == compartment.getSpatialDimensions()
						&& existingCompartment.isConstant() == compartment.getConstant() ) {
					// they are obviously the same, so take the one already persisted
					sbmlCompartment = existingCompartment;
					sbmlCompartmentList.add(sbmlCompartment);
					// other cases would be, that the name and Id are the same, but are actually different compartments
					// with different properties.
					// then append the name of the new compartment with a number (or something) and link all entities in this
					// model to that new one.
					// This means that further down, when looking up compartment, we can no longer match by sbaseId directly
					// but also need a mapping info from the original sbasid (that all entities in jsbml will reference)
					// to the newly given name.
					// but for now, as kegg only has one default compartment and it's the only time we load more than one model
					// we keep it like that.
				} else {
					setSbaseProperties(compartment, sbmlCompartment);
					setCompartmentProperties(compartment, sbmlCompartment);
					sbmlCompartmentList.add(sbmlCompartment);
				}
			} catch (ClassCastException e) {	
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " is not of type SBMLCompartment! Aborting...");
				return null; // need to find better error handling way..
			} catch (IncorrectResultSizeDataAccessException e) {
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " was found multiple times");
				return null; // need to find better error handling way..
			}
		}
		return sbmlCompartmentList;
	}
	
	private void setCompartmentProperties(Compartment compartment, SBMLCompartment sbmlCompartment) {
		// TODO Auto-generated method stub
		
	}

	private void setSbaseProperties(Compartment compartment, SBMLCompartment sbmlCompartment) {
		// TODO Auto-generated method stub
		
	}

	private List<SBMLSpecies> buildAndPersistSBMLSpecies(ListOf<Species> speciesListOf, Map<String, SBMLCompartment> compartmentLookupMap) {
		List<SBMLSpecies> speciesList = new ArrayList<>();
		List<Species> groupSpeciesList = new ArrayList<>();
		for (Species species : speciesListOf) {
			if(species.getName().equals("Group")) {
				logger.debug("found group");
				groupSpeciesList.add(species);
			} else {
				SBMLSpecies existingSpecies = this.sbmlSpeciesRepository.findBySBaseName(species.getName());
				if(existingSpecies != null) {
					speciesList.add(existingSpecies);
				} else {
					SBMLSpecies newSpecies = new SBMLSpecies();
					setSbaseProperties(species, newSpecies);
					//setCompartmentalizedSbaseProperties(species, newSpecies, compartmentLookupMap);
					setSpeciesProperties(species, newSpecies);
					SBMLSpecies persistedNewSpecies = this.sbmlSpeciesRepository.save(newSpecies);
					speciesList.add(persistedNewSpecies);
				}
			}
		}
		// now build the groups nodes
		for (Species species : groupSpeciesList) {
			SBMLSpeciesGroup newSBMLSpeciesGroup = new SBMLSpeciesGroup();
			List<String> groupMemberSymbols = new ArrayList<>();
			XMLNode ulNode = species.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						logger.info(chars);
						groupMemberSymbols.add(chars);
					}
				}
			}
			setSbaseProperties(species, newSBMLSpeciesGroup);
			//setCompartmentalizedSbaseProperties(species, newSBMLSpeciesGroup, compartmentLookupMap);
			setSpeciesProperties(species, newSBMLSpeciesGroup);
			String speciesSbaseName = newSBMLSpeciesGroup.getsBaseName();
			for (String symbol : groupMemberSymbols) {
				SBMLSpecies existingSpecies = this.sbmlSpeciesRepository.findBySBaseName(symbol);
				newSBMLSpeciesGroup.addSpeciesToGroup(existingSpecies);
				speciesSbaseName += "_";
				speciesSbaseName += symbol;
			}
			SBMLSpeciesGroup existingSBMLSpeciesGroup = (SBMLSpeciesGroup) sbmlSpeciesRepository.findBySBaseName(speciesSbaseName);
			if (existingSBMLSpeciesGroup != null) {
				speciesList.add(existingSBMLSpeciesGroup);
			} else {
				newSBMLSpeciesGroup.setsBaseName(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseId(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseMetaId("meta_" + speciesSbaseName);
				SBMLSpecies persistedNewSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup);
				speciesList.add(persistedNewSpeciesGroup);
			}
		}
		return speciesList;
	}
	
	private void setSpeciesProperties(Species species, SBMLSpecies newSpecies) {
		// TODO Auto-generated method stub
		
	}

	private void setSbaseProperties(SBase source, SBMLSBaseEntity target) {
		// TODO Auto-generated method stub
		
	}

	private HelperQualSpeciesReturn buildAndPersistSBMLQualSpecies(ListOf<QualitativeSpecies> qualSpeciesListOf, Map<String, SBMLCompartment> compartmentLookupMap) {
		HelperQualSpeciesReturn helperQualSpeciesReturn = new HelperQualSpeciesReturn();
		Map<String, SBMLQualSpecies> qualSpeciesMap = new HashMap<>();
		List<QualitativeSpecies> groupQualSpeciesList = new ArrayList<>();
		for (QualitativeSpecies qualSpecies : qualSpeciesListOf) {
			if(qualSpecies.getName().equals("Group")) {
				logger.debug("found qual Species group");
				groupQualSpeciesList.add(qualSpecies);// need to do them after all species have been persisted
			} else {
				SBMLQualSpecies existingSpecies = this.sbmlQualSpeciesRepository.findBySBaseName(qualSpecies.getName());
				if (existingSpecies != null) {
					if(!qualSpeciesMap.containsKey(existingSpecies.getsBaseId())) {
						qualSpeciesMap.put(existingSpecies.getsBaseId(), existingSpecies);
					}	
					if(!qualSpecies.getId().substring(5).equals(qualSpecies.getName())) {
						// possibly this is the case when we have an entity duplicated in sbml, but deduplicate it here
						// transitions might reference this duplicate entity and need to be pointed to the original one
						helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), existingSpecies.getsBaseId());
					}
					
				} else {
					SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
					setSbaseProperties(qualSpecies, newQualSpecies);
					//setCompartmentalizedSbaseProperties(qualSpecies, newQualSpecies, compartmentLookupMap);
					setQualSpeciesProperties(qualSpecies, newQualSpecies);
					newQualSpecies.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpecies.getName()));
					SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies);
					qualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
				}
			}
		}
		// now build the group nodes
		for (QualitativeSpecies qualSpecies : groupQualSpeciesList) {
			if (qualSpecies.getId().equals("qual_Group_4")) {
				logger.debug("Group 4");
			}
			SBMLQualSpeciesGroup newSBMLQualSpeciesGroup = new SBMLQualSpeciesGroup();
			List<String> groupMemberSymbols = new ArrayList<>();
			XMLNode ulNode = qualSpecies.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						logger.info(chars);
						groupMemberSymbols.add(chars);
					}
				}
			}
			setSbaseProperties(qualSpecies, newSBMLQualSpeciesGroup);
			//setCompartmentalizedSbaseProperties(qualSpecies, newSBMLQualSpeciesGroup, compartmentLookupMap);
			setQualSpeciesProperties(qualSpecies, newSBMLQualSpeciesGroup);
			String qualSpeciesSbaseName = newSBMLQualSpeciesGroup.getsBaseName();
			for (String symbol : groupMemberSymbols) {
				SBMLQualSpecies existingQualSpecies = this.sbmlQualSpeciesRepository.findBySBaseName(symbol);
				newSBMLQualSpeciesGroup.addQualSpeciesToGroup(existingQualSpecies);
				qualSpeciesSbaseName += "_";
				qualSpeciesSbaseName += symbol;
			}
			SBMLQualSpeciesGroup existingSBMLQualSpeciesGroup = (SBMLQualSpeciesGroup) sbmlQualSpeciesRepository.findBySBaseName(qualSpeciesSbaseName);
			if (existingSBMLQualSpeciesGroup != null) {
				if (!qualSpeciesMap.containsKey(existingSBMLQualSpeciesGroup.getsBaseId())) {
					qualSpeciesMap.put(existingSBMLQualSpeciesGroup.getsBaseId(), existingSBMLQualSpeciesGroup);
				}
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
			} else {
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseName(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseId(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseMetaId("meta_" + qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpeciesSbaseName));
				SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup);
				qualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
			}
		}
		helperQualSpeciesReturn.setSpeciesMap(qualSpeciesMap);
		if(helperQualSpeciesReturn.getsBaseIdMap() == null) {
			Map<String, String> emptySBaseIdMap = new HashMap<>();
			helperQualSpeciesReturn.setsBaseIdMap(emptySBaseIdMap);
		}
		return helperQualSpeciesReturn;
	}
	
	private void setQualSpeciesProperties(QualitativeSpecies source,
			SBMLQualSpecies target) {
		// TODO Auto-generated method stub
		
	}

	private List<SBMLSimpleTransition> buildAndPersistTransitions(Map<String, String> qualSBaseLookupMap,
			 ListOf<Transition> transitionListOf) {
		List<SBMLSimpleTransition> transitionList = new ArrayList<>();
		for (Transition transition : transitionListOf) {
			SBMLQualSpecies tmp = null;
			String newTransitionId = "";
			if(qualSBaseLookupMap.containsKey(transition.getListOfInputs().get(0).getQualitativeSpecies())) {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies()));
			} else {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(transition.getListOfInputs().get(0).getQualitativeSpecies());
			}
			if(tmp == null) {
				logger.debug("What is going on?");
			}
			newTransitionId += tmp.getsBaseName();
			newTransitionId += "-";
			newTransitionId += transition.getSBOTermID();
			newTransitionId += "-";
			if(qualSBaseLookupMap.containsKey(transition.getListOfOutputs().get(0).getQualitativeSpecies())) {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(qualSBaseLookupMap.get(transition.getListOfOutputs().get(0).getQualitativeSpecies()));
			} else {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(transition.getListOfOutputs().get(0).getQualitativeSpecies());
			}
			if (tmp != null) {
				newTransitionId += (tmp).getsBaseName();
			} else {
				logger.debug("Species is null");
			}
			
			SBMLSimpleTransition existingSimpleTransition = this.sbmlSimpleTransitionRepository.getByTransitionId(newTransitionId, 2);
			String inputName = null;
			String outputName = null;
			if(qualSBaseLookupMap.containsKey(transition.getListOfInputs().get(0).getQualitativeSpecies())) {
				inputName = qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies());
			} else {
				inputName = transition.getListOfInputs().get(0).getQualitativeSpecies();
			}
			if(qualSBaseLookupMap.containsKey(transition.getListOfOutputs().get(0).getQualitativeSpecies())) {
				outputName = qualSBaseLookupMap.get(transition.getListOfOutputs().get(0).getQualitativeSpecies());
			} else {
				outputName = transition.getListOfOutputs().get(0).getQualitativeSpecies();
			}
			if(existingSimpleTransition != null ) {
				if(existingSimpleTransition.getInputSpecies() == null || existingSimpleTransition.getOutputSpecies() == null) {
					logger.debug("How?");
				}
				if(existingSimpleTransition.getInputSpecies().getsBaseName().equals(inputName) &&
						existingSimpleTransition.getOutputSpecies().getsBaseName().equals(outputName)) {
					transitionList.add(existingSimpleTransition);
				}
			} else {
				SBMLSimpleTransition newSimpleTransition = new SBMLSimpleTransition();
				setSbaseProperties(transition, newSimpleTransition);
				newSimpleTransition.setTransitionId(newTransitionId);
				newSimpleTransition.setInputSpecies(this.sbmlQualSpeciesRepository.findBySBaseId(inputName));
				newSimpleTransition.setOutputSpecies(this.sbmlQualSpeciesRepository.findBySBaseId(outputName));
				SBMLSimpleTransition persistedNewSimpleTransition = this.sbmlSimpleTransitionRepository.save(newSimpleTransition);
				transitionList.add(persistedNewSimpleTransition);
			}
		}
		return transitionList;
	}
	
	
	@Override
	public boolean isValidSBML(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSBMLVersionCompatible(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Iterable<Object>> extractAndConnectExternalResources(
			Map<String, Iterable<SBMLSBaseEntity>> allEntities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GraphBaseEntity> buildAndPersist(Model model, String filename) {
		// compartment
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model);
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getsBaseId(), compartment);
		}
		List<SBMLSpecies> persistedSBMLSpecies = buildAndPersistSBMLSpecies(model.getListOfSpecies(), compartmentLookupMap);

		QualModelPlugin qualModelPlugin = (QualModelPlugin) model.getExtension("qual");
		HelperQualSpeciesReturn qualSpeciesHelper = buildAndPersistSBMLQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
		
		Map<String, SBMLQualSpecies> persistedQualSpeciesList = qualSpeciesHelper.getSpeciesMap();
		Map<String, String> qualSBaseLookupMap = qualSpeciesHelper.getsBaseIdMap();
		List<SBMLSimpleTransition> persistedTransitionList	= buildAndPersistTransitions(qualSBaseLookupMap, qualModelPlugin.getListOfTransitions());
		
		List<GraphBaseEntity> returnList= new ArrayList<>();
		persistedSBMLSpecies.forEach(species->{
			returnList.add(species);
		});
		persistedQualSpeciesList.forEach((k, qualSpecies)-> {
			returnList.add(qualSpecies);
		});
		persistedTransitionList.forEach(transition-> {
			returnList.add(transition);
		});
		return returnList;
	}

	

	@Override
	public boolean clearDatabase() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<GraphBaseEntity> getAllEntities() {
		// TODO Auto-generated method stub
		return null;
	}

}