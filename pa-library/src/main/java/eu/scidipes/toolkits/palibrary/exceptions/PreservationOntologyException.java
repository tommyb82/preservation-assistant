package eu.scidipes.toolkits.palibrary.exceptions;

/**
 * @author Simon Berriman
 */
public class PreservationOntologyException extends PreservationException {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public PreservationOntologyException() {
	}

	/**
	 * @param message
	 */
	public PreservationOntologyException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PreservationOntologyException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PreservationOntologyException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public PreservationOntologyException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
