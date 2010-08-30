package pl.psnc.dl.ege.webapp.request;

/**
 * Created during performance of {@link ConversionRequestResolver}.<br/>
 * <br/>
 * Each instance has its own status - it is used to point out
 * appropriate HTTP status when an error occurs.   
 * 
 *  @author mariuszs
 */
public class RequestResolvingException extends Exception
{
	private final Status status;
	
	public static final String CONV_PARAMS = "Conversion properties expected on conversion request (POST method)!";
	
	public enum Status { 
		WRONG_METHOD,BAD_REQUEST, ERROR
	}
	
	public RequestResolvingException(String message){
		super(message);
		this.status = Status.ERROR;
	}
	
	public RequestResolvingException(RequestResolvingException.Status status){
		this.status = status;
	}
	
	public RequestResolvingException(RequestResolvingException.Status status, String message){
		super(message);
		this.status = status;
	}
	
	public Status getStatus(){
		return this.status;
	}
}
