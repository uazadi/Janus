package it.unimib.disco.essere.deduplicator.versioning;

public class VersionerException extends Exception {

	public VersionerException() {
	}

	public VersionerException(String message) {
		super(message);
	}

	public VersionerException(Throwable cause) {
		super(cause);
	}

	public VersionerException(String message, Throwable cause) {
		super(message, cause);
	}

	public VersionerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
