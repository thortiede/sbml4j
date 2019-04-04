package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.NodeEdgeList;
import org.tts.model.NodeNodeEdge;
import org.tts.model.SifFile;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	public FileServiceImpl() {
		super();
	}

	@Override
	public SifFile getSifFromNodeEdgeList(NodeEdgeList nodeEdgeList) {
		if(nodeEdgeList == null) {
			return null;
		} else {
			SifFile ret = new SifFile();
			for (NodeNodeEdge nne : nodeEdgeList.getNodeNodeEdgeList()) {
				ret.addSifEntry(nne.getNode1(), nne.getEdge(), nne.getNode2());
			}
			return ret;
		}
	}

}
