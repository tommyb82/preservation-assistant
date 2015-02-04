/**
 *
 */
package eu.scidipes.toolkits.palibrary.exceptions;

import eu.scidipes.toolkits.palibrary.interfaces.Preservable;

/**
 * @author Simon Berriman
 * @author Tom Bunting
 */
public class PreservationStorageException extends PreservationException {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final Preservable preservable;

	/**
	 * @param message
	 * @param preservable
	 */
	public PreservationStorageException(final String message, final Preservable preservable) {
		super(message);
		this.preservable = preservable;
	}

	/**
	 * @param cause
	 * @param preservable
	 */
	public PreservationStorageException(final Throwable cause, final Preservable preservable) {
		super(cause);
		this.preservable = preservable;
	}

	/**
	 * @param message
	 * @param cause
	 * @param preservable
	 */
	public PreservationStorageException(final String message, final Throwable cause, final Preservable preservable) {
		super(message, cause);
		this.preservable = preservable;
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 * @param preservable
	 */
	public PreservationStorageException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace, final Preservable preservable) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.preservable = preservable;
	}

	/**
	 * Returns the wrapped {@link Preservable} instance for which the preservation operation failed
	 * 
	 * @return the preservable
	 */
	public Preservable getPreservable() {
		return preservable;
	}

}
