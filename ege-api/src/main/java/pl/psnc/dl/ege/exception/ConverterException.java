package pl.psnc.dl.ege.exception;

/**
 * Should be threw if some error occurred during
 * perform of conversion logic. 
 * 
 * @author mariuszs
 */
public class ConverterException extends EGEException {
	
	private String from;
	
	private String to;
	
	public static final String UNSUPPORTED_CONVERSION_TYPES = "Unsupported conversion action data types!";
	public static final String NULL_CONVERSION_TYPES = "Null conversion action data types!"; 
	
	public ConverterException(String message){
		super(message);
		this.from = null;
		this.to = null;
	}
	
	/**
	 * Default constructor
	 * 
	 * @param message
	 * @param from
	 * @param to
	 */
	public ConverterException(String message, String from, String to) {
		super(message);
		this.from = from;
		this.to = to;
	}
	
	/**
	 * Returns message of exceptions
	 * 
	 * @return message
	 */
	@Override
	public String getMessage(){
		if(from != null && to != null)
			return message + " : throwed while performing conversion from: " + getFrom() + " to: " + getTo();
		else
			return message;	
	}
	
	/**
	 * Returns String representation of {@link DataType} 
	 * which was a source of failed conversion.
	 *  
	 * @return source data type of failed conversion
	 */
	public String getFrom(){
		return from;
	}
	
	/**
	 * Sets from parameter.
	 * 
	 * @param from
	 */
	public void setFrom(String from){
		this.from = from;
	}
	
	/**
	 * Returns String representation of {@link DataType}
	 * which was a destination data type of failed conversion.
	 * 
	 * @return
	 */
	public String getTo(){
		return to;
	}
		
	/**
	 * Sets to parameter.
	 * 
	 * @param to
	 */
	public void setTo(String to){
		this.to = to;
	}
	
}
