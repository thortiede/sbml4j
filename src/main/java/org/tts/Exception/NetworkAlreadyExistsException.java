package org.tts.Exception;

public class NetworkAlreadyExistsException extends Exception {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 6042383075273242720L;

	private int code;
	
	public NetworkAlreadyExistsException(int code, String msg) {
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
