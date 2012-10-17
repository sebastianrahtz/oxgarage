package pl.psnc.dl.ege.webapp.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import pl.psnc.dl.ege.EGE;
import pl.psnc.dl.ege.EGEImpl;
import pl.psnc.dl.ege.configuration.EGEConfigurationManager;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.exception.ValidatorException;
import pl.psnc.dl.ege.types.ConversionAction;
import pl.psnc.dl.ege.types.ConversionsPath;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;
import pl.psnc.dl.ege.utils.DataBuffer;
import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.utils.IOResolver;
import pl.psnc.dl.ege.webapp.config.LabelProvider;
import pl.psnc.dl.ege.webapp.config.MimeExtensionProvider;
import pl.psnc.dl.ege.webapp.config.PreConfig;
import pl.psnc.dl.ege.webapp.request.ConversionRequestResolver;
import pl.psnc.dl.ege.webapp.request.ConversionsPropertiesHandler;
import pl.psnc.dl.ege.webapp.request.Method;
import pl.psnc.dl.ege.webapp.request.OperationId;
import pl.psnc.dl.ege.webapp.request.RequestResolver;
import pl.psnc.dl.ege.webapp.request.RequestResolvingException;

/**
 * EGE RESTful WebService interface.
 * 
 * Conversion web service servlet, accepting requests in REST WS manner.
 * 
 * @author mariuszs
 */
/*
 * TODO : Metody tworzace XML wrzucic do osobnej klasy lub uzyc Apache Velocity.
 */
public class ConversionServlet extends HttpServlet {

	private static final String imagesDirectory = "media";

	private static final String EZP_EXT = ".ezp";

	private static final String FORMAT_DOCX = "docx";

	private static final String FORMAT_ODT = "oo";

	private static final String APPLICATION_MSWORD = "application/msword";

	private static final String APPLICATION_EPUB = "application/epub+zip";

	private static final String APPLICATION_ODT = "application/vnd.oasis.opendocument.text";

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final Logger LOGGER = Logger			.getLogger(ConversionServlet.class);

	private static final long serialVersionUID = 1L;

	public static final String SLASH = "/";

	public static final String COMMA = ",";

	public static final String SEMICOLON = ";";

	public static final String R_WRONG_METHOD = "Wrong method: GET, expected: POST.";

	public static final String CONVERSIONS_SLICE_BASE = "Conversions/";

	public static final String ZIP_EXT = ".zip";

	public static final String DOCX_EXT = ".docx";

	public static final String EPUB_EXT = ".epub";

	public static final String ODT_EXT = ".odt";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConversionServlet() {
		super();
	}

