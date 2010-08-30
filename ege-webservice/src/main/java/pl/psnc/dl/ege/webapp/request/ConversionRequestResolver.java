package pl.psnc.dl.ege.webapp.request;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import pl.psnc.dl.ege.types.DataType;

/**
 * <p>Request resolving for Conversion class servlet.</p>
 * 
 * @author mariuszs
 *
 */
public class ConversionRequestResolver extends RequestResolver
{
	
	public static final String CONVERSIONS_SLICE_BASE = "Conversions/";

	private static final String EN = "en";


	public ConversionRequestResolver(HttpServletRequest request, Method method)
		throws RequestResolvingException
	{
		this.method = method;
		this.request = request;
		init();
	}


	private void init()
		throws RequestResolvingException
	{
		if (method.equals(Method.GET)) {
			resolveGET();
		}
		else if (method.equals(Method.POST)) {
			resolvePOST();
		}
	}


	private void resolvePOST()
		throws RequestResolvingException
	{
		String[] queries = resolveQueries();
		if (queries.length < 3) {
			throw new RequestResolvingException(
					RequestResolvingException.Status.BAD_REQUEST);
		}

		DataType iDataType = decodeDataType(queries[1]);
		int lastIndex = queries.length - 1;
		if (queries[queries.length - 1].lastIndexOf("conversion") > -1)
			lastIndex = queries.length - 2;
		List<DataType> pathFrame = new ArrayList<DataType>();
		pathFrame.add(iDataType);
		//Construct path frame to compare it with selected conversion path
		for (int i = 2; i <= lastIndex; i++) {
			DataType dt = decodeDataType(queries[i]);
			if (dt == null && i != lastIndex) {
				throw new RequestResolvingException(
						RequestResolvingException.Status.BAD_REQUEST);
			}
			pathFrame.add(dt);
		}
		operation = OperationId.PERFORM_CONVERSION;
		data = pathFrame;

	}


	private void resolveGET()
		throws RequestResolvingException
	{
		String[] queries = resolveQueries();
		if (queries.length > 1) {
			// query contains conversion path informations
			if (queries.length > 2) {
				throw new RequestResolvingException(
						RequestResolvingException.Status.WRONG_METHOD);
			}
			else {
				DataType inputType = decodeDataType(queries[1]);
				operation = OperationId.PRINT_CONVERSIONS_PATHS;
				data = inputType;
			}
		}
		else if (queries.length == 1) {
			operation = OperationId.PRINT_INPUT_TYPES;
		}
	}

	private String[] resolveQueries()
	{
		String params = request.getRequestURL().toString();
		params = (params.endsWith(SLASH) ? params : params + SLASH);
		params = params.substring(params.indexOf(CONVERSIONS_SLICE_BASE),
			params.length());
		String[] queries = params.split(SLASH);
		return queries;
	}
	
	/**
	 * Reads additional conversion properties send through request.
	 * Properties are used only when requesting for conversion (by POST method).
	 * 
	 * @return
	 * @throws RequestResolvingException
	 */
	public String getConversionProperties()
		throws RequestResolvingException
	{
		if (method.equals(Method.POST)) {
			return request.getParameter("properties");
		}
		else {
			throw new RequestResolvingException(
					RequestResolvingException.CONV_PARAMS);
		}
	}

	/*
	 * TODO : local names mechanism 
	 */
	public String getLocale()
	{
		return EN;
	}


}
