package pl.psnc.dl.ege;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.QName;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.MultiXslOutputResolver;
import pl.psnc.dl.ege.component.ConfigurableConverter;
import pl.psnc.dl.ege.configuration.EGEConfigurationManager;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.types.ConversionActionArguments;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.utils.IOResolver;

/**
 * <p>
 * Converter based on xsl transformations.
 * </p>
 * Each extension instance provides one transformation possibility.<br>
 * <b>Important : </b> the converter expects only compressed data. Data is
 * compressed with standard EGE IOResolver received from
 * EGEConfigurationManager.
 * 
 * @author mariuszs
 */
public class MultiXslConverter implements ConfigurableConverter {

	private static final String EAD = "EAD";
	
	private static final String MASTER = "MASTER";

	private static final String STYLESHEETS_PATH;

	static {
		STYLESHEETS_PATH = EGEConstants.TEIROOT + "stylesheet" + File.separator;
	}
	
	/*
	 * URI of resource - xsl transformation scheme.
	 */
	private URI xslUri;

	private URI defaultUri = null;

	private final IOResolver ior = EGEConfigurationManager.getInstance()
			.getStandardIOResolver();

	/*
	 * List of possible conversions.
	 */
	private List<ConversionActionArguments> possibleConversions = new ArrayList<ConversionActionArguments>();

