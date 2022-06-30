package org.sbml4j.service.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sbml4j.model.provenance.ProvenanceMetaDataNode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ProvenanceMetaDataNodeSerializer extends StdSerializer<ProvenanceMetaDataNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4002803385898201854L;

	protected ProvenanceMetaDataNodeSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void serialize(ProvenanceMetaDataNode value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {
		jgen.writeStartObject();
		
		Map<String, Object> nodeProvenanceAnnotation = value.getProvenance();
		// process the direct provenance annotations
		for (String key : nodeProvenanceAnnotation.keySet()) {
			Object val = nodeProvenanceAnnotation.get(key);
			if (val instanceof Long) {
				jgen.writeNumberField(key, (Long) val);
			} else if (val instanceof Boolean) {
				jgen.writeBooleanField(key, (Boolean) val);
			} else { //if (val instanceof String) {
				jgen.writeStringField(key, (String) val);
			}
		}
		
		// process subelements
		List<ProvenanceMetaDataNode> subelements = value.getProvenanceAnnotationSubelements();
		for (ProvenanceMetaDataNode elem : subelements) {
			jgen.writeFieldName(elem.getProvenanceName());
			this.serialize(elem, jgen, provider);
			jgen.writeEndObject();
		}
		
		jgen.writeEndObject();
		
	}

}