	/**
	 * Serves GET requests - responses are : list of input data types and lists
	 * of possible conversions paths.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			RequestResolver rr = new ConversionRequestResolver(request,
					Method.GET);
			if (rr.getOperationId().equals(OperationId.PRINT_CONVERSIONS_PATHS)) {
				DataType idt = (DataType) rr.getData();
				EGE ege = new EGEImpl();
				List<ConversionsPath> paths = ege.findConversionPaths(idt);
				printConversionsPaths(response, rr, paths);
			} else if (rr.getOperationId()
					.equals(OperationId.PRINT_INPUT_TYPES)) {
				EGE ege = new EGEImpl();
				Set<DataType> inpfo = ege.returnSupportedInputFormats();
				if (inpfo.size() == 0) {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					return;
				}
				printConversionPossibilities(response, rr, inpfo);
			}

		} catch (RequestResolvingException ex) {
			if (ex.getStatus().equals(
					RequestResolvingException.Status.WRONG_METHOD)) {
				response.sendError(405, R_WRONG_METHOD);
			} else {
				throw new ServletException(ex);
			}
		}

	}

	/*
	 * Send in response xml data of possible conversions paths.
	 */
	protected void printConversionsPaths(HttpServletResponse response,
			RequestResolver rr, List<ConversionsPath> paths) throws IOException {
		LabelProvider lp = getLabelProvider();
		response.setContentType("text/xml");
		PrintWriter out = response.getWriter();
		if (paths.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		StringBuffer resp = new StringBuffer();
		StringBuffer sbpath = new StringBuffer();
		StringBuffer pathopt = new StringBuffer();
		resp.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
		resp.append("<conversions-paths xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
		int counter = 0;
		String reqTransf;
		for (ConversionsPath cp : paths) {
			resp.append("<conversions-path xlink:href=\"");
			reqTransf = rr.getRequest().getRequestURL().toString();
			if (reqTransf.endsWith(SLASH)) {
				reqTransf = reqTransf.substring(0, reqTransf.length() - 2);
				resp.append(reqTransf
						.substring(0, reqTransf.lastIndexOf(SLASH))
						+ SLASH
						+ rr.encodeDataType((DataType) rr.getData())
						+ SLASH);
			} else {
				resp.append(reqTransf
						.substring(0, reqTransf.lastIndexOf(SLASH))
						+ SLASH
						+ rr.encodeDataType((DataType) rr.getData())
						+ SLASH);
			}
			sbpath.delete(0, sbpath.length());
			pathopt.delete(0, pathopt.length());
			counter = 0;
			for (ConversionAction ca : cp.getPath()) {
				sbpath.append(rr.encodeDataType(ca.getConversionOutputType())
						+ SLASH);
				pathopt.append("<conversion id=\"" + ca.toString()
						+ "\" index=\"" + counter + "\" >");
				String paramsDefs = ca.getConversionActionArguments().getPropertiesDefinitions();
				if (paramsDefs.length() > 0) {
					Properties props = new Properties();
					props.loadFromXML(new ByteArrayInputStream(paramsDefs.getBytes("UTF-8")));
					Set<Object> keySet = new TreeSet(props.keySet());
					for (Object key : keySet) {
						if (!key.toString().endsWith(".type")) {
							pathopt.append("<property id=\"" + key
									+ "\"><value>");
							pathopt.append("<![CDATA[" + props.get(key)
									+ "]]></value>");
							pathopt.append("<type>"
									+ props.get(key.toString() + ".type")
									+ "</type>");
							pathopt.append("<property-name>"
									+ lp.getLabel(key.toString())
									+ "</property-name></property>");
						}
					}
				}
				pathopt.append("</conversion>");
				counter++;
			}
			resp.append(sbpath);
			resp.append("\" ><path-name><![CDATA[ \n " + cp.toString()
					+ " \n ]]></path-name>");
			resp.append(pathopt);
			resp.append("</conversions-path>");
		}
		resp.append("</conversions-paths>");
		out.print(resp.toString());
		out.close();
	}

	/*
	 * Sends to response xml data of supported input data types.
	 */
	protected void printConversionPossibilities(HttpServletResponse response,
			RequestResolver rr, Set<DataType> inputDataTypes)
			throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml");
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<input-data-types xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
		String prefix = rr.getRequest().getRequestURL().toString()
				+ (rr.getRequest().getRequestURL().toString().endsWith(SLASH) ? ""
						: "/");
		for (DataType dt : inputDataTypes) {
			out.println("<input-data-type id=\"" + dt.toString()
					+ "\" xlink:href=\"" + prefix + rr.encodeDataType(dt)
					+ "/\" />");
		}
		out.println("</input-data-types>");
		out.close();
	}

	/**
	 * Servers POST method - performs conversions over specified within URL
	 * conversions path.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			ConversionRequestResolver rr = new ConversionRequestResolver(
					request, Method.POST);
			List<DataType> pathFrame = (List<DataType>) rr.getData();
			processConversion(response, rr, pathFrame);
		} catch (RequestResolvingException ex) {
			if (ex.getStatus().equals(
					RequestResolvingException.Status.BAD_REQUEST)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else if (ex.getStatus().equals(
					RequestResolvingException.Status.WRONG_METHOD)) {
				response.sendError(405, R_WRONG_METHOD);
			} else {
				throw new ServletException(ex);
			}
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	/*
	 * Performs conversion.
	 */
	protected void processConversion(HttpServletResponse response,
			ConversionRequestResolver rr, List<DataType> pathFrame)
			throws IOException, FileUploadException, EGEException,
			ConverterException, RequestResolvingException {
		EGE ege = new EGEImpl();
		List<ConversionsPath> cp = ege.findConversionPaths(pathFrame.get(0));
		ConversionsPath cpath = null;
		boolean found = false;
		for (ConversionsPath path : cp) {
			if ((pathFrame.size() - 1) != path.getPath().size()) {
				continue;
			}
			found = true;
			int count = 1;
			for (ConversionAction ca : path.getPath()) {
				if (!ca.getConversionOutputType().equals(pathFrame.get(count))) {
					found = false;
					break;
				}
				count++;
			}
			if (found) {
				cpath = path;
				break;
			}
		}
		if (!found) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} else {
			doConvert(response, rr, ege, cpath);
		}
	}

