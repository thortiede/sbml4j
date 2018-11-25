package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.NodeEdgeList;
import org.tts.model.NodeNodeEdge;
import org.tts.model.SifFile;

@Service
public class FileServiceImpl implements FileService {

	
	private NodeEdgeListService nodeEdgeListService;
	
	
	@Autowired
	public FileServiceImpl(NodeEdgeListService nodeEdgeListService) {
		super();
		this.nodeEdgeListService = nodeEdgeListService;
	}



	@Override
	public SifFile getFullNet() {
		
		return convertToSif( nodeEdgeListService.getFullNet());
		
	}



	private SifFile convertToSif(NodeEdgeList nodeEdgeList) {
		SifFile ret = new SifFile();
		for (NodeNodeEdge nne : nodeEdgeList.getNodeNodeEdgeList()) {
			ret.addSifEntry(nne.getNode1(), nne.getEdge(), nne.getNode2());
		}
		return ret;
	}



	@Override
	public SifFile getMetabolicNetwork() {
		
		return convertToSif(nodeEdgeListService.getMetabolicNet());
		
		
	}

}
