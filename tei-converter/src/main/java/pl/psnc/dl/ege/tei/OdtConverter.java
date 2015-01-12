package pl.psnc.dl.ege.tei;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltTransformer;

import org.tei.exceptions.ConfigurationException;

import org.apache.log4j.Logger;


/**
 * <p>
 * Class specifying the .odt document transformation operations.
 * </p>
 * Performs transformation from .odt to TEI XML format and vice versa.
 * 
 * @author Lukas Platinsky based on code written by mariuszs
 * 
 */

public class OdtConverter extends ComplexConverter {

	private static final Logger LOGGER = Logger.getLogger(OdtConverter.class);

	/**
	 * Constructs converter for conversion from TEI
	 * 
	 * @param profile String representing the profile name
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public OdtConverter (String profile)
			throws IOException, ConfigurationException {
		super(profile);
	}

	/**
	 * Constructs converter for conversion to TEI
	 * 
	 * @param profile String representing the profile name
	 * @param fileName String holding the name of file we are converting
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public OdtConverter (String profile, String fileName)
			throws IOException, ConfigurationException {
		super(profile, fileName);
	}

	/**
	 * Returns path to the template file
	 */
	protected String getTemplateFile() {
		return new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator
						+ "profiles" + File.separator
						+ profile + File.separator
						+ Format.ODT.getId() + File.separator 
						+ "template.odt";
	}

	/**
	 * Returns array of directories, which we need to copy after conversion
	 */
	protected String[] getDirectoriesToCopy() {
		return new String[] {"/Pictures"};
	}

	/**
	 * Returns the name of the file containing main content used in conversion into TEI
	 */
	protected String getContentsFileNameToTEI() {
		return "content.xml";
	}

	/**
	 * Returns the name of the file containing main content used in conversion from TEI
	 */
	protected String getContentsFileNameFromTEI() {
		return "newcontent.xml";
	}

	/**
	 * Returns stylesheet for conversion into TEI
	 */
	protected StreamSource getStylesheetToTEI() {
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator
						+ "profiles" + File.separator
						+ profile + File.separator
						+ Format.ODT.getId() + File.separator 
						+ "from.xsl";
		return new StreamSource(new File(stylesheet));
	}

	/**
	 * Returns stylesheet for conversion from TEI
	 */
	protected StreamSource getStylesheetFromTEI() {
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator
						+ "profiles" + File.separator
						+ profile + File.separator
						+ Format.ODT.getId() + File.separator 
						+ "to.xsl";
		return new StreamSource(new File(stylesheet));

	}

	/**
	 * Sets all relevant XSLT parameters needed for conversion into TEI
	 */
	protected void setParametersToTEI(XsltTransformer transformer) {
		transformer.setParameter(new QName("dir"), new XdmAtomicValue(tempDirectoryNameURI));
	}

	/**
	 * Sets all relevant XSLT parameters needed for conversion from TEI
	 */
	protected void setParametersFromTEI(XsltTransformer transformer) {
		transformer.setParameter(new QName("outputDir"), new XdmAtomicValue(tempDirectoryNameURI));
	}

	/**
	 * Returns path to the directory containing images
	 */
	protected String getImagesDirectoryName() {
		return "Pictures" + File.separator;
	}

	/**
	 * Returns path to the directory containing images relative to the content of main document file
	 */
	protected String getImagesDirectoryNameRelativeToDocument() {
		return "Pictures" + File.separator;
	}

	@Override
	public void mergeTEI(XdmNode teiDoc) throws SaxonApiException,
			FileNotFoundException, IOException {

		super.mergeTEI(teiDoc);
		
		// remove original content.xml file
		File orgContentFile = new File(tempDirectoryName + File.separator + "content.xml");
		orgContentFile.delete();
		// move new content.xml
		File newContentFile = new File(tempDirectoryName + File.separator + "newcontent.xml");
		newContentFile.renameTo(orgContentFile);
	}
}