	public void convert(InputStream inputStream, OutputStream outputStream,
			ConversionActionArguments conversionDataTypes)
			throws ConverterException, IOException {
		if (!isSupported(conversionDataTypes)) {
			throw new ConverterException(
					ConverterException.UNSUPPORTED_CONVERSION_TYPES);
		}
		File inTempDir = prepareTempDir();
		File outTempDir = null;
		try {
			if (conversionDataTypes.getInputType().getFormat().equals(EAD)) {
				outTempDir = performEADTransformation(prepareInputData(
						inputStream, inTempDir));
			} else {
				outTempDir = performStandardTransformation(prepareInputData(
						inputStream, inTempDir));
			}
			ior.compressData(outTempDir, outputStream);
		} catch (ZipException ex) {
			throw new ConverterException(
					"Error during conversion unzipping : probably wrong input data.");
		} catch (SaxonApiException ex) {
			ex.printStackTrace();
			throw new ConverterException(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ConverterException(ex.getMessage());
		} finally {
			if (inTempDir != null && inTempDir.exists())
				EGEIOUtils.deleteDirectory(inTempDir);
			if (outTempDir != null && outTempDir.exists())
				EGEIOUtils.deleteDirectory(outTempDir);
		}
	}

	/*
	 * Creates temporary directory with UUID random name.
	 */
	private File prepareTempDir() {
		File inTempDir = null;
		String uid = UUID.randomUUID().toString();
		inTempDir = new File(EGEConstants.TEMP_PATH + File.separator + uid
				+ File.separator);
		inTempDir.mkdir();
		return inTempDir;
	}

	/*
	 * prepares received data - decompress and open file stream.
	 */
	private InputStream prepareInputData(InputStream inputStream, File inTempDir)
			throws IOException, ConverterException {
		ior.decompressStream(inputStream, inTempDir);
		// perform transform
		File sFile = searchForData(inTempDir, "^.*\\.((?i)xml)$");
		if (sFile == null) {
			// search for any file
			sFile = searchForData(inTempDir, "^.*");
			if (sFile == null) {
				throw new ConverterException(
						"No file data was found for conversion!");
			}
		}
		FileInputStream fis = new FileInputStream(sFile);
		return fis;
	}

	private File searchForData(File dir, String regex) {
		for (File f : dir.listFiles()) {
			if (!f.isDirectory() && Pattern.matches(regex, f.getName())) {
				return f;
			} else if (f.isDirectory() && !f.getName().equals("images")) {
				File sf = searchForData(f, regex);
				if (sf != null) {
					return sf;
				}
			}
		}
		return null;
	}

	/*
	 * Performs EAD conversions - result contains many files packed to .zip
	 * archive
	 */
	private File performEADTransformation(InputStream inputStream)
			throws SaxonApiException, IOException, ConverterException {
		InputStream is = null;
		try {
			Processor proc = new Processor(false);
			XsltCompiler comp = proc.newXsltCompiler();
			try{
				is = new FileInputStream(new File(xslUri));
			}catch(IOException ex){
				if(defaultUri != null){
					URL xslURL = defaultUri.toURL();
					is = xslURL.openStream();
				}else{
					throw ex;
				}
			}
			// create temporary files directory
			String uid = UUID.randomUUID().toString();
			File tempDir = new File(System.getProperty("java.io.tmpdir")
					+ File.separator + uid + File.separator);
			tempDir.mkdir();

			// setup xslt processor
			proc.getUnderlyingConfiguration().setOutputURIResolver(
					new MultiXslOutputResolver(uid));
			XsltExecutable exec = comp.compile(new StreamSource(is));
			XsltTransformer transformer = exec.load();
			transformer.setInitialContextNode(proc.newDocumentBuilder().build(
					new StreamSource(inputStream)));
			Serializer result = new Serializer();

			// create dummy result file - result file is empty
			File dummyResult = new File(tempDir.getPath() + ".xml");
			FileOutputStream dummyOs = new FileOutputStream(dummyResult);

			result.setOutputStream(dummyOs);
			transformer.setDestination(result);
			transformer.transform();
			dummyOs.close();
			dummyResult.delete();

			return tempDir;

		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					// do nothing
				}
			}
			try {
				inputStream.close();
			} catch (Exception ex) {
				// do nothing
			}
		}
	}

	/*
	 * Performs standard xslt conversion.
	 */
	private File performStandardTransformation(InputStream inputStream)
			throws IOException, SaxonApiException, SAXNotRecognizedException,
			SAXNotSupportedException {
		InputStream is = null;
		File tempDir = null;
		File inTmpDir = null;
		FileOutputStream fos = null;
		try {
			String uid = UUID.randomUUID().toString();
			tempDir = new File(EGEConstants.TEMP_PATH + File.separator + uid
					+ File.separator);
			tempDir.mkdir();
			File standXml = new File(tempDir + File.separator + "stand.xml");
			fos = new FileOutputStream(standXml);
			Processor proc = new Processor(false);
			proc.getUnderlyingConfiguration().setValidation(false);
			proc.getUnderlyingConfiguration().getSourceParser()
					.setErrorHandler(null);
			XsltCompiler comp = proc.newXsltCompiler();
			/* System.err.println("READ XSL " + xslUri);*/
			try{
				is = new FileInputStream(new File(xslUri));
			}catch(IOException ex){
				if(defaultUri != null){
					URL xslURL = defaultUri.toURL();
					is = xslURL.openStream();
				}else{
					throw ex;
				}
			}
			XsltExecutable exec = comp.compile(new StreamSource(is));
			XsltTransformer transformer = exec.load();
			DocumentBuilder documentBuilder = proc.newDocumentBuilder();
			documentBuilder.setDTDValidation(false);
			// write the file			
			inTmpDir = prepareTempDir();
			File inputFile = new File(inTmpDir + File.separator + "input");
			OutputStream tempOut = new FileOutputStream(inputFile); 
			byte[] buf = new byte[1024]; 
			int len; 
			while ((len = inputStream.read(buf)) > 0) { 
				tempOut.write(buf, 0, len); 
			} 
			tempOut.flush();
			tempOut.close();
			//create input stream from the file
			FileInputStream fis = new FileInputStream(inputFile);
			try {
				transformer.setInitialContextNode(documentBuilder.build(new StreamSource(fis)));
				transformer.setParameter(new QName("directory"), new XdmAtomicValue(tempDir.toString() + File.separator));
			} catch(SaxonApiException ex) {
				// document is not an xml document - used for csv to tei conversion
				ex.printStackTrace();
				transformer.setInitialTemplate(new QName("main"));
				transformer.setParameter(new QName("input-uri"), new XdmAtomicValue(inputFile.toString()));
			}
			transformer.setParameter(new QName("configDirectory"), new XdmAtomicValue(EGEConstants.TEIROOT));
			Serializer result = new Serializer();
			result.setOutputStream(fos);
			transformer.setDestination(result);
			transformer.transform();
			if(standXml.length()==0) standXml.delete();
			return tempDir;
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (is != null) {
				is.close();
			}
			if (inTmpDir != null && inTmpDir.exists())
				EGEIOUtils.deleteDirectory(inTmpDir);
			inputStream.close();
		}
	}

	public List<ConversionActionArguments> getPossibleConversions() {
		return possibleConversions;
	}

	private boolean isSupported(ConversionActionArguments cadtCheck) {
		for (ConversionActionArguments cadt : possibleConversions) {
			if (cadt.equals(cadtCheck)) {
				return true;
			}
		}
		return false;
	}

	public void configure(Map<String, String> params) throws EGEException {
		try {
			xslUri = new File(STYLESHEETS_PATH + File.separator +params.get("xsluri")).toURI();
			String iFormat = params.get("iFormat");
			StringBuffer sb = new StringBuffer();
			sb.append("jar:file:");
			sb.append(getClass().getProtectionDomain().getCodeSource()
					.getLocation().getFile());
			// default xsl
			if (EAD.equals(iFormat)) {
				sb.append("!/ead2enrich/ead2enrich.xsl");
				defaultUri = new URI(sb.toString());
			} else if (MASTER.equals(iFormat)) {
				sb.append("!/master2enrich/master2enrich.xsl");
				defaultUri = new URI(sb.toString());
			}
			String iMIME = params.get("iMimeType");
			String iDescription = params.get("iDescription");
			String iType = EGEConstants.getType(params.get("iType"));
			String oFormat = params.get("oFormat");
			String oMIME = params.get("oMimeType");
			String oDescription = params.get("oDescription");
			String oType = EGEConstants.getType(params.get("oType"));
			boolean visibility = params.get("visibility").equals("true");
			int cost = Integer.parseInt(params.get("cost"));
			ConversionActionArguments cadt = new ConversionActionArguments(
					new DataType(iFormat, iMIME, iDescription, iType), 
					new DataType(oFormat, oMIME, oDescription, oType), visibility, cost);
			possibleConversions.add(cadt);
		} catch (NullPointerException ex) {
			throw new EGEException(EGEException.WRONG_CONFIGURATION
					+ " 'null' value of parameter.");
		} catch (Exception ex) {
			throw new EGEException(EGEException.WRONG_CONFIGURATION + " "
					+ ex.getMessage());
		}
	}

}
