package us.kbase.auth2.lib.exceptions;

/** The provided token either does not exist in the database or is otherwise
 * invalid.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class InvalidTokenException extends AuthenticationException {
	
	public InvalidTokenException() {
		super(AuthError.INVALID_TOKEN, null);
	}
	
	public InvalidTokenException(final String message) {
		super(AuthError.INVALID_TOKEN, message);
	}
	
	public InvalidTokenException(final String message, final Throwable cause) {
		super(AuthError.INVALID_TOKEN, message, cause);
	}
}
