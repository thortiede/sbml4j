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
