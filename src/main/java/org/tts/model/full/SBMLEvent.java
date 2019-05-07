package org.tts.model.full;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;

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
