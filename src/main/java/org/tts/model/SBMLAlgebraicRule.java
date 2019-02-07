package org.tts.model;

/**
 * As per definition in the SBML Core Specification
 * an "AlgebraicRule is used to express equations that are 
 * neither assignments of model variables nor rates of change"
 * It thus adds no further attributes to the inherited {@code math} attribute
 * from SBMLRule and exisits to be able to distinguish it from the other types
 * of rules. 
 * @author ttiede
 *
 */
public class SBMLAlgebraicRule extends SBMLRule {

}
