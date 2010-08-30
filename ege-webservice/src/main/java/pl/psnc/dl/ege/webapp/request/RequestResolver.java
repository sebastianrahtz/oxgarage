package pl.psnc.dl.ege.webapp.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import pl.psnc.dl.ege.types.DataType;

/**
 * <p>Abstract base class for resolving request for WS operation.</p>
 * 
 * @author mariuszs
 */
public abstract class RequestResolver
{	
	public static final String UNDERSCORE = "_";

	public static final String DUO_UNDERSCORE = "__";

	public static final String COLON = ":";

	public static final String SPLITTER = COLON;

	public static final String SLASH = "/";

	public static final String COMMA = ",";

	public static final String SEMICOLON = ";";

	protected Method method;
	
	protected HttpServletRequest request;
	
	protected OperationId operation = null;
	
	protected Object data = null;
	
	/**
	 * Properly formed request should inform about operation to perform.<br/>
	 * E.g. operation of conversion or operation of listing all available validations. 
	 * 
	 * @return operation id
	 */
	public OperationId getOperationId(){
		return operation;
	}
	
	/**
	 * Returns decoded operation data (if it is needed), e.g. data types.
	 * Returned 'Object' should be casted into expected type.
	 * 
	 * @return data
	 */
	public Object getData(){
		return data;
	}
	
	/**
	 * Returns reference to request. 
	 * 
	 * @return
	 */
	public HttpServletRequest getRequest(){
		return request;
	}
	
	/**
	 * Should return local id of request. 
	 * 
	 * @return
	 */
	public abstract String getLocale();
	
	/**
	 * Encodes specified data type into URI proper form.
	 * Encoder uses 'UTF-8'. Encoded data type syntax is -  
	 * <p><b>[format]:[mime_part1]:[mime_part2]</b></p>
	 * 
	 * @param dataType
	 * @return
	 */
	public String encodeDataType(DataType dataType)
	{
		String format = null;
		String mime = null;
		try {
			format = dataType.getFormat();
			String[] mimes = dataType.getMimeType().split(SLASH);
			mime = mimes[0] + SPLITTER + mimes[1];
			return URLEncoder.encode(format + SPLITTER + mime, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			return null;
		}
	}
	
	/**
	 * Decodes URI part into data type.
	 * Encoded data type syntax is -  
	 * <p><b>[format]:[mime_part1]:[mime_part2]</b></p>
	 * 
	 * @param uriPart
	 * @return decoded data type
	 */
	public DataType decodeDataType(String uriPart)
	{
		try {
			String[] partial = (URLDecoder.decode(uriPart, "UTF-8"))
					.split(SPLITTER);
			DataType dataType = null;
			String mime = partial[1] + SLASH + partial[2];
			dataType = new DataType(partial[0], mime.replaceAll(" ","+"));
			return dataType;
		}
		catch (UnsupportedEncodingException ex) {
			return null;
		}

	}
}
