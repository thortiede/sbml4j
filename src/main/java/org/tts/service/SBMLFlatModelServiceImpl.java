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
import org.tts.model.flat.FlatSpecies;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.HelperQualSpeciesReturn;
import org.tts.model.common.SBMLCompartment;
import org.tts.model.common.SBMLFile;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLQualSpeciesGroup;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpeciesGroup;
import org.tts.repository.common.SBMLQualSpeciesRepository;
import org.tts.repository.common.SBMLSBaseEntityRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.tts.repository.simpleModel.SBMLSimpleTransitionRepository;

@Service
@Deprecated
public class SBMLFlatModelServiceImpl implements SBMLService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	FlatSpeciesRepository flatSpeciesRepository;
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	
	/**
	 * This actually gets duplicated in all different SBML-xx-ServiceImplementations.
	 * At some point this should be consolidated into one common class.
	 * But as it accesses repositories it needs to be kept different for each of them.
	 * Theoretically the compartment is different in each of those models, or not present in some models
	 * i.e. in the flat model it probably is not necessary
	 * @param model
	 * @return
	 */
	private List<SBMLCompartment> getCompartmentList(Model model) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
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
					sbmlCompartmentList.add(existingCompartment);
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
					SBMLCompartment sbmlCompartment = new SBMLCompartment();
					setSbaseProperties(compartment, sbmlCompartment);
					setCompartmentProperties(compartment, sbmlCompartment);
					sbmlCompartmentList.add((SBMLCompartment) this.sbmlSBaseEntityRepository.save(sbmlCompartment));
				}
			} catch (ClassCastException e) {	
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " is not of type SBMLCompartment! Aborting...");
				return null; // need to find better error handling way..
			} catch (IncorrectResultSizeDataAccessException e) {
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " was found multiple times");
				return null; // need to find better error handling way..
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception " + e.getMessage() + " while persisting SBMLCompartment " + compartment.getId());
				return null;
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

	/**
	 * Creates 
	 * @param speciesListOf
	 * @param compartmentLookupMap
	 * @return
	 */
	private List<FlatSpecies> buildAndPersistSBMLSpecies(ListOf<Species> speciesListOf, Map<String, SBMLCompartment> compartmentLookupMap) {
		List<FlatSpecies> speciesList = new ArrayList<>();
		List<Species> groupSpeciesList = new ArrayList<>();
		for (Species species : speciesListOf) {
			if(species.getName().equals("Group")) {
				logger.debug("found group");
				groupSpeciesList.add(species);
			} else {
				/*FlatSpecies existingSpecies = this.flatSpeciesRepository.findBySBaseName(species.getName());
				if(existingSpecies != null) {
					speciesList.add(existingSpecies);
				} else {
					FlatSpecies newSpecies = new FlatSpecies();
					// WARNING: TODO: The next line is broken and is not supposed to be like that, if this file is ever going to be used
					// FlatSpecies ist not derived from SBMLCompartmentalizedSBase anymore, hence the following call will not work.
					// Flat Species needs to have set GraphBaseEntityProperties however.
					//setSbaseProperties(species, newSpecies);
					//setCompartmentalizedSbaseProperties(species, newSpecies, compartmentLookupMap);
					//  TODO: setSpeciesProperties(species, newSpecies);
					FlatSpecies persistedNewSpecies = this.flatSpeciesRepository.save(newSpecies);
					speciesList.add(persistedNewSpecies);
				}*/
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
			/*for (String symbol : groupMemberSymbols) {
				FlatSpecies existingSpecies = this.flatSpeciesRepository.findBySBaseName(symbol);
				// TODO: newSBMLSpeciesGroup.addSpeciesToGroup(existingSpecies);
				speciesSbaseName += "_";
				speciesSbaseName += symbol;
			}*/
			SBMLSpeciesGroup existingSBMLSpeciesGroup = null;// TODO:  = (SBMLSpeciesGroup) sbmlSpeciesRepository.findBySBaseName(speciesSbaseName);
			if (existingSBMLSpeciesGroup != null) {
				//  TODO: speciesList.add(existingSBMLSpeciesGroup);
			} else {
				newSBMLSpeciesGroup.setsBaseName(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseId(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseMetaId("meta_" + speciesSbaseName);
				FlatSpecies persistedNewSpeciesGroup ;// TODO: = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup);
				//  TODO: speciesList.add(persistedNewSpeciesGroup);
			}
		}
		return speciesList;
	}
	
	private void setSpeciesProperties(Species species, SBMLSpeciesGroup newSBMLSpeciesGroup) {
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
					//  TODO: newQualSpecies.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpecies.getName()));
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
				//  TODO: newSBMLQualSpeciesGroup.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpeciesSbaseName));
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
		List<FlatSpecies> persistedSBMLSpecies = buildAndPersistSBMLSpecies(model.getListOfSpecies(), compartmentLookupMap);

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

	@Override
	public boolean sbmlFileNodeExists(String originalFilename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SBMLFile createSbmlFileNode(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBMLFile getSbmlFileNode(String originalFilename) {
		// TODO Auto-generated method stub
		return null;
	}

}
