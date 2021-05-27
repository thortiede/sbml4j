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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.api.GeneAnalysisItem;
import org.sbml4j.model.api.PathwayInfoItem;
import org.sbml4j.model.api.QualifierItem;
import org.sbml4j.model.api.QualifierItemContent;
import org.sbml4j.model.api.ReactionInfoItem;
import org.sbml4j.model.api.ReactionPartnerItem;
import org.sbml4j.model.api.RelationInfoItem;
import org.sbml4j.model.api.Output.MetabolicPathwayReturnType;
import org.sbml4j.model.api.Output.NonMetabolicPathwayReturnType;
import org.sbml4j.model.api.RelationInfoItem.DirectionEnum;
import org.sbml4j.model.common.BiomodelsQualifier;
import org.sbml4j.model.common.SBMLSpecies;
import org.sbml4j.model.common.GraphEnum.ExternalResourceType;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.repository.common.BiomodelsQualifierRepository;
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
public class AnalysisService {

	@Autowired
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	
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
	
	public List<GeneAnalysisItem> getGeneAnalysis(String geneSymbol) {
		
		List<GeneAnalysisItem> geneAnalysisItems = new ArrayList<>();
		Map<String, SBMLSpecies> geneSymbolSpecies = this.sbmlSpeciesService.findAllBySymbol(geneSymbol);
		if(geneSymbolSpecies.isEmpty()) {
			geneSymbolSpecies = this.sbmlSpeciesService.findAllBySymbol(geneSymbol, true);
		}
		if (geneSymbolSpecies.isEmpty()) {
			geneSymbolSpecies = this.sbmlSpeciesService.findByExternalResourceSecondaryName(geneSymbol);
		}
		for (SBMLSpecies species : geneSymbolSpecies.values()) {
			GeneAnalysisItem item = new GeneAnalysisItem();
			item.setGene(species.getsBaseName());
			List<PathwayInfoItem> pathwayInfoItems = new ArrayList<>();
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
			Map<String, QualifierItem> qualifierMap = new HashMap<>();
			
			for (BiomodelsQualifier qualifier : biomodelsQualifierOfSpecies) {
				String uri = qualifier.getEndNode().getUri();
				String qualifierType = qualifier.getQualifier().toString();
				
				String qualifierItemName = null;
				if (uri.contains("identifiers.org")) {
					String[] splitted = uri.split("/");
					qualifierItemName = splitted[splitted.length-2].replace('.', '_');
					//currentQualifierMap.computeIfAbsent(splitted[splitted.length-2].replace('.', '_'), k -> new ArrayList<String>()).add(uri);
					if (qualifier.getEndNode().getType() != null 
							&& qualifier.getEndNode().getType().equals(ExternalResourceType.KEGGGENES) 
							&& qualifier.getEndNode().getSecondaryNames() != null) {
						for (String secondaryName : qualifier.getEndNode().getSecondaryNames()) {
							item.addSecondaryNamesItem(secondaryName);
						}
					}
				} else if (qualifier.getEndNode().getType().equals(ExternalResourceType.MDANDERSON)) { // uri.contains("mdanderson")) {
					qualifierItemName = "mdanderson";
					//currentQualifierMap.computeIfAbsent("mdanderson", k -> new ArrayList<>()).add(uri);
				}
				// Top Level for the Qualifier Info (i.e. entrez-gene)
				
				if (qualifierMap.containsKey(qualifierItemName)) {
					List<QualifierItemContent> qualifierItemContentList = qualifierMap.get(qualifierItemName).getContent();
					// iterate through all known qualifier for e.g. entrez gene, so BQB_HAS_VERSION, BQB_IS_DESCRIBED_BY
					boolean hasQualifierType = false;
					for (QualifierItemContent qualifierItemContent : qualifierItemContentList) {
						if (qualifierItemContent.getType().equals(qualifierType)) {
							hasQualifierType = true;
							qualifierItemContent.addUrlItem(uri);
						}
					}
					if (!hasQualifierType) {
						qualifierItemContentList.add(new QualifierItemContent().type(qualifierType).addUrlItem(uri));
					}
				} else {
					qualifierMap.put(qualifierItemName, new QualifierItem().name(qualifierItemName).addContentItem(new QualifierItemContent().type(qualifierType).addUrlItem(uri)));
				}
			}
			for (QualifierItem qualifierItemToAdd : qualifierMap.values()) {
				item.addQualifierItem(qualifierItemToAdd);
			}
			if (item.getQualifier() == null) {
				item.setQualifier(new ArrayList<>());
			}
			
			// find all transitions this species is part of
			for (NonMetabolicPathwayReturnType transition : this.sbmlSimpleTransitionService.getTransitionsForSpecies(species.getEntityUUID())) {
				RelationInfoItem relItem = new RelationInfoItem();
				relItem.direction(transition.getInputSpecies().getEntityUUID().equals(species.getEntityUUID()) ? DirectionEnum.OUT : DirectionEnum.IN);
				relItem.sboTerm(transition.getTransition().getsBaseSboTerm());
				relItem.setType(this.utilityService.translateSBOString(transition.getTransition().getsBaseSboTerm()));
				relItem.setName(transition.getInputSpecies().getEntityUUID().equals(species.getEntityUUID())? transition.getOutputSpecies().getsBaseName() : transition.getInputSpecies().getsBaseName());
				item.addRelationsItem(relItem);
			}
			if (item.getRelations() == null) {
				item.setRelations(new ArrayList<>());
			}
			
			// find all reactions this species is part of
			Map<String, ReactionInfoItem> reactionMap = new HashMap<>();
			for (MetabolicPathwayReturnType reaction : this.sbmlSimpleReactionService.findAllReactionsForSpecies(species.getEntityUUID())) {
				for (MetabolicPathwayReturnType speciesOfReaction : this.sbmlSimpleReactionService.findAllSpeciesForReaction(reaction.getReaction().getEntityUUID())) {
					ReactionInfoItem reactionInfoItem = null;
					if (reactionMap.containsKey(speciesOfReaction.getReaction().getsBaseName())) {
						reactionInfoItem = reactionMap.get(speciesOfReaction.getReaction().getsBaseName());
					} else {
						reactionInfoItem = new ReactionInfoItem().
							name(speciesOfReaction.getReaction().getsBaseName()).
							sboTerm(speciesOfReaction.getReaction().getsBaseSboTerm()).
							type(this.utilityService.translateSBOString(speciesOfReaction.getReaction().getsBaseSboTerm())).
							reversible(speciesOfReaction.getReaction().isReversible());
					}
					switch (speciesOfReaction.getTypeOfRelation()) {
					case "IS_REACTANT":
						reactionInfoItem.addReactantsItem(
								new ReactionPartnerItem()
									.name(speciesOfReaction.getSpecies().getsBaseName())
									.sboTerm(speciesOfReaction.getSpecies().getsBaseSboTerm())
									.type(this.utilityService.translateSBOString(speciesOfReaction.getSpecies().getsBaseSboTerm()))
								);
						break;
					case "IS_PRODUCT":
						reactionInfoItem.addProductsItem(
								new ReactionPartnerItem()
									.name(speciesOfReaction.getSpecies().getsBaseName())
									.sboTerm(speciesOfReaction.getSpecies().getsBaseSboTerm())
									.type(this.utilityService.translateSBOString(speciesOfReaction.getSpecies().getsBaseSboTerm()))
								);
						break;
					case "IS_CATALYST":
						reactionInfoItem.addCatalystsItem(
								new ReactionPartnerItem()
									.name(speciesOfReaction.getSpecies().getsBaseName())
									.sboTerm(speciesOfReaction.getSpecies().getsBaseSboTerm())
									.type(this.utilityService.translateSBOString(speciesOfReaction.getSpecies().getsBaseSboTerm()))
								);
						break;
					}
					reactionMap.put(reactionInfoItem.getName(), reactionInfoItem);
				}
			}
			for (ReactionInfoItem reactionInfoItemToAdd : reactionMap.values()) {
				item.addReactionsItem(reactionInfoItemToAdd);
			}
			if (item.getReactions() == null) {
				item.setReactions(new ArrayList<>());
			}
			
			geneAnalysisItems.add(item);
		}
		
		
		return geneAnalysisItems;
	}
	
}
