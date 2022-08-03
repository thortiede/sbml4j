/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.model.full;

/**
 * As per definition in the SBML Core Specification
 * an "AlgebraicRule is used to express equations that are 
 * neither assignments of model variables nor rates of change"
 * It thus adds no further attributes to the inherited {@code math} attribute
 * from SBMLRule and exisits to be able to distinguish it from the other types
 * of rules. 
 * @author Thorsten Tiede
 *
 */
public class SBMLAlgebraicRule extends SBMLRule {

}
