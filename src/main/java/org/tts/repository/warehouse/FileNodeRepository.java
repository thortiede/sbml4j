package org.tts.repository.warehouse;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.warehouse.FileNode;

public interface FileNodeRepository extends Neo4jRepository<FileNode, Long> {

	public List<FileNode> findByFileNodeTypeAndFilename(FileNodeType fileNodeType, String originalFilename);
	
}
