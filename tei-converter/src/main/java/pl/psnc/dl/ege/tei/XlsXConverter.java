package pl.psnc.dl.ege.tei;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Writer;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.QName;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import org.tei.utils.SaxonProcFactory;
import org.tei.utils.FileUtils;
import org.tei.utils.XMLUtils;

import org.tei.exceptions.ConfigurationException;

import org.apache.log4j.Logger;

/**
 * <p>
 * Class specifying the .xlsx document transformation operations.
 * </p>
 * Performs transformation from .xlsx to TEI XML format and vice versa.
 * 
 * @author Sebastian Rahtz based on code written by mariuszs
 * 
 */

public class XlsXConverter extends ComplexConverter {

	private static final Logger LOGGER = Logger.getLogger(XlsXConverter.class);

	/**
	 * Constructs converter for conversion to TEI
	 * 
	 * @param profile String representing the profile name
	 * @param fileName String holding the name of file we are converting
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public XlsXConverter (String profile, String fileName)
			throws IOException, ConfigurationException {
		super(profile, fileName);
	}

	/**
	 * Constructs converter for conversion from TEI
	 * 
	 * @param profile String representing the profile name
	 * @throws IOException
	 * @throws ConfigurationException
	 */
        public XlsXConverter (String profile)
	throws IOException, ConfigurationException {
		super(profile);
	}

	/**
	 * Returns the name of the file containing main content used in conversion from TEI
	 */
	protected String getContentsFileNameFromTEI() {
		return "_rels" + File.separator + ".rels";
	}
	protected String getContentsFileNameToTEI() {
		return "_rels" + File.separator + ".rels";
	}
    
        protected String[] getDirectoriesToCopy() {
		return new String[] {};
	}
	protected String getTemplateFile() { return "";	}

	/**
	 * Returns stylesheet for conversion into TEI
	 */
	protected StreamSource getStylesheetToTEI() {
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator
						+ "profiles" + File.separator
						+ profile + File.separator
						+ Format.XLSX.getId() + File.separator 
						+ "from.xsl";
		return new StreamSource(new File(stylesheet));
	}


	/**
	 * Sets all relevant XSLT parameters needed for conversion into TEI
	 */
	protected void setParametersToTEI(XsltTransformer transformer) {
		transformer.setParameter(new QName("workDir"), new XdmAtomicValue(tempDirectoryNameURI));
		transformer.setParameter(new QName("inputDir"), new XdmAtomicValue(tempDirectoryNameURI));
	}
       protected StreamSource getStylesheetFromTEI() { 
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator
						+ "profiles" + File.separator
						+ profile + File.separator
						+ Format.XLSX.getId() + File.separator 
						+ "from.xsl";
		return new StreamSource(new File(stylesheet));
       }
       protected String getImagesDirectoryNameRelativeToDocument() {  return "";}
       protected String getImagesDirectoryName() {  return "";}
       protected void setParametersFromTEI(XsltTransformer transformer) {}

}
