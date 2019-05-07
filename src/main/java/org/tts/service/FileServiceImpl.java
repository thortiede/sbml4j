package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.NodeNodeEdge;
import org.tts.model.api.Output.SifFile;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	public FileServiceImpl() {
		super();
	}

	@Override
	public SifFile getSifFromNodeEdgeList(NodeEdgeList nodeEdgeList) {
		if(nodeEdgeList != null) {
			SifFile ret = new SifFile();
			for (NodeNodeEdge nne : nodeEdgeList.getNodeNodeEdgeList()) {
				ret.addSifEntry(nne.getNode1(), nne.getEdge(), nne.getNode2());
			}
			return ret;
		} else {
			return null;
		}
	}

}
