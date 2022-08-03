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
package org.sbml4j.model.full;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

public class SBMLEvent extends SBMLSBaseEntity {

	private SBMLTrigger trigger;
	
	private SBMLPriority priority;
	
	private SBMLDelay delay;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	List<SBMLEventAssignment> eventAssignments;

	public SBMLTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(SBMLTrigger trigger) {
		this.trigger = trigger;
	}

	public SBMLPriority getPriority() {
		return priority;
	}

	public void setPriority(SBMLPriority priority) {
		this.priority = priority;
	}

	public SBMLDelay getDelay() {
		return delay;
	}

	public void setDelay(SBMLDelay delay) {
		this.delay = delay;
	}

	public List<SBMLEventAssignment> getEventAssignments() {
		return eventAssignments;
	}

	public void setEventAssignments(List<SBMLEventAssignment> eventAssignments) {
		this.eventAssignments = eventAssignments;
	}
}
