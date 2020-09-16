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
package org.tts.Exception;

public class SBML4jAddExtensionException extends Exception {
	
	/**
	 * Generated serialVersionUID for Serialization
	 */
	private static final long serialVersionUID = 5470250965350498807L;

	public SBML4jAddExtensionException(String message) {
		super(message);
	}
	
	/**
	 * Effort to support exception chaining
	 * @param throwable The Throwable to be chained
	 */
	public SBML4jAddExtensionException(Throwable throwable) {
		super(throwable);
	}
	
	/**
	 * Effort to support exception chaining
	 * @param message The message from the Exception to pass on
	 * @param throwable The Throwable to be chained
	 */
	public SBML4jAddExtensionException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
