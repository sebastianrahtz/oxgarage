package pl.psnc.dl.ege.exception;

/**
 * Standard EGE class of exceptions.
 * 
 * @author mariuszs
 */
public class EGEException extends Exception {
	
	public static final String WRONG_CONFIGURATION = "An error occurred during configuration of component!"; 
	
	protected String message;
	
	/**
	 * Default constructor.
	 * 
	 * @param message message of exception
	 */
	public EGEException(String message){
		this.message = message;
	}
	
	/**
	 * Returns message of exception
	 * 
	 * @return message
	 */
	public String getMessage(){
		return this.message;
	}
	
	/**
	 * Method Sets message of exception
	 * 
	 * @param message message of exception
	 */
	public void setMessage(String message){
		this.message = message;
	}
	
	
	
}

