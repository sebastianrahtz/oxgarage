package pl.psnc.dl.ege.tei;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import java.util.zip.ZipOutputStream;
import java.io.BufferedOutputStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.s9api.XdmNode;

import org.xml.sax.ErrorHandler;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.tei.exceptions.ConfigurationException;
import org.tei.tei.DocXTransformationProperties;
import org.tei.utils.SaxonProcFactory;

import pl.psnc.dl.ege.component.Converter;
import pl.psnc.dl.ege.configuration.EGEConfigurationManager;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.types.ConversionActionArguments;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.utils.IOResolver;


import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.parse.sax.SAXParseInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.rnc.RncOutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.resolver.Resolver;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * EGE Converter interface implementation
 * </p>
 * 
 * Provides multiple conversions for Enrich TEI format.<br>
 * <b>Important : </b> the converter expects only compressed data. Data is
 * compressed with standard EGE IOResolver received from
 * EGEConfigurationManager.
 * 
 * @author mariuszs
 * 
 */
public class TEIConverter implements Converter,ErrorHandler {
	
	private static final String EX_NO_FILE_DATA_WAS_FOUND = "No file data was found for conversion";

	// List of directories which might contain images for input type
	private static final List<String> imagesInputDirectories = Arrays.asList(new String[] {"media", "Pictures"});
	// List of directories which might contain fonts
	private static final List<String> fontsInputDirectories = Arrays.asList(new String[] {"fonts"});

	private static final Logger LOGGER = Logger.getLogger(TEIConverter.class);

	public static final String DOCX_ERROR = "Probably trying to convert from DocX with wrong input.";

	private IOResolver ior = EGEConfigurationManager.getInstance()
			.getStandardIOResolver();


	public void error(TransformerException exception)
			throws TransformerException {
		LOGGER.info("Error: " + exception.getMessage());
	}


	public void fatalError(TransformerException exception)
			throws TransformerException {
		LOGGER.info("Fatal Error: " + exception.getMessage());
		throw exception;
	}


	public void warning(TransformerException exception)
			throws TransformerException {
		LOGGER.info("Warning: " + exception.getMessage());	
	}


	public void error(SAXParseException exception) throws SAXException {
		LOGGER.info("Error: " + exception.getMessage());
	}


	public void fatalError(SAXParseException exception) throws SAXException {
		LOGGER.info("Fatal Error: " + exception.getMessage());
		throw exception;
	}


	public void warning(SAXParseException exception) throws SAXException {
		LOGGER.info("Warning: " + exception.getMessage());
	}
	public void convert(InputStream inputStream, OutputStream outputStream,
			final ConversionActionArguments conversionDataTypes)
			throws ConverterException, IOException {
		boolean found = false;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		try {
			for (ConversionActionArguments cadt : ConverterConfiguration.CONVERSIONS) {
				if (conversionDataTypes.equals(cadt)) {
					String profile = cadt.getProperties().get(
							ConverterConfiguration.PROFILE_KEY);
					LOGGER.info(dateFormat.format(date) + ": Converting FROM:  "
						    + conversionDataTypes.getInputType().toString()
						    + " TO "
						    + conversionDataTypes.getOutputType().toString()
						    + " WITH profile " + profile );
					convertDocument(inputStream, outputStream, cadt.getInputType(), cadt.getOutputType(),
							cadt.getProperties());
					found = true;
				}
			}
		} catch (ConfigurationException ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new ConverterException(ex.getMessage());
		} catch (SaxonApiException ex) {
			// return wrong docx input message
			if (ex.getMessage() != null
					&& ex.getMessage().contains("FileNotFoundException")
					&& conversionDataTypes.getInputType().getFormat().equals(
							Format.DOCX.getFormatName())
					&& conversionDataTypes.getInputType().getMimeType().equals(
							Format.DOCX.getMimeType())) {
				LOGGER.warn(ex.getMessage(), ex);
				throw new ConverterException(DOCX_ERROR);
			}
			LOGGER.error(ex.getMessage(), ex);
			throw new ConverterException(ex.getMessage());
		}
		if (!found) {
			throw new ConverterException(
					ConverterException.UNSUPPORTED_CONVERSION_TYPES);
		}
	}

