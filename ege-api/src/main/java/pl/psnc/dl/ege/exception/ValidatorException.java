package pl.psnc.dl.ege.exception;

import pl.psnc.dl.ege.types.DataType;

/**
 * Exception occur when trying to perform
 * validate() method on unknown to {@link Validator} implementation
 * {@link DataType} or when some unpredicted error occurs within validation method. 
 * 
 * @author mariuszs
 *
 */
public class ValidatorException extends EGEException {
	
	public static final String NOT_SUPPORTED_DATA_TYPE = "Specified DataType not supported by Validator(s): "; 
	
	public static final String INTERNAL_ERROR = "Internal error occured in validation method ";
	
	enum Type {
		NOT_SUPPORTED_DATA, INTERNAL_ERROR;
	}
	
	private Type errorType = Type.INTERNAL_ERROR;
	
	/**
	 * Default constructor.
	 */
	public ValidatorException(){
		super(INTERNAL_ERROR);
	}
	
	/**
	 * Constructor with message parameter
	 * 
	 * @param message message of exception
	 */
	public ValidatorException(String message){
		super(message);
	}
	
	/**
	 * Constructs exception with 'not supported data type' message.
	 * Instance of DataType is specified in exception message.
	 * 
	 * @param dataType argument of data type
	 */
	public ValidatorException(DataType dataType){
		 super(NOT_SUPPORTED_DATA_TYPE + dataType.toString());
		 this.errorType = Type.NOT_SUPPORTED_DATA;
	}
	
	/**
	 * Returns type of error reported by this exception.
	 * 
	 * @return
	 */
	public Type getErrorType(){
		return errorType;
	}
	
}
