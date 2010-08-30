package pl.psnc.dl.ege.validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import pl.psnc.dl.ege.types.ValidationResult;

/**
 * Catches errors received during XML validation process
 * of SAX parser. <br/>
 * 
 * Each object contains one {@link ValidationResult} instance - where
 * all received notifications are stored. Depending on received errors 
 * status of validation result is changed.<br/>
 * 
 * After validation, validation result instance can be retrieved in order
 * to read status and contained messages.   
 * 
 * @author mariuszs
 *
 */
public class StandardErrorHandler
	implements ErrorHandler
{
	
	private final ValidationResult valResult;
	
	/**
	 * Constructs error handler with validation result of SUCCESS status. 
	 */
	public StandardErrorHandler(){
		valResult = new ValidationResult(ValidationResult.Status.SUCCESS);
	}
	
	/**
	 * Constructs error handler with specified validation result instance.
	 * If referenced valResult parameter is 'null' a default instance is created.  
	 * 
	 * @param valResult
	 */
	public StandardErrorHandler(ValidationResult valResult){
		if(valResult == null){
			valResult = new ValidationResult(ValidationResult.Status.SUCCESS);
		}
		this.valResult = valResult;
	}
	
	public void error(SAXParseException exception)
		throws SAXException
	{
		valResult.putMessage(
			"Error in line (" + exception.getLineNumber() + "), column  ("
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage());
		if(!valResult.getStatus().equals(ValidationResult.Status.FATAL)){
			valResult.setStatus(ValidationResult.Status.ERROR);
		}
	}


	public void fatalError(SAXParseException exception)
		throws SAXException
	{
		valResult.putMessage(
			"Fatal error! in line (" + exception.getLineNumber()
					+ "), column  (" + exception.getColumnNumber() + ") : "
					+ exception.getMessage());
		valResult.setStatus(ValidationResult.Status.FATAL);
	}

	
	public void warning(SAXParseException exception)
		throws SAXException
	{
		valResult.putMessage(
			"Warning in line (" + exception.getLineNumber() + "), column  ("
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage());
	}

	/**
	 * Returns contained validation result instance.
	 * 
	 * @return
	 */
	public ValidationResult getValidationResult()
	{
		return valResult;
	}
	
}
