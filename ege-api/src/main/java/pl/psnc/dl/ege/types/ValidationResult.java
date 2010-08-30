package pl.psnc.dl.ege.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores result of EGE validation process.<br/>
 * 
 * Each instance provides client with information about result
 * status and messages that were generated during validation process.<br/><br/>
 *  
 * Currently three status values are supported : 
 * <ul>
 * <li>ERROR : errors were found, not necessarily critical; </li>
 * <li>SUCCESS : streamed data was validated as correct representation; messages may contain warnings.</li>
 * <li>FATAL : validation process ended with fatal error(s);   
 * </ul>
 * <br/>
 * By default there is a limit of 50 for number of messages, that can be kept in result.<br/>
 * This value can be change through proper constructor.
 * 
 * @author mariuszs
 */
public class ValidationResult
{
	/**
	 * Enum class represents validation status. 
	 */
	public static enum Status {
		ERROR, SUCCESS, FATAL;
	}
	
	/**
	 * Default value of maximum number of messages.
	 */
	public static final int DEFAULT_NR_OF_MESSAGES = 50;
	
	protected int maxMessages = DEFAULT_NR_OF_MESSAGES;
	
	protected Status status;
	
	protected List<String> messages = new ArrayList<String>();
	
	/**
	 * Default constructor.
	 * 
	 * @param status
	 */
	public ValidationResult(Status status){
		this.status = status;
	}
	
	/**
	 * Constructor - status with single message.
	 * 
	 * @param status status of validation
	 * @param message validation message
	 */
	public ValidationResult(Status status, String message){
		this(status);
		this.messages.add(message);
	}
	
	/**
	 * Constructor - status with multiple messages.
	 * 
	 * @param status status of validation
	 * @param messages validation messages
	 */
	public ValidationResult(Status status, List<String> messages){
		this.status = status;
		this.messages = messages;
	}
	
	/**
	 * Constructor - status with max number of validation messages.
	 * 
	 * @param status status of validation
	 * @param maxMessages maximum number of validation messages
	 */
	public ValidationResult(Status status, int maxMessages){
		this(status);
		this.maxMessages = maxMessages;
	}
	
	/**
	 * Constructor - status with single message and threshold for number of messages.
	 *  
	 * @param status status of validation
	 * @param message validation message
	 * @param maxMessages maximum number of messages
	 */
	public ValidationResult(Status status, String message, int maxMessages){
		this(status,message);
		this.maxMessages = maxMessages;
	}
	
	/**
	 * Contructor - status with muliple messages and threshold for number of messages.
	 * 
	 * @param status status of validation
	 * @param messages validation messages
	 * @param maxMessages maximum number of messages
	 */
	public ValidationResult(Status status, List<String> messages, int maxMessages){
		this(status,messages);
		this.maxMessages = maxMessages;
	}
	
	/**
	 * Records validation message.
	 * 
	 * @param message validation message
	 */
	public void putMessage(String message){
		if(messages.size() < maxMessages){
			messages.add(message);
			if(messages.size() == maxMessages){
				messages.add("...");
			}
		}
	}
	
	/**
	 * Returns list of validation messages.
	 * 
	 * @return list of validation messages
	 */
	public List<String> getMessages(){
		List<String> messages = new ArrayList<String>(this.messages);
		return messages;
	}
	
	/**
	 * Returns status of validation.
	 * 
	 * @return status
	 */
	public Status getStatus(){
		return status;
	}
	
	/**
	 * Changes status of validation.
	 * 
	 * @param status status of validation
	 */
	public void setStatus(Status status){
		this.status = status;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("ValidationResult(max.messages:"+this.maxMessages+")");
		sb.append("\nStatus:"+this.status);
		for(String msg : messages){
			sb.append(msg);
			sb.append("\n");
		}
		return sb.toString();
	}
	
}	
