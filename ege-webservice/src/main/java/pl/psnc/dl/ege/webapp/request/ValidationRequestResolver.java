package pl.psnc.dl.ege.webapp.request;

import javax.servlet.http.HttpServletRequest;

import pl.psnc.dl.ege.types.DataType;

/**
 * <p>RequestResolver extension for validation operation of web service.</p>
 * Decodes request URL and marks one of the available validation operations.
 * 
 * @author mariuszs
 */
public class ValidationRequestResolver extends RequestResolver
{
	private static final String SLICE_BASE = "Validation/";
	
	
	public ValidationRequestResolver(HttpServletRequest request, Method method) throws RequestResolvingException{
		this.request = request;
		this.method = method;
		init();
	}
	
	private void init() throws RequestResolvingException{
		if(method.equals(Method.POST)){
			resolvePOST();
		}
		else if(method.equals(Method.GET)){
			resolveGET();
		}
	}
	
	private void resolvePOST() throws RequestResolvingException{
		String[] queries = resolveQueries();
		if(queries.length > 1){
			DataType dataType = decodeDataType(queries[1]);
			data = dataType;
			operation = OperationId.PERFORM_VALIDATION;
		}
		else{
			throw new RequestResolvingException(RequestResolvingException.Status.BAD_REQUEST);
		}
	}
	
	private void resolveGET() throws RequestResolvingException{
		String[] queries = resolveQueries();
		if(queries.length == 1){
			 operation = OperationId.PRINT_VALIDATIONS;
		}
		else{
			throw new RequestResolvingException(RequestResolvingException.Status.WRONG_METHOD);
		}
	}
	
	private String[] resolveQueries()
	{
		String params = request.getRequestURL().toString();
		params = (params.endsWith(SLASH) ? params : params + SLASH);
		params = params.substring(params.indexOf(SLICE_BASE),
			params.length());
		String[] queries = params.split(SLASH);
		return queries;
	}
	
	
	@Override
	public String getLocale()
	{
		return null;
	}
	
}
