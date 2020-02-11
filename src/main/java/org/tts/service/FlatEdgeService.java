package org.tts.service;


import org.springframework.stereotype.Service;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.relationship.CatalystFlatEdge;
import org.tts.model.flat.relationship.ControlFlatEdge;
import org.tts.model.flat.relationship.DephosphorylationFlatEdge;
import org.tts.model.flat.relationship.DissociationFlatEdge;
import org.tts.model.flat.relationship.GlycosylationFlatEdge;
import org.tts.model.flat.relationship.InhibitionFlatEdge;
import org.tts.model.flat.relationship.MethylationFlatEdge;
import org.tts.model.flat.relationship.MolecularInteractionFlatEdge;
import org.tts.model.flat.relationship.NonCovalentBindingFlatEdge;
import org.tts.model.flat.relationship.PhosphorylationFlatEdge;
import org.tts.model.flat.relationship.ProductFlatEdge;
import org.tts.model.flat.relationship.ReactantFlatEdge;
import org.tts.model.flat.relationship.StimulationFlatEdge;
import org.tts.model.flat.relationship.TargetsFlatEdge;
import org.tts.model.flat.relationship.UbiquitinationFlatEdge;
import org.tts.model.flat.relationship.UncertainProcessFlatEdge;
import org.tts.model.flat.relationship.UnknownFromSourceFlatEdge;

@Service
public class FlatEdgeService {

	public FlatEdge createFlatEdge(String sboTermString) {
		FlatEdge newEdge;
		switch (sboTermString) {
			case "SBO:0000180":
				newEdge = new DissociationFlatEdge();
				break;
			case "SBO:0000330":
				newEdge = new DephosphorylationFlatEdge();
				break;
			case "SBO:0000396":
				newEdge = new UncertainProcessFlatEdge();
				break;
			case "SBO:0000177":
				newEdge = new NonCovalentBindingFlatEdge();
				break;
			case "SBO:0000170":
				newEdge = new StimulationFlatEdge();
				break;
			case "SBO:0000217":
				newEdge = new GlycosylationFlatEdge();
				break;
			case "SBO:0000216":
				newEdge = new PhosphorylationFlatEdge();
				break;
			case "SBO:0000169":
				newEdge = new InhibitionFlatEdge();
				break;
			case "SBO:0000224":
				newEdge = new UbiquitinationFlatEdge();
				break;
			case "SBO:0000214":
				newEdge = new MethylationFlatEdge();
				break;
			case "SBO:0000344":
				newEdge = new MolecularInteractionFlatEdge();
				break;
			case "SBO:0000168":
				newEdge = new ControlFlatEdge();
				break;
			case "targets":
				newEdge = new TargetsFlatEdge();
				break;
			case "unknownFromSource":
				newEdge = new UnknownFromSourceFlatEdge();
				break;
			case "IS_REACTANT":
				newEdge = new ReactantFlatEdge();
				break;
			case "IS_PRODUCT":
				newEdge = new ProductFlatEdge();
				break;
			case "IS_CATALYST":
				newEdge = new CatalystFlatEdge();
				break;
			default:
				newEdge = new UnknownFromSourceFlatEdge();
				break;
			}
		return newEdge;
	}
}