	/*
	 * Prepares transformation : based on MIME type.
	 */
	private void convertDocument(InputStream inputStream, OutputStream outputStream,
			DataType fromDataType, DataType toDataType, Map<String, String> properties) throws IOException,
			SaxonApiException, ConfigurationException, ConverterException {
		String toMimeType = toDataType.getMimeType();
		String profile = properties.get(ConverterConfiguration.PROFILE_KEY);

		// from DOCX to TEI
		if (ConverterConfiguration.XML_MIME.equals(toMimeType)
				&& toDataType.getFormat().equals(ConverterConfiguration.TEI)
				&& fromDataType.getFormat().equals(Format.DOCX.getId())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.DOCX
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			transformFromDocX(inputStream, outputStream, profile, properties);
		}
		// from XlSX to TEI
		else if (ConverterConfiguration.XML_MIME.equals(toMimeType)
				&& toDataType.getFormat().equals(ConverterConfiguration.TEI)
				&& fromDataType.getFormat().equals(Format.XLSX.getId())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.XLSX
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			transformFromXlsX(inputStream, outputStream, profile, properties);
		}
		// from HTML to TEI
		else if (ConverterConfiguration.XML_MIME.equals(toMimeType)
				&& toDataType.getFormat().equals(ConverterConfiguration.TEI)
				&& fromDataType.getFormat().equals(Format.XHTML.getId())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.XHTML
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "xml");
			performXsltTransformation(inputStream, outputStream, Format.XHTML.getProfile(), profile, "from", properties);
		}
		// from TEI to DOCX
		else if (Format.DOCX.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.DOCX
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			Processor proc = SaxonProcFactory.getProcessor();
			XsltCompiler comp = proc.newXsltCompiler();
			transformToDocX(inputStream, outputStream, proc, comp, profile, properties);
		}
		// from ODT to TEI
		else if (ConverterConfiguration.XML_MIME.equals(toMimeType)
				&& toDataType.getFormat().equals(ConverterConfiguration.TEI)
				&& fromDataType.getFormat().equals(Format.ODT.getId())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.ODT
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			transformFromOdt(inputStream, outputStream, profile, properties);
		}
		// from TEI to ODT
		else if (Format.ODT.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.ODT
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			Processor proc = SaxonProcFactory.getProcessor();
			XsltCompiler comp = proc.newXsltCompiler();
			transformToOdt(inputStream, outputStream, proc, comp, profile, properties);
		}
		// TEI to HTML for ODD
		else if (Format.ODDHTML.getMimeType().equals(toMimeType)
			 && fromDataType.getFormat().equals(Format.ODDHTML.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.ODDHTML
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "html");
			performXsltTransformation(inputStream, outputStream, Format.ODDHTML
					.getProfile(), profile,"to", properties);
		}
		// TEI to XHTML
		else if (Format.XHTML.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.XHTML
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "html");
			performXsltTransformation(inputStream, outputStream, Format.XHTML
					.getProfile(), profile,"to", properties);
		}
		// TEI to RELAXNG
		else if (Format.RELAXNG.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.RELAXNG
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "rng");
			performXsltTransformation(inputStream, outputStream, Format.RELAXNG
					.getProfile(), profile,"to", properties);
		}
		// TEI to RNC
		else if (Format.RNC.getMimeType().equals(toMimeType)
			 && fromDataType.getFormat().equals(Format.RNC.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.RELAXNG
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "rnc");
			try {
			    generateRngThenTrang(inputStream, outputStream, Format.RELAXNG
							       .getProfile(), profile, properties);
			}
			catch (Exception e) {
				throw new IOException("to RNG then Trang to make RNC failed: " + e.toString());
			}
		}
		// TEI to XSD
		else if (Format.XSD.getMimeType().equals(toMimeType)
			 && fromDataType.getFormat().equals(Format.XSD.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.RELAXNG
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "zip");
			try {
			    generateRngThenTrang(inputStream, outputStream, Format.RELAXNG
							       .getProfile(), profile, properties);
			}
			catch (Exception e) {
				throw new IOException("to RNG then Trang to make XSD failed: " + e.toString());
			}
		}

		// TEI to DTD
		else if (Format.DTD.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.DTD
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "dtd");
			performXsltTransformation(inputStream, outputStream, Format.DTD
					.getProfile(), profile,"to", properties);
		}
		// TEI to LITE
		else if (Format.LITE.getMimeType().equals(toMimeType) 
			 && fromDataType.getFormat().equals(Format.LITE.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.LITE
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "xml");
			performXsltTransformation(inputStream, outputStream, Format.LITE
					.getProfile(), profile,"to", properties);
		}
		// TEI to LATEX
		else if (Format.LATEX.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.LATEX
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "tex");
			performXsltTransformation(inputStream, outputStream, Format.LATEX
					.getProfile(), profile,"to", properties);
		}
		// TEI to ODDJSON
		else if (Format.ODDJSON.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.ODDJSON
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "json");
			performXsltTransformation(inputStream, outputStream, Format.ODDJSON
					.getProfile(), profile,"to", properties);
		}
		// TEI to FO
		else if (Format.FO.getMimeType().equals(toMimeType)) {
				//&& Format.FO.getFormatName().equals(dataType.getFormat())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.FO
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "fo");
			performXsltTransformation(inputStream, outputStream, Format.FO
					.getProfile(), profile,"to", properties);
		}
		// TEI to EPUB
		else if (Format.EPUB.getMimeType().equals(toMimeType)) {
			if (!ConverterConfiguration.checkProfile(profile, Format.EPUB
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			transformToEpub(inputStream, outputStream, profile, Format.EPUB.getProfile(), properties);
		}
		// TEI to TEXT
		else if (Format.TEXT.getMimeType().equals(toMimeType)
			 && fromDataType.getFormat().equals(Format.TEXT.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.TEXT
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "txt");
			performXsltTransformation(inputStream, outputStream, Format.TEXT
					.getProfile(), profile,"to", properties);
		}
		// TEI to identity XML
		else if (Format.XML.getMimeType().equals(toMimeType)
			 && fromDataType.getFormat().equals(Format.XML.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.XML
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "xml");
			performXsltTransformation(inputStream, outputStream, Format.XML
					.getProfile(), profile,"to", properties);
		}
		// TEI to RDF
		else if (Format.RDF.getMimeType().equals(toMimeType)
			 && fromDataType.getFormat().equals(Format.RDF.getFormatName())) {
			if (!ConverterConfiguration.checkProfile(profile, Format.RDF
					.getProfile())) {
				LOGGER.debug(ConverterConfiguration.PROFILE_NOT_FOUND_MSG);
				profile = EGEConstants.DEFAULT_PROFILE;
			}
			properties.put("extension", "rdf");
			performXsltTransformation(inputStream, outputStream, Format.RDF
					.getProfile(), profile,"to", properties);
		}
       
	}

	/*
	 * prepares received data - decompress, search for file to convert and open file stream.
	 */
	private InputStream prepareInputData(InputStream inputStream, File inTempDir)
			throws IOException, ConverterException {
		ior.decompressStream(inputStream, inTempDir);
		File sFile = searchForData(inTempDir, "^.*\\.((?i)xml)$");
		if (sFile == null) {
			//search for any file
			sFile = searchForData(inTempDir, "^.*");
			if(sFile == null){
				throw new ConverterException("No file data was found for conversion");
			}
		}
		FileInputStream fis = new FileInputStream(sFile);
		return fis;
	}

	/*
	 * prepares received data - decompress and open file stream, doesn't search for xml file, it's supplied as argument
	 */
	private InputStream prepareInputData(InputStream inputStream, File inTempDir, File inputFile)
			throws IOException, ConverterException {
		if (inputFile == null) {
			//search for any file
			inputFile = searchForData(inTempDir, "^.*");
			if(inputFile == null){
				throw new ConverterException("No file data was found for conversion");
			}
		}
		FileInputStream fis = new FileInputStream(inputFile);
		return fis;
	}
	
	/*
	 * Search for specified by regex file 
	 */
	private File searchForData(File dir, String regex) {
		for (File f : dir.listFiles()) {
			if (!f.isDirectory() && Pattern.matches(regex, f.getName())) {
				return f;
			} else if (f.isDirectory() &&
				!imagesInputDirectories.contains(f.getName()) &&
				!fontsInputDirectories.contains(f.getName())) {
				File sf = searchForData(f, regex);
				if (sf != null) {
					return sf;
				}
			}
		}
		return null;
	}
	
	private File prepareTempDir() {
		File inTempDir = null;
		String uid = UUID.randomUUID().toString();
		inTempDir = new File(EGEConstants.TEMP_PATH + File.separator + uid
				+ File.separator);
		inTempDir.mkdir();
		return inTempDir;
	}

	/**
	 * Decompress zips containing images
	 */
	private void prepareImages(File imageDir) throws IOException {
		File sFile = searchForData(imageDir, "^.*\\.((?i)zip)$");
		ZipFile zipFile = null;
		File zipOutputDir = null;
		while (sFile!=null) {
			try { 
				zipFile = new ZipFile(sFile);
				
				zipOutputDir = new File(imageDir + File.separator + sFile.getName().replace('.', '-') + File.separator);
				zipOutputDir.mkdir();
				EGEIOUtils.unzipFile(zipFile, zipOutputDir);
				sFile.delete();
				sFile = searchForData(imageDir, "^.*\\.((?i)zip)$");
			}
			catch (Exception e) {
				throw new IOException("Some of the zip archives were damaged: " + e.toString());
			}
		}
	}

	/*
	 * Performs transformation with XSLT 
	 */
	private void performXsltTransformation(InputStream inputStream,
					       OutputStream outputStream, String id, String profile, String direction, Map<String, String> properties)
			throws IOException, SaxonApiException, ConverterException {
		FileOutputStream fos = null;
		InputStream is = null;
		File inTmpDir = null;
		File outTempDir = null;
		File outputDir = null;
		try {
			inTmpDir = prepareTempDir();
			ior.decompressStream(inputStream, inTmpDir);
			// avoid processing files ending in .bin
			File inputFile = searchForData(inTmpDir, "^.*(?<!bin)$");
			if(inputFile!=null) {
			outTempDir = prepareTempDir();
			is = prepareInputData(inputStream, inTmpDir, inputFile);
			Processor proc = SaxonProcFactory.getProcessor();
			XsltCompiler comp = proc.newXsltCompiler();
			// get images and correct graphics tags
			XdmNode initialNode = getImages(inTmpDir.toString(), outTempDir.toString(), "media" + File.separator, 
							"media" + File.separator, inputFile, proc, is, "Xslt", properties);
			String extension = properties.get("extension");
			File resFile = new File(outTempDir + File.separator + "document." + extension);
			fos = new FileOutputStream(resFile);
			XsltExecutable exec = comp.compile(resolveConfiguration(id, comp, profile, direction));
			XsltTransformer transformer = exec.load();
			if(properties.get(ConverterConfiguration.LANGUAGE_KEY)!=null) 
			    {
				XdmAtomicValue doclang = new XdmAtomicValue(properties.get(ConverterConfiguration.LANGUAGE_KEY));
				transformer.setParameter(new QName("lang"), doclang);
				transformer.setParameter(new QName("doclang"), doclang);
				transformer.setParameter(new QName("documentationLanguage"), doclang);
			    }
			setTransformationParameters(transformer, id);
			transformer.setInitialContextNode(initialNode);
			Serializer result = new Serializer();
			result.setOutputStream(fos);
			transformer.setDestination(result);
			transformer.transform();
			ior.compressData(outTempDir, outputStream);
			}
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
				// do nothing
			}
			try {
				fos.close();
			} catch (Exception ex) {
				// do nothing
			}
			if (outTempDir != null && outTempDir.exists())
				EGEIOUtils.deleteDirectory(outTempDir);
			if (inTmpDir != null && inTmpDir.exists())
				EGEIOUtils.deleteDirectory(inTmpDir);
			}
	}


	/*
	 * Performs transformation over XSLT to make RNG schema, then runs trang
	 */
	private void generateRngThenTrang(InputStream inputStream,
							OutputStream outputStream, String id, String profile, Map<String, String> properties)
	    throws IOException, SaxonApiException, ConverterException, InputFailedException, SAXException, OutputFailedException, InvalidParamsException {
		FileOutputStream fos = null;
		InputStream is = null;
		File inTmpDir = null;
		File outTempDir = null;
		File outputDir = null;
		try {
			inTmpDir = prepareTempDir();
			ior.decompressStream(inputStream, inTmpDir);
			File inputFile = searchForData(inTmpDir, "^.*");
			outTempDir = prepareTempDir();
			is = prepareInputData(inputStream, inTmpDir, inputFile);
			Processor proc = SaxonProcFactory.getProcessor();
			XsltCompiler comp = proc.newXsltCompiler();
			// get images and correct graphics tags
			XdmNode initialNode = getImages(inTmpDir.toString(), outTempDir.toString(), "media" + File.separator, 
							"media" + File.separator, inputFile, proc, is, "Xslt", properties);
			String extension = properties.get("extension");
			String realextension = properties.get("extension");
			if (extension.equalsIgnoreCase("zip"))  {
				realextension = "xsd";
			}
			File inFile = new File(outTempDir + File.separator + "document.rng");
			File outFile = new File(outTempDir + File.separator + "document." + realextension);
			fos = new FileOutputStream(inFile);
			XsltExecutable exec = comp.compile(resolveConfiguration(id, comp, profile, "to"));
			XsltTransformer transformer = exec.load();
			if(properties.get(ConverterConfiguration.LANGUAGE_KEY)!=null) 
			    {
				XdmAtomicValue doclang = new XdmAtomicValue(properties.get(ConverterConfiguration.LANGUAGE_KEY));
				transformer.setParameter(new QName("lang"), doclang);
				transformer.setParameter(new QName("doclang"), doclang);
				transformer.setParameter(new QName("documentationLanguage"), doclang);
			    }
			setTransformationParameters(transformer, id);
			transformer.setInitialContextNode(initialNode);
			Serializer result = new Serializer();
			result.setOutputStream(fos);
			transformer.setDestination(result);
			transformer.transform();
			InputFormat inFormat = new SAXParseInputFormat();
			OutputFormat of;
			Resolver resolver =null;
			of = new RncOutputFormat();
			String[] inputParamArray = new String[]{};
			String[] outputParamArray = new String[]{};
			if (extension.equalsIgnoreCase("xsd") || extension.equalsIgnoreCase("zip")) {
			    of = new XsdOutputFormat();
			    outputParamArray = new String[]{"disable-abstract-elements"};
			}
			SchemaCollection sc =  inFormat.load(UriOrFile.toUri(inFile.getAbsolutePath()), inputParamArray, realextension, this,resolver);
			OutputDirectory od = new LocalOutputDirectory( 
								      sc.getMainUri(),
								      outFile,
								      "." + realextension,
								      "UTF-8",
								      72,
								      2
								       );
			of.output(sc, od, outputParamArray, "rng", this);
			try{			    			    
			    if(! inFile.delete()){
				LOGGER.info("Delete operation failed on " + inFile);
			    }
			    
			}catch(Exception e){
			    
			    e.printStackTrace();
			    
			}
			ior.compressData(outTempDir, outputStream);
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
				// do nothing
			}
			try {
				fos.close();
			} catch (Exception ex) {
				// do nothing
			}
			if (outTempDir != null && outTempDir.exists())
				EGEIOUtils.deleteDirectory(outTempDir);
			if (inTmpDir != null && inTmpDir.exists())
				EGEIOUtils.deleteDirectory(inTmpDir);
		}

	}

	/*
	 * Additional parameters for XHTML transformation.
	 */
	private void setTransformationParameters(XsltTransformer transformer,
			String id) {
		if (Format.XHTML.getId().equals(id)) {
			transformer.setParameter(new QName("STDOUT"), new XdmAtomicValue(
					"true"));
			transformer.setParameter(new QName("splitLevel"),
					new XdmAtomicValue("-1"));
			transformer.setParameter(new QName("lang"),
					new XdmAtomicValue("en"));
			transformer.setParameter(new QName("doclang"), new XdmAtomicValue(
					"en"));
			transformer.setParameter(new QName("documentationLanguage"),
					new XdmAtomicValue("en"));
			transformer.setParameter(new QName("institution"),
					new XdmAtomicValue(""));
		}
	}
	

	/*
	 * Performs from XlsX to TEI transformation
	 */
	private void transformFromXlsX(InputStream is, OutputStream os,
			String profile, Map<String, String> properties) throws IOException, SaxonApiException,
			ConfigurationException, ConverterException {
		File tmpDir = prepareTempDir();
		InputStream fis = null;
		String fileName = properties.get("fileName");
		ComplexConverter xlsX = new XlsXConverter(profile, fileName);
		try {
			ior.decompressStream(is, tmpDir);
			// should contain only single file
			File xlsXFile = searchForData(tmpDir, "^.*\\.((?i)xlsx)$");
			if (xlsXFile == null) {
				xlsXFile = searchForData(tmpDir, "^.*");
				if (xlsXFile == null) {
					throw new ConverterException(EX_NO_FILE_DATA_WAS_FOUND);
				}
			}
			fis = new FileInputStream(xlsXFile);
			xlsX.toTEI(fis, os);
		} finally {
			if(fis != null){
				try{
					fis.close();
				}catch(Exception ex){
					// do nothing
				}
			}
			if (tmpDir != null) {
				EGEIOUtils.deleteDirectory(tmpDir);
			}
			if(xlsX != null){
				xlsX.cleanUp();
			}
		}
	}
	/*
	 * Performs from DocX to TEI transformation
	 */
	private void transformFromDocX(InputStream is, OutputStream os,
			String profile, Map<String, String> properties) throws IOException, SaxonApiException,
			ConfigurationException, ConverterException {
		File tmpDir = prepareTempDir();
		InputStream fis = null;
		String fileName = properties.get("fileName");
		ComplexConverter docX = new DocXConverter(profile, fileName);
		try {
			ior.decompressStream(is, tmpDir);
			// should contain only single file
			File docXFile = searchForData(tmpDir, "^.*\\.((?i)doc|(?i)docx)$");
			if (docXFile == null) {
				docXFile = searchForData(tmpDir, "^.*");
				if (docXFile == null) {
					throw new ConverterException(EX_NO_FILE_DATA_WAS_FOUND);
				}
			}
			fis = new FileInputStream(docXFile);
			docX.toTEI(fis, os);
		} finally {
			if(fis != null){
				try{
					fis.close();
				}catch(Exception ex){
					// do nothing
				}
			}
			if (tmpDir != null) {
				EGEIOUtils.deleteDirectory(tmpDir);
			}
			if(docX != null){
				docX.cleanUp();
			}
		}
	}
	
	/*
	 * Performs From TEI to DocX transformation
	 */
	private void transformToDocX(InputStream is, OutputStream os,
			Processor proc, XsltCompiler comp, final String profile, Map<String, String> properties)
			throws IOException, SaxonApiException, ConfigurationException,
			ConverterException {
		File inTmpDir = prepareTempDir();
		File outTmpDir = prepareTempDir();
		ior.decompressStream(is, inTmpDir);
		File inputFile = searchForData(inTmpDir, "^.*");
		InputStream inputStream = prepareInputData(is, inTmpDir, inputFile);
		ComplexConverter docX = null;
		FileOutputStream fos = null;
		try {
			docX = new DocXConverter(profile);
			// get images and correct graphics tags
			XdmNode initialNode = getImages(inTmpDir.toString(), docX.getDirectoryName(), docX.getImagesDirectoryName(), 
						docX.getImagesDirectoryNameRelativeToDocument(), inputFile, proc, inputStream, "toDocx", properties);
			// perform conversion
			// remove files
			docX.mergeTEI(initialNode);
			File oDocXFile = new File(outTmpDir.getAbsolutePath() + File.separator + "result.docx");
			fos = new FileOutputStream(oDocXFile);
			// pack directory to final DocX file
			docX.zipToStream(fos, new File(docX.getDirectoryName()));
			// double compress DocX file anyway
			ior.compressData(outTmpDir, os);
			// clean temporary files
		} finally {
			// perform cleanup
			try{
				inputStream.close();
			}catch(Exception ex){
				// do nothing
			}
			if(fos != null){
				try{
					fos.close();
				}catch(Exception ex){
					// do nothing
				}
			}
			if(docX != null){
				docX.cleanUp();
			}
			EGEIOUtils.deleteDirectory(inTmpDir);
			EGEIOUtils.deleteDirectory(outTmpDir);
		}
	}

	private void transformFromOdt(InputStream is, OutputStream os,
			String profile, Map<String, String> properties) throws IOException, SaxonApiException,
			ConfigurationException, ConverterException {
		File tmpDir = prepareTempDir();
		InputStream fis = null;
		String fileName = properties.get("fileName");
		ComplexConverter odt = new OdtConverter(profile, fileName);
		try {
			ior.decompressStream(is, tmpDir);
			// should contain only single file
			File odtFile = searchForData(tmpDir, "^.*\\.((?i)odt|(?i)ott)$");
			if (odtFile == null) {
				odtFile = searchForData(tmpDir, "^.*");
				if (odtFile == null) {
					throw new ConverterException(EX_NO_FILE_DATA_WAS_FOUND);
				}
			}
			fis = new FileInputStream(odtFile);
			odt.toTEI(fis, os);
		} finally {
			if(fis != null){
				try{
					fis.close();
				}catch(Exception ex){
					// do nothing
				}
			}
			if (tmpDir != null) {
				EGEIOUtils.deleteDirectory(tmpDir);
			}
			if(odt != null){
				odt.cleanUp();
			}
		}
	}

	private void transformToOdt(InputStream is, OutputStream os,
			Processor proc, XsltCompiler comp, final String profile, Map<String, String> properties)
			throws IOException, SaxonApiException, ConfigurationException,
			ConverterException {
		File inTmpDir = prepareTempDir();
		File outTmpDir = prepareTempDir();
		ior.decompressStream(is, inTmpDir);
		File inputFile = searchForData(inTmpDir, "^.*");
		InputStream inputStream = prepareInputData(is, inTmpDir, inputFile);
		ComplexConverter odt = null;
		FileOutputStream fos = null;
		// assign properties
		try {
			odt = new OdtConverter(profile);
			// get images and correct graphics tags
			XdmNode initialNode = getImages(inTmpDir.toString(), odt.getDirectoryName(), odt.getImagesDirectoryName(), 
						odt.getImagesDirectoryNameRelativeToDocument(), inputFile, proc, inputStream, "toOdt", properties);
			// perform conversion
			odt.mergeTEI(initialNode);
			File oOdtFile = new File(outTmpDir.getAbsolutePath() + File.separator + "result.odt");
			fos = new FileOutputStream(oOdtFile);
			// pack directory to final Odt file
			odt.zipToStream(fos, new File(odt.getDirectoryName()));
			// double compress Odt file anyway
			ior.compressData(outTmpDir, os);
			// clean temporary files
		} finally {
			// perform cleanup
			try{
				inputStream.close();
			}catch(Exception ex){
				// do nothing
			}
			if(fos != null){
				try{
					fos.close();
				}catch(Exception ex){
					// do nothing
				}
			}
			if(odt != null){
				odt.cleanUp();
			}
			EGEIOUtils.deleteDirectory(inTmpDir);
			EGEIOUtils.deleteDirectory(outTmpDir);
		}
	}

	public void transformToEpub(InputStream inputStream, OutputStream outputStream,
			final String profile, String id, Map<String, String> properties)
			throws IOException, SaxonApiException, ConfigurationException,
			ConverterException {
		FileOutputStream fos = null;
		InputStream is = null;
		File inTmpDir = null;
		File outTempDir = null;
		File outputDir = null;
		try {
			inTmpDir = prepareTempDir();
			ior.decompressStream(inputStream, inTmpDir);
			File inputFile = searchForData(inTmpDir, "^.*");
			outTempDir = prepareTempDir();
			is = prepareInputData(inputStream, inTmpDir, inputFile);
			Processor proc = SaxonProcFactory.getProcessor();
			XsltCompiler comp = proc.newXsltCompiler();
			// get images and correct graphics tags
			XdmNode initialNode = getImages(inTmpDir.toString(), outTempDir.toString(), "OPS" + File.separator + "media" + 
							File.separator, "media" + File.separator, inputFile, proc, is, "toEpub", properties);
			XsltExecutable exec = comp.compile(resolveConfiguration(id, comp, profile,"to"));
			XsltTransformer transformer = exec.load();
			String dirname = outTempDir.toURI().toString();
			transformer.setParameter(new QName("directory"), new XdmAtomicValue(dirname));
			transformer.setParameter(new QName("outputDir"), new XdmAtomicValue(dirname + File.separator + "OPS" + File.separator));
			File coverTemplate = new File (ConverterConfiguration.STYLESHEETS_PATH + File.separator + "profiles" + File.separator + profile + File.separator + "epub" + File.separator + "cover.jpg");
			if (coverTemplate.exists()) { 
			    String coverOutputDir = outTempDir + File.separator + "OPS" + File.separator;
			    String coverImage = ImageFetcher.generateCover(coverTemplate, coverOutputDir, properties);
			    transformer.setParameter(new QName("coverimage"), new XdmAtomicValue(coverImage));
			}
			setTransformationParameters(transformer, id);
			transformer.setInitialContextNode(initialNode);
			Serializer result = new Serializer();
			transformer.setDestination(result);
			transformer.transform();
			outputDir = prepareTempDir();
			File oEpubFile = new File(outputDir.getAbsolutePath() + File.separator + "result.epub");
			fos = new FileOutputStream(oEpubFile);
			// pack directory to final Epub file
			ZipOutputStream zipOs = new ZipOutputStream(
				new BufferedOutputStream(fos));
			// zip it with mimetype on first position and uncompressed
			try {
			    File mimetype = new File(outTempDir + File.separator + "mimetype");
			    EGEIOUtils.constructZip(outTempDir, zipOs, "", mimetype);
			}
			finally 
			    {
				zipOs.close();
			    }
			// double compress epub file anyway
			ior.compressData(outputDir, outputStream);
			// clean temporary files
		}
		finally {
			try {
				is.close();
			} catch (Exception ex) {
				// do nothing
			}
			if(fos != null){
				try{
					fos.close();
				}catch(Exception ex){
					// do nothing
				}
			}
			if (outTempDir != null && outTempDir.exists())
				EGEIOUtils.deleteDirectory(outTempDir);
			if (inTmpDir != null && inTmpDir.exists())
				EGEIOUtils.deleteDirectory(inTmpDir);
			if (outputDir != null && outputDir.exists())
				EGEIOUtils.deleteDirectory(outputDir);
		}
	}

	private XdmNode getImages(String inputTempDir, String outputTemp, String outputImgDir, String imgDirRelativeToDoc, 
					File inputFile, Processor proc, InputStream is, String conversion, Map<String,String> properties)
			throws IOException, SaxonApiException, ConverterException {
		File inputImages = null;
		boolean getImages = true;
		boolean downloadImages = true;
		boolean textOnly = false;
		if(properties.get(ConverterConfiguration.IMAGES_KEY)!=null) 
			getImages = properties.get(ConverterConfiguration.IMAGES_KEY).equals("true");
		if(properties.get(ConverterConfiguration.FETCHIMAGES_KEY)!=null)
			downloadImages = properties.get(ConverterConfiguration.FETCHIMAGES_KEY).equals("true");
		if(properties.get(ConverterConfiguration.TEXTONLY_KEY)!=null) 
			textOnly = properties.get(ConverterConfiguration.TEXTONLY_KEY).equals("true");
		for(String imageDir : imagesInputDirectories) {
			inputImages = new File(inputTempDir + File.separator + imageDir + File.separator);			
			if(inputImages.exists()) {
				// there are images to copy
				prepareImages(inputImages);
				File outputImages = new File(outputTemp + File.separator + outputImgDir);
				outputImages.mkdirs();
				return ImageFetcher.getChangedNode(inputFile, outputImgDir, imgDirRelativeToDoc, 
									inputImages, outputImages, conversion, 
									properties);
			}
		}
		File outputImages = new File(outputTemp + File.separator + outputImgDir);
		outputImages.mkdirs();		
		return ImageFetcher.getChangedNode(inputFile, outputImgDir, imgDirRelativeToDoc, 
								null, outputImages, conversion, 
								properties);
	}

	/*
	 * Setups new URIResolver for XSLT compiler and returns StreamSource of XSL
	 * transform scheme.
	 */
	private StreamSource resolveConfiguration(final String id, 
						  XsltCompiler comp, String profile, String direction) throws IOException {
		comp.setURIResolver(TEIConverterURIResolver
				.newInstance(ConverterConfiguration.STYLESHEETS_PATH + File.separator + "profiles" + File.separator
						+ profile + File.separator + id));
		return new StreamSource(new FileInputStream(new File(
				ConverterConfiguration.STYLESHEETS_PATH + File.separator + "profiles"
						+ File.separator + profile + File.separator + id
						+ File.separator + direction + ".xsl")));
	}

	public List<ConversionActionArguments> getPossibleConversions() {
		return (List<ConversionActionArguments>) ConverterConfiguration.CONVERSIONS;
	}
}
