package org.tts.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

public class SifFile extends ResourceSupport {
	
	private Long fileId = null;
	
	private String filename = null;
	
	private List<SifEntry> sifEntries;
	
	
	
	/**
	 * Default Constructor
	 * Needed for building the beans
	 */
	public SifFile() {
		this.sifEntries = new ArrayList<SifEntry>();
	}



	public Long getFileId() {
		return fileId;
	}



	public void setFileId(Long id) {
		this.fileId = id;
	}



	public String getFilename() {
		return filename;
	}



	public void setFilename(String filename) {
		this.filename = filename;
	}



	public List<SifEntry> getSifEntries() {
		return sifEntries;
	}



	public void setSifEntries(List<SifEntry> sifEntries) {
		this.sifEntries = sifEntries;
	}
	
	public SifFile addSifEntry(String startnode, String edge, List<String> endnodes) {
		this.getSifEntries().add(new SifEntry(startnode, edge, endnodes));
		return this;
	}
	
	public SifFile addSifEntry(String startnode, String edge, String endnode) {
		List<String> endnodes = new ArrayList<String>();
		endnodes.add(endnode);
		return addSifEntry(startnode, edge, endnodes);
	}
	
	
	public String toString() {
		String ret;
		List<String> entries = new ArrayList<String>();
		for (SifEntry entry : getSifEntries()) {
			entries.add(entry.toString());
		}
		return String.join("\n", entries);
	}
		
}
