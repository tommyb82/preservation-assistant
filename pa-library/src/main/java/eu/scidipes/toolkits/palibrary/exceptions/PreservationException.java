/**
 *
 */
package eu.scidipes.toolkits.palibrary.exceptions;

/**
 * @author Simon Berriman
 */
public class PreservationException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public PreservationException() {
	}

	/**
	 * @param message
	 */
	public PreservationException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PreservationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PreservationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public PreservationException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
