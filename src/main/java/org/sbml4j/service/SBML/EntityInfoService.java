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
package org.sbml4j.service.SBML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.api.entityInfo.EntityInfoItem;
import org.sbml4j.model.api.entityInfo.IdItem;
import org.sbml4j.model.api.entityInfo.PathwayInfoItem;
import org.sbml4j.model.api.entityInfo.QualifierItem;
import org.sbml4j.model.api.entityInfo.QualifierItemContent;
import org.sbml4j.model.api.entityInfo.QualifierItemValue;
import org.sbml4j.model.api.entityInfo.ReactionInfoItem;
import org.sbml4j.model.api.entityInfo.ReactionPartnerItem;
import org.sbml4j.model.api.entityInfo.RelationInfoItem;
import org.sbml4j.model.api.entityInfo.RelationInfoItem.DirectionEnum;
import org.sbml4j.model.base.GraphEnum.ExternalResourceType;
import org.sbml4j.model.sbml.SBMLSpecies;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpecies;
import org.sbml4j.model.sbml.ext.sbml4j.BiomodelsQualifier;
import org.sbml4j.model.sbml.ext.sbml4j.NameNode;
import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.sbml4j.model.sbml.simple.ext.qual.SBMLSimpleTransition;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.repository.sbml.ext.sbml4j.BiomodelsQualifierRepository;
import org.sbml4j.service.NameNodeService;
import org.sbml4j.service.PathwayService;
import org.sbml4j.service.UtilityService;
import org.sbml4j.service.SimpleSBML.SBMLSimpleReactionService;
import org.sbml4j.service.SimpleSBML.SBMLSimpleTransitionService;
import org.sbml4j.service.SimpleSBML.SBMLSpeciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class EntityInfoService {

	@Autowired
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	
	@Autowired
	NameNodeService nameNodeService;
	
	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	SBMLSimpleReactionService sbmlSimpleReactionService;
	
	@Autowired
	SBMLSimpleTransitionService sbmlSimpleTransitionService;
	
	@Autowired
	SBMLSpeciesService sbmlSpeciesService;
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	@Autowired
	UtilityService utilityService;
	
	public List<EntityInfoItem> batchGetEntityInfo(List<String> geneSymbols) {
		List<EntityInfoItem> batchEntityInfoItems = new ArrayList<>();
		for (String geneSymbol : geneSymbols) {
			List<EntityInfoItem> symbolItems = this.getEntityInfo(geneSymbol);
			if (!symbolItems.isEmpty()) {
				batchEntityInfoItems.addAll(symbolItems);
			}
		}
		return batchEntityInfoItems;
	}
	
	
	public List<EntityInfoItem> getEntityInfo(String geneSymbol) {
		
		List<EntityInfoItem> entityInfoItems = new ArrayList<>();
		Map<String, SBMLSpecies> geneSymbolSpecies = this.sbmlSpeciesService.findAllBySymbol(geneSymbol);
		if(geneSymbolSpecies.isEmpty()) {
			geneSymbolSpecies = this.sbmlSpeciesService.findAllBySymbol(geneSymbol, true);
		}
		if (geneSymbolSpecies.isEmpty()) {
			geneSymbolSpecies = this.sbmlSpeciesService.findByExternalResourceSecondaryName(geneSymbol);
		}
		
		Map<String, ReactionInfoItem> reactionMap = new HashMap<>();
		Map<String, QualifierItem> qualifierMap = new HashMap<>();
		
		
		
		if (geneSymbolSpecies.isEmpty()) {
			EntityInfoItem emptyItem = new EntityInfoItem().gene(geneSymbol).pathways(new ArrayList<>()).qualifier(new ArrayList<>()).reactions(new ArrayList<>()).relations(new ArrayList<>()).secondaryNames(new ArrayList<>());
			entityInfoItems.add(emptyItem);
			return entityInfoItems;
		} else {
			EntityInfoItem item = new EntityInfoItem();
			for (SBMLSpecies species : geneSymbolSpecies.values()) {
				
				if (item.getGene() == null) {
					item.setGene(species.getsBaseName());
				} else {
					item.addSecondaryNamesItem(species.getsBaseName());
				}
				
				List<PathwayInfoItem> pathwayInfoItems;
				if (item.getPathways() == null) {
					pathwayInfoItems = new ArrayList<>();
				} else {
					pathwayInfoItems = item.getPathways();
				}
				List<PathwayNode> pathways = this.pathwayService.getPathwayNodesOfSBase(UUID.fromString(species.getEntityUUID()));
				for (PathwayNode pathway : pathways) {
					PathwayInfoItem infoItem = new PathwayInfoItem();
					infoItem.setName(pathway.getPathwayNameString());
					infoItem.setKeGGId(pathway.getPathwayIdString());
					String keGGUrlString = this.sbml4jConfig.getExternalResourcesProperties().getKeggDatabaseProperties().getPathwayLinkPrefix();
					try {
						keGGUrlString += pathway.getPathwayIdString().split("_")[1];
					} catch (Exception e) {
						// apparently we could not split the pathwayIdString and take the second element (from i.e. path_hsa05216); probably not dealing with a Kegg pathway
						try {
							keGGUrlString = this.sbml4jConfig.getExternalResourcesProperties().getKeggDatabaseProperties().getPathwaySearchPrefix() +
									pathway.getPathwayNameString().split("(")[0].trim().replaceAll(" ", "+");
						} catch (Exception e2) {
							keGGUrlString = this.sbml4jConfig.getExternalResourcesProperties().getKeggDatabaseProperties().getPathwaySearchPrefix() +
									pathway.getPathwayNameString().replaceAll(" ", "+");
						}
					}
					infoItem.setKeGGUrl(keGGUrlString);
					pathwayInfoItems.add(infoItem);
				}
				item.setPathways(pathwayInfoItems);
				List<BiomodelsQualifier> biomodelsQualifierOfSpecies = this.biomodelsQualifierRepository.findAllForSBMLSpecies(species.getEntityUUID());
				
				
				for (BiomodelsQualifier qualifier : biomodelsQualifierOfSpecies) {
					String uri = qualifier.getEndNode().getUri();
					String qualifierType = qualifier.getQualifier().toString();
					
					String qualifierItemName = null;
					if (uri.contains("identifiers.org")) {
						String[] splitted = uri.split("/");
						qualifierItemName = splitted[splitted.length-2].replace('.', '_');
						//currentQualifierMap.computeIfAbsent(splitted[splitted.length-2].replace('.', '_'), k -> new ArrayList<String>()).add(uri);
						if (qualifier.getEndNode().getType() != null 
								&& qualifier.getEndNode().getType().equals(ExternalResourceType.KEGGGENES) ) {
							Iterable<NameNode> secondaryNameNodes = this.nameNodeService.findAllByExternalResource(qualifier.getEndNode().getEntityUUID());
							if (secondaryNameNodes != null) {
								for (NameNode secondaryNameNode : secondaryNameNodes) {
									item.addSecondaryNamesItem(secondaryNameNode.getName());
								}
							}
						}
					} else if (qualifier.getEndNode().getType().equals(ExternalResourceType.MDANDERSON)) { // uri.contains("mdanderson")) {
						qualifierItemName = "mdanderson";
						//currentQualifierMap.computeIfAbsent("mdanderson", k -> new ArrayList<>()).add(uri);
					}
					// get the value item for the current uri (which then includes a potential extracted id)
					QualifierItemValue value = new QualifierItemValue();
					value.setUrl(uri);
					value.setIdentifier(qualifier.getEndNode().getShortIdentifierFromUri());
					
					if (qualifierMap.containsKey(qualifierItemName)) {
						List<QualifierItemContent> qualifierItemContentList = qualifierMap.get(qualifierItemName).getContent();
						// iterate through all known qualifier for e.g. entrez gene, so BQB_HAS_VERSION, BQB_IS_DESCRIBED_BY
						boolean hasQualifierType = false;
						
						for (QualifierItemContent qualifierItemContent : qualifierItemContentList) {
							if (qualifierItemContent.getType().equals(qualifierType)) {
								hasQualifierType = true;
								
								qualifierItemContent.addValuesItem(value); //addUrlItem(uri);
							}
						}
						if (!hasQualifierType) {
							qualifierItemContentList.add(new QualifierItemContent().type(qualifierType).addValuesItem(value));
						}
					} else {
						qualifierMap.put(qualifierItemName, new QualifierItem().name(qualifierItemName).addContentItem(new QualifierItemContent().type(qualifierType).addValuesItem(value)));
					}
				}
				
				
				// find all transitions this species is part of
				for (PathwayNode pathway : pathways) {
					Iterable<SBMLSimpleTransition> transitionsOfPathway = this.sbmlSimpleTransitionService.findAllTransitionsInPathway(pathway.getEntityUUID());
					for (SBMLSimpleTransition transition : transitionsOfPathway) {
						for(SBMLQualSpecies inputSpecies : transition.getInputSpecies()) {
							if (inputSpecies.getsBaseName().equals(species.getsBaseName())) {
								for (SBMLQualSpecies outputSpecies : transition.getOutputSpecies()) {
									// we have a match
									RelationInfoItem relItem = new RelationInfoItem();
									relItem.direction(DirectionEnum.OUT)
										.sboTerm(transition.getsBaseSboTerm())
										.type(this.utilityService.translateSBOString(transition.getsBaseSboTerm()))
										.name(outputSpecies.getsBaseName());
									item.addRelationsItem(relItem);
								}
							}
						}
						for(SBMLQualSpecies outputSpecies : transition.getOutputSpecies()) {
							if (outputSpecies.getsBaseName().equals(species.getsBaseName())) {
								for (SBMLQualSpecies inputSpecies : transition.getInputSpecies()) {
									// we have a match
									RelationInfoItem relItem = new RelationInfoItem();
									relItem.direction(DirectionEnum.IN)
										.sboTerm(transition.getsBaseSboTerm())
										.type(this.utilityService.translateSBOString(transition.getsBaseSboTerm()))
										.name(inputSpecies.getsBaseName());
									item.addRelationsItem(relItem);
								}
							}
						}
					}
				}
				
				/**for (NonMetabolicPathwayReturnType transition : this.sbmlSimpleTransitionService.getTransitionsForSpecies(species.getEntityUUID())) {
					RelationInfoItem relItem = new RelationInfoItem();
					relItem.direction(transition.getInputSpecies().getEntityUUID().equals(species.getEntityUUID()) ? DirectionEnum.OUT : DirectionEnum.IN);
					relItem.sboTerm(transition.getTransition().getsBaseSboTerm());
					relItem.setType(this.utilityService.translateSBOString(transition.getTransition().getsBaseSboTerm()));
					relItem.setName(transition.getInputSpecies().getEntityUUID().equals(species.getEntityUUID())? transition.getOutputSpecies().getsBaseName() : transition.getInputSpecies().getsBaseName());
					item.addRelationsItem(relItem);
				}*/
				
			
				// find all reactions this species is part of
				for (SBMLSimpleReaction reaction : this.sbmlSimpleReactionService.findAllReactionsForSpecies(species.getEntityUUID())) {
					ReactionInfoItem reactionInfoItem;
					String reactionSBaseName = reaction.getsBaseName();
					if (!reactionMap.containsKey(reactionSBaseName)) {
						reactionInfoItem = new ReactionInfoItem().
							name(reactionSBaseName).
							sboTerm(reaction.getsBaseSboTerm()).
							type(this.utilityService.translateSBOString(reaction.getsBaseSboTerm())).
							reversible(reaction.isReversible());
						
						
						for (SBMLSpecies reactant : this.sbmlSimpleReactionService.findAllReactionPartnersOfType(reaction.getEntityUUID(), "IS_REACTANT")) {
							reactionInfoItem.addReactantsItem(
									new ReactionPartnerItem()
										.name(reactant.getsBaseName())
										.sboTerm(reactant.getsBaseSboTerm())
										.type(this.utilityService.translateSBOString(reactant.getsBaseSboTerm()))
									);
						}
						for (SBMLSpecies product : this.sbmlSimpleReactionService.findAllReactionPartnersOfType(reaction.getEntityUUID(), "IS_PRODUCT")) {
							reactionInfoItem.addProductsItem(
									new ReactionPartnerItem()
										.name(product.getsBaseName())
										.sboTerm(product.getsBaseSboTerm())
										.type(this.utilityService.translateSBOString(product.getsBaseSboTerm()))
									);
						}
						for (SBMLSpecies catalyst : this.sbmlSimpleReactionService.findAllReactionPartnersOfType(reaction.getEntityUUID(), "IS_CATALYST")) {
							reactionInfoItem.addCatalystsItem(
									new ReactionPartnerItem()
										.name(catalyst.getsBaseName())
										.sboTerm(catalyst.getsBaseSboTerm())
										.type(this.utilityService.translateSBOString(catalyst.getsBaseSboTerm()))
									);
						}
						reactionMap.put(reactionInfoItem.getName(), reactionInfoItem);
					}
				}
			}
		
			if (item.getRelations() == null) {
				item.setRelations(new ArrayList<>());
			}
			
			// add all collected qualifier to the item
			for (QualifierItem qualifierItemToAdd : qualifierMap.values()) {
				item.addQualifierItem(qualifierItemToAdd);
			}
			if (item.getQualifier() == null) {
				item.setQualifier(new ArrayList<>());
			}
			// add all collected ReactionInfoItems
			for (ReactionInfoItem reactionInfoItemToAdd : reactionMap.values()) {
				item.addReactionsItem(reactionInfoItemToAdd);
			}
			if (item.getReactions() == null) {
				item.setReactions(new ArrayList<>());
			}
			
			entityInfoItems.add(item);
		
		}
		return entityInfoItems;
	}


	public List<IdItem> getIdMap(String geneSymbol,String idSystem) {
		
		
		
		Map<String, QualifierItem> qualifierMap = new HashMap<>();
		List<IdItem> idItemList = new ArrayList<>();
		
		Map<String, SBMLSpecies> geneSymbolSpecies = this.sbmlSpeciesService.findAllBySymbol(geneSymbol);
		if(geneSymbolSpecies.isEmpty()) {
			geneSymbolSpecies = this.sbmlSpeciesService.findAllBySymbol(geneSymbol, true);
		}
		if (geneSymbolSpecies.isEmpty()) {
			geneSymbolSpecies = this.sbmlSpeciesService.findByExternalResourceSecondaryName(geneSymbol);
		}
		if (geneSymbolSpecies.isEmpty()) {
			return idItemList;
		} else {
			IdItem item = new IdItem();
			for (SBMLSpecies species : geneSymbolSpecies.values()) {
				
				if (item.getGene() == null) {
					item.setGene(species.getsBaseName());
				} else {
					item.addSecondaryNamesItem(species.getsBaseName());
				}
				
				List<BiomodelsQualifier> biomodelsQualifierOfSpecies = this.biomodelsQualifierRepository.findAllForSBMLSpecies(species.getEntityUUID());
				
				
				for (BiomodelsQualifier qualifier : biomodelsQualifierOfSpecies) {
					String uri = qualifier.getEndNode().getUri();
					String qualifierType = qualifier.getQualifier().toString();
					
					String qualifierItemName = null;
					if (uri.contains("identifiers.org")) {
						String[] splitted = uri.split("/");
						qualifierItemName = splitted[splitted.length-2].replace('.', '_');
						//currentQualifierMap.computeIfAbsent(splitted[splitted.length-2].replace('.', '_'), k -> new ArrayList<String>()).add(uri);
						if (qualifier.getEndNode().getType() != null 
								&& qualifier.getEndNode().getType().equals(ExternalResourceType.KEGGGENES) ) {
							Iterable<NameNode> secondaryNameNodes = this.nameNodeService.findAllByExternalResource(qualifier.getEndNode().getEntityUUID());
							if (secondaryNameNodes != null) {
								for (NameNode secondaryNameNode : secondaryNameNodes) {
									item.addSecondaryNamesItem(secondaryNameNode.getName());
								}
							}
						}
					} else if (qualifier.getEndNode().getType().equals(ExternalResourceType.MDANDERSON)) { // uri.contains("mdanderson")) {
						qualifierItemName = "mdanderson";
						//currentQualifierMap.computeIfAbsent("mdanderson", k -> new ArrayList<>()).add(uri);
					}
					// get the value item for the current uri (which then includes a potential extracted id)
					QualifierItemValue value = new QualifierItemValue();
					value.setUrl(uri);
					value.setIdentifier(qualifier.getEndNode().getShortIdentifierFromUri());
					
					if (qualifierMap.containsKey(qualifierItemName)) {
						List<QualifierItemContent> qualifierItemContentList = qualifierMap.get(qualifierItemName).getContent();
						// iterate through all known qualifier for e.g. entrez gene, so BQB_HAS_VERSION, BQB_IS_DESCRIBED_BY
						boolean hasQualifierType = false;
						
						for (QualifierItemContent qualifierItemContent : qualifierItemContentList) {
							if (qualifierItemContent.getType().equals(qualifierType)) {
								hasQualifierType = true;
								
								qualifierItemContent.addValuesItem(value); //addUrlItem(uri);
							}
						}
						if (!hasQualifierType) {
							qualifierItemContentList.add(new QualifierItemContent().type(qualifierType).addValuesItem(value));
						}
					} else {
						qualifierMap.put(qualifierItemName, new QualifierItem().name(qualifierItemName).addContentItem(new QualifierItemContent().type(qualifierType).addValuesItem(value)));
					}
				}
			}
			for (QualifierItem qualifierItemToAdd : qualifierMap.values()) {
				item.addQualifierItem(qualifierItemToAdd);
			}
			idItemList.add(item);
			return idItemList;
		}
	}
	
}
