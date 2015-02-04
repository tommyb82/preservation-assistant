package eu.scidipes.toolkits.palibrary.exceptions;

/**
 * @author Simon Berriman
 */
public class PreservationInitialisationException extends PreservationException {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public PreservationInitialisationException() {
	}

	/**
	 * @param message
	 */
	public PreservationInitialisationException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PreservationInitialisationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PreservationInitialisationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public PreservationInitialisationException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
