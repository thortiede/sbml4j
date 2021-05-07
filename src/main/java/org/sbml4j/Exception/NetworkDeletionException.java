package org.sbml4j.Exception;

public class NetworkDeletionException extends Exception {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 2280786314414407681L;

	private int code;
	
	public NetworkDeletionException(int code, String msg) {
		super(msg);
		this.setCode(code);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
