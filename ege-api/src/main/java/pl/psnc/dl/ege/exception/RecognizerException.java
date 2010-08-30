package pl.psnc.dl.ege.exception;

/**
 * Exception occurrence is expected when using 
 * <code>recognize()</code> method of a Recognizer implementation 
 *  - if MIME type of a specified input was not recognized. 
 *  
 * @author mariuszs
 */
public class RecognizerException extends EGEException{
	
	public final static String MSG = "Component was not able to recognize MIME type of specified data.";
	
	/**
	 * Default constructor.
	 */
	public RecognizerException(){
		super(MSG);
	}
	
	/**
	 * Constructor with specified message.
	 * 
	 * @param message message of exception
	 */
	public RecognizerException(String message) {
		super(message);
	}
	
}
