package pl.psnc.dl.ege.webapp.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import pl.psnc.dl.ege.EGE;
import pl.psnc.dl.ege.EGEImpl;
import pl.psnc.dl.ege.exception.ValidatorException;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;
import pl.psnc.dl.ege.webapp.request.Method;
import pl.psnc.dl.ege.webapp.request.RequestResolver;
import pl.psnc.dl.ege.webapp.request.RequestResolvingException;
import pl.psnc.dl.ege.webapp.request.ValidationRequestResolver;

/**
 * Serves validation operations in RESTful WS manner.
 */
public class ValidationServlet
	extends HttpServlet
{

	private static final Logger LOGGER = Logger
			.getLogger(ValidationServlet.class);

	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ValidationServlet()
	{
		super();
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response)
		throws ServletException, IOException
	{
		try {
			//resolve request and catch any errors
			RequestResolver rr = new ValidationRequestResolver(request,
					Method.GET);
			//print available validation options
			printAvailableValidations(response, rr);
		}
		catch (RequestResolvingException ex) {
			if (ex.getStatus().equals(
				RequestResolvingException.Status.WRONG_METHOD)) {
				//TODO : something with "wrong" method message (and others)
				response.sendError(405, ConversionServlet.R_WRONG_METHOD);
			}
			else {
				throw new ServletException(ex);
			}
		}
	}


	/*
	 * Print into response available validation options
	 */
	private void printAvailableValidations(HttpServletResponse response,
			RequestResolver rr)
		throws ServletException
	{
		EGE ege = new EGEImpl();
		try {
			PrintWriter out = response.getWriter();
			Set<DataType> dts = ege.returnSupportedValidationFormats();
			if(dts.size() == 0){
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return;
			}
			response.setContentType("text/xml");
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out
					.println("<validations xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
			String prefix = rr.getRequest().getRequestURL().toString()
					+ (rr.getRequest().getRequestURL().toString().endsWith(
						RequestResolver.SLASH) ? "" : "/");
			for (DataType dt : dts) {
				out.println("<input-data-type id=\"" + dt.toString()
						+ "\" xlink:href=\"" + prefix + rr.encodeDataType(dt)
						+ "/\" />");
			}
			out.println("</validations>");
		}
		catch (IOException ex) {
			throw new ServletException(ex);
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response)
		throws ServletException, IOException
	{
		try {
			RequestResolver rr = new ValidationRequestResolver(request,
					Method.POST);
			DataType dt = (DataType) rr.getData();
			performValidation(dt, rr, response);
		}
		catch (RequestResolvingException ex) {
			//TODO: zastanowic sie nad sytuacja, gdy request jest nieprawidlowo sparsowany
			if (ex.getStatus().equals(
				RequestResolvingException.Status.BAD_REQUEST)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			else {
				throw new ServletException(ex);
			}
		}
		catch (ValidatorException ex){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	/*
	 * Performs validation and prints its results into the response.
	 */
	private void performValidation(DataType dt, RequestResolver rr,
			HttpServletResponse response)
		throws Exception
	{
		EGE ege = new EGEImpl();
		InputStream is = null;
		if (ServletFileUpload.isMultipartContent(rr.getRequest())) {
			try {
				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iter = upload.getItemIterator(rr.getRequest());
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					if (!item.isFormField()) {
						is = item.openStream();
						//perform validation and print result to response.
						ValidationResult result = ege.performValidation(is, dt);
						printValidationResult(response,result);
						is.close();
					}
				}
			}
			catch(ValidatorException ex){
				throw ex;
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				throw ex;
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException ex) {
						//ignore
					}
				}
			}
		}
	}
	
	public void printValidationResult(HttpServletResponse response, ValidationResult result) throws IOException{
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml");
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<validation-result>");
		out.println("<status>" + result.getStatus()
				+ "</status>");
		out.println("<messages>");
		int i = 1;
		for (String msg : result.getMessages()) {
			out.println("<message><![CDATA[" + msg + "]]></message>");
			i++;
		}
		out.println("</messages>");
		out.println("</validation-result>");
	}
	

}
