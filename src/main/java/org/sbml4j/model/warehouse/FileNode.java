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
package org.sbml4j.model.warehouse;

import org.neo4j.ogm.annotation.NodeEntity;
import org.sbml4j.model.common.GraphEnum.FileNodeType;

@NodeEntity(label="FileNode")
public class FileNode extends WarehouseGraphNode {

	private FileNodeType fileNodeType;
	
	private String filename;
	
	private byte[] filecontent;

	private String md5sum;
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public byte[] getFilecontent() {
		return filecontent;
	}

	public void setFilecontent(byte[] filecontent) {
		this.filecontent = filecontent;
	}

	public FileNodeType getFileNodeType() {
		return fileNodeType;
	}

	public void setFileNodeType(FileNodeType fileNodeType) {
		this.fileNodeType = fileNodeType;
	}

	public String getMd5sum() {
		return md5sum;
	}

	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	
}
