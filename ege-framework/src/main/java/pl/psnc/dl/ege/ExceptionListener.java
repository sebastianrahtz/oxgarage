package pl.psnc.dl.ege;

/**
 * Interface for catching exceptions - most likely can be used by class to catch
 * exceptions in threads and send them to listener.
 * 
 * @author mariuszs
 *
 */
public interface ExceptionListener {
	
	/**
	 * Implemented method normally would store received exception.
	 * 
	 * @param ex received exception
	 */
	public void catchException(Exception ex);
	
}
