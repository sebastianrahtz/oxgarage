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
 * Class specifying the .docx document transformation operations.
 * </p>
 * Performs transformation from .docx to TEI XML format and vice versa.
 * 
 * @author Lukas Platinsky based on code written by mariuszs
 * 
 */

public class DocXConverter extends ComplexConverter {

	private static final Logger LOGGER = Logger.getLogger(DocXConverter.class);

	/**
	 * Constructs converter for conversion from TEI
	 * 
	 * @param profile String representing the profile name
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public DocXConverter (String profile)
			throws IOException, ConfigurationException {
		super(profile);
		File killFile = new File(tempDirectoryName + File.separator + "word"
						 + File.separator + "webSettings.xml");
		killFile.delete();

		// mangle styles.xml file
		try {
		File oldStyles = new File (tempDirectoryName + File.separator + "word" + File.separator + "styles.xml");
		File newStyles = new File (tempDirectoryName + File.separator + "word" + File.separator + "newstyles.xml");
		Processor proc = SaxonProcFactory.getProcessor();
		XsltCompiler comp = proc.newXsltCompiler();
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator   + "docx" + File.separator + "tools" + File.separator   + "fixstyle.xsl";
		XsltExecutable exec = comp.compile(new StreamSource(stylesheet));
		XsltTransformer transformer = exec.load();
		DocumentBuilder documentBuilder = proc.newDocumentBuilder();
		FileInputStream fis = new FileInputStream(oldStyles);
		transformer.setInitialContextNode(documentBuilder.build(new StreamSource(fis)));
		Serializer result = new Serializer();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(newStyles), "UTF-8"));
		result.setOutputWriter(writer);
		transformer.setDestination(result);
		transformer.transform();
		writer.close();
		oldStyles.delete();
		newStyles.renameTo(oldStyles);		
		} catch (Exception ex) {
		    
		    LOGGER.info("EXCEPTION " + ex);
		}

		// mangle _rels/.rels.xml file
		try {
		File oldDotrels = new File (tempDirectoryName + File.separator + "_rels" + File.separator + ".rels");
		File newDotrels = new File (tempDirectoryName + File.separator + "_rels" + File.separator + "newdotrels");
		Processor proc = SaxonProcFactory.getProcessor();
		XsltCompiler comp = proc.newXsltCompiler();
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator   + "docx" + File.separator + "tools" + File.separator   + "fixdotrels.xsl";
		XsltExecutable exec = comp.compile(new StreamSource(stylesheet));
		XsltTransformer transformer = exec.load();
		DocumentBuilder documentBuilder = proc.newDocumentBuilder();
		FileInputStream fis = new FileInputStream(oldDotrels);
		transformer.setInitialContextNode(documentBuilder.build(new StreamSource(fis)));
		Serializer result = new Serializer();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(newDotrels), "UTF-8"));
		result.setOutputWriter(writer);
		transformer.setDestination(result);
		transformer.transform();
		writer.close();
		oldDotrels.delete();
		newDotrels.renameTo(oldDotrels);		


		/* show files, debug
		String files;
		File folder = new File(tempDirectoryName + File.separator + "word" );
		File[] listOfFiles = folder.listFiles(); 
		
		for (int i = 0; i < listOfFiles.length; i++) 
		    {
			
			if (listOfFiles[i].isFile()) 
			    {
				files = listOfFiles[i].getName();
				LOGGER.info(files);
			    }
		    }
		*/
		} catch (Exception ex) {
		    
		    LOGGER.info("EXCEPTION " + ex);
		}


	}

	/**
	 * Constructs converter for conversion to TEI
	 * 
	 * @param profile String representing the profile name
	 * @param fileName String holding the name of file we are converting
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public DocXConverter (String profile, String fileName)
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
						+ Format.DOCX.getId() + File.separator 
						+ "template.docx";
	}

	/**
	 * Returns array of directories, which we need to copy after conversion
	 */
	protected String[] getDirectoriesToCopy() {
		return new String[] {"word/media", "word/embeddings", "word/fonts"};
	}

	/**
	 * Returns the name of the file containing main content used in conversion into TEI
	 */
	protected String getContentsFileNameToTEI() {
		return "word" + File.separator + "document.xml";
	}

	/**
	 * Returns the name of the file containing main content used in conversion from TEI
	 */
	protected String getContentsFileNameFromTEI() {
		return "word" + File.separator + "document.xml";
	}

	/**
	 * Returns stylesheet for conversion into TEI
	 */
	protected StreamSource getStylesheetToTEI() {
		String stylesheet = new File(ConverterConfiguration.STYLESHEETS_PATH).toString() + File.separator
						+ "profiles" + File.separator
						+ profile + File.separator
						+ Format.DOCX.getId() + File.separator 
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
						+ Format.DOCX.getId() + File.separator 
						+ "to.xsl";
		return new StreamSource(new File(stylesheet));

	}

	/**
	 * Sets all relevant XSLT parameters needed for conversion into TEI
	 */
	protected void setParametersToTEI(XsltTransformer transformer) {
		transformer.setParameter(new QName("word-directory"), new XdmAtomicValue(tempDirectoryNameURI));
	}

	/**
	 * Sets all relevant XSLT parameters needed for conversion from TEI
	 */
	protected void setParametersFromTEI(XsltTransformer transformer) {
		transformer.setParameter(new QName("word-directory"), new XdmAtomicValue(tempDirectoryNameURI));
		transformer.setParameter(new QName("isofreestanding"), new XdmAtomicValue("true"));
	}

	/**
	 * Returns path to the directory containing images
	 */
	protected String getImagesDirectoryName() {
		return "word" + File.separator + "media" + File.separator;
	}

	/**
	 * Returns path to the directory containing images relative to the content of main document file
	 */
	protected String getImagesDirectoryNameRelativeToDocument() {
		return "media" + File.separator;
	}

	@Override
	public void mergeTEI(XdmNode teiDoc) throws SaxonApiException,
			FileNotFoundException, IOException {
		super.mergeTEI(teiDoc);
		
		// remove original core.xml file
		File orgCoreFile = new File(tempDirectoryName + File.separator + "docProps"
				+ File.separator + "core.xml");
		orgCoreFile.delete();

		// move new core.xml
		File newCoreFile = new File(tempDirectoryName + File.separator + "docProps"
				+ File.separator + "newcore.xml");
		newCoreFile.renameTo(orgCoreFile);

		// remove original custom.xml file
		File orgCustomFile = new File(tempDirectoryName + File.separator + "docProps"
				+ File.separator + "custom.xml");
		orgCustomFile.delete();

		// move new custom.xml
		File newCustomFile = new File(tempDirectoryName + File.separator + "docProps"
				+ File.separator + "newcustom.xml");
		newCustomFile.renameTo(orgCustomFile);

		// remove original app.xml file
		File orgAppFile = new File(tempDirectoryName + File.separator + "docProps"
				+ File.separator + "app.xml");
		orgAppFile.delete();

		// move new app.xml
		File newAppFile = new File(tempDirectoryName + File.separator + "docProps"
				+ File.separator + "newapp.xml");
		newAppFile.renameTo(orgAppFile);
	}
}