	private void doConvert(HttpServletResponse response,
			ConversionRequestResolver rr, EGE ege, ConversionsPath cpath)
			throws FileUploadException, IOException, RequestResolvingException,
			EGEException, FileNotFoundException, ConverterException,
			ZipException {
		InputStream is = null;
		OutputStream os = null;
		if (ServletFileUpload.isMultipartContent(rr.getRequest())) { 
		    ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iter = upload.getItemIterator(rr.getRequest());
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				if (!item.isFormField()) {
					is = item.openStream();
					applyConversionsProperties(rr.getConversionProperties(), cpath, item.getName());
					// creating temporary data buffer
					DataBuffer buffer = new DataBuffer(0, EGEConstants.BUFFER_TEMP_PATH);
					String alloc = buffer.allocate(is);
					InputStream ins = buffer.getDataAsStream(alloc);
					is.close();
					// input validation - print result if fatal error
					// occurs.
					try {
						ValidationResult vRes = ege.performValidation(ins, cpath.getInputDataType());
						if (vRes.getStatus().equals(ValidationResult.Status.FATAL)) {
							ValidationServlet valServ = new ValidationServlet();
							valServ.printValidationResult(response, vRes);
							try {
								ins.close();
							} finally {
								buffer.removeData(alloc, true);
							}
							return;
						}
					} catch (ValidatorException vex) {
						LOGGER.debug(vex.getMessage());
					} finally {
						try {
							ins.close();
						} catch (Exception ex) {
							// do nothing
						}
					}
					File zipFile = null;
					FileOutputStream fos = null;
					String newTemp = UUID.randomUUID().toString();
					IOResolver ior = EGEConfigurationManager.getInstance().getStandardIOResolver();
					File buffDir = new File(buffer.getDataDir(alloc));
					// Check if there are any images to copy
					if(iter.hasNext()) {
						// Create directory for images
						File images = new File(buffDir + File.separator + imagesDirectory + File.separator);
						images.mkdir();
						File saveTo = null;
						FileItemStream imageItem = null;
						do { // Save all images to that directory
							imageItem = iter.next();
							// when input form for images is not empty, save images
							if(imageItem.getName()!=null && imageItem.getName().length()!=0) {
								saveTo = new File(images + File.separator + imageItem.getName());
								InputStream imgis = imageItem.openStream();
								OutputStream imgos = new FileOutputStream(saveTo);
								byte[] buf = new byte[1024];
								int len; 
								while ((len = imgis.read(buf)) > 0) { 
									imgos.write(buf, 0, len); 
								} 
								imgis.close(); 
								imgos.close(); 
							}
						} while(iter.hasNext());
					}
					zipFile = new File(EGEConstants.BUFFER_TEMP_PATH
							+ File.separator + newTemp + EZP_EXT);
					fos = new FileOutputStream(zipFile);
					ior.compressData(buffDir, fos);
					ins = new FileInputStream(zipFile);
					File szipFile = new File(EGEConstants.BUFFER_TEMP_PATH
							+ File.separator + newTemp + ZIP_EXT);
					fos = new FileOutputStream(szipFile);
					try {
						try {
							ege.performConversion(ins, fos, cpath);
						} finally {
							fos.close();
						}
						boolean isComplex = EGEIOUtils
								.isComplexZip(szipFile);
						response.setContentType(APPLICATION_OCTET_STREAM);
						int dotIndex = item.getName().lastIndexOf(".");					
						String fN = null;						
						if(dotIndex == -1 || dotIndex == 0) fN = item.getName();	
						else fN = item.getName().substring(0, dotIndex);
						if (isComplex) {
							String fileExt;
							if (cpath.getOutputDataType().getMimeType()
									.equals(APPLICATION_MSWORD)) {
								fileExt = DOCX_EXT;
							} else if (cpath.getOutputDataType().getMimeType()
									.equals(APPLICATION_EPUB)) {
								fileExt = EPUB_EXT; 
							} else if (cpath.getOutputDataType().getMimeType()
									.equals(APPLICATION_ODT)) {
								fileExt = ODT_EXT; 
							}else {
								fileExt = ZIP_EXT;
							}
							response.setHeader("Content-Disposition",
									"attachment; filename=\"" + fN + fileExt + "\"");
							FileInputStream fis = new FileInputStream(
									szipFile);
							os = response.getOutputStream();
							try {
								EGEIOUtils.copyStream(fis, os);
							} finally {
								fis.close();
							}
						} else {
							String fileExt = getMimeExtensionProvider()
									.getFileExtension(cpath.getOutputDataType().getMimeType());
							response.setHeader("Content-Disposition",
									"attachment; filename=\"" + fN + fileExt + "\"");
							os = response.getOutputStream();
							EGEIOUtils.unzipSingleFile(new ZipFile(szipFile), os);
						}
					} finally {
						ins.close();
						if (os != null) {
							os.flush();
							os.close();
						}
						buffer.clear(true);
						szipFile.delete();
						if (zipFile != null) {
							zipFile.delete();
						}
					}
				}
			}
		}
	        else {
		    String inputData = URLDecoder.decode(rr.getRequest().getParameter("input"));
		    String fN = rr.getRequest().getParameter("filename");
		    if (inputData == null || inputData.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		    }
		    applyConversionsProperties(rr.getConversionProperties(), cpath, "input");

		    is = new ByteArrayInputStream(inputData.getBytes());
		    DataBuffer buffer = new DataBuffer(0, EGEConstants.BUFFER_TEMP_PATH);
		    String alloc = buffer.allocate(is);
		    InputStream ins = buffer.getDataAsStream(alloc);
		    is.close();
		    File zipFile = null;
		    FileOutputStream fos = null;
		    String newTemp = UUID.randomUUID().toString();
		    IOResolver ior = EGEConfigurationManager.getInstance().getStandardIOResolver();
		    File buffDir = new File(buffer.getDataDir(alloc));
		    // Check if there are any images to copy
		    zipFile = new File(EGEConstants.BUFFER_TEMP_PATH
				       + File.separator + newTemp + EZP_EXT);
		    fos = new FileOutputStream(zipFile);
		    ior.compressData(buffDir, fos);
		    ins = new FileInputStream(zipFile);
		    File szipFile = new File(EGEConstants.BUFFER_TEMP_PATH
					     + File.separator + newTemp + ZIP_EXT);
		    fos = new FileOutputStream(szipFile);
		    try {
			try {
			    ege.performConversion(ins, fos, cpath);
			} finally {
			    fos.close();
			}
			boolean isComplex = EGEIOUtils
			    .isComplexZip(szipFile);
			response.setContentType(APPLICATION_OCTET_STREAM);
			if (isComplex) {
			    String fileExt;
			    if (cpath.getOutputDataType().getMimeType()
				.equals(APPLICATION_MSWORD)) {
				fileExt = DOCX_EXT;
			    } else if (cpath.getOutputDataType().getMimeType()
				       .equals(APPLICATION_EPUB)) {
				fileExt = EPUB_EXT; 
			    } else if (cpath.getOutputDataType().getMimeType()
				       .equals(APPLICATION_ODT)) {
				fileExt = ODT_EXT; 
			    }else {
				fileExt = ZIP_EXT;
			    }
			    response.setHeader("Content-Disposition",
					       "attachment; filename=\"" + fN + fileExt + "\"");
			    FileInputStream fis = new FileInputStream(
								      szipFile);
			    os = response.getOutputStream();
			    try {
				EGEIOUtils.copyStream(fis, os);
			    } finally {
				fis.close();
			    }
			} else {
			    String fileExt = getMimeExtensionProvider()
				.getFileExtension(cpath.getOutputDataType().getMimeType());
			    response.setHeader("Content-Disposition",
					       "attachment; filename=\"" + fN + fileExt + "\"");
			    os = response.getOutputStream();
			    EGEIOUtils.unzipSingleFile(new ZipFile(szipFile), os);
			}
		    } finally {
			ins.close();
			if (os != null) {
			    os.flush();
			    os.close();
			}
			buffer.clear(true);
			szipFile.delete();
			if (zipFile != null) {
			    zipFile.delete();
			}
		    }
		      
		    
		}
	}


	private void applyConversionsProperties(String properties, ConversionsPath cP, String fileName) 
					throws RequestResolvingException {
		if(properties!=null && properties.trim().length()!=0) {
			StringBuffer sb = new StringBuffer();
			sb.append("<properties>");
			sb.append(properties);
			sb.append("<fileInfo name=\"" + fileName + "\"></fileInfo></properties>");		
			ConversionsPropertiesHandler cpp = new ConversionsPropertiesHandler(sb.toString());
			cpp.applyPathProperties(cP);
		} else {// apply empty properties if no properties were provided
			for (int i = 0; i < cP.getPath().size(); i++) {
				cP.getPath().get(i).getConversionActionArguments().setProperties(null);
			}
		}
	}

	/**
	 * Returns local names provider.
	 * 
	 * @return
	 */
	public LabelProvider getLabelProvider() {
		return (LabelProvider) this.getServletContext().getAttribute(
				PreConfig.LABEL_PROVIDER);
	}

	/**
	 * Returns map that contains mapping of mime type to file extension.
	 * 
	 * @return
	 */
	public MimeExtensionProvider getMimeExtensionProvider() {
		return (MimeExtensionProvider) this.getServletContext().getAttribute(
				PreConfig.MIME_EXTENSION_PROVIDER);
	}

}
