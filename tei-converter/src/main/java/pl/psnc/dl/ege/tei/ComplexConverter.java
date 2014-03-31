package pl.psnc.dl.ege.tei;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.tei.exceptions.ConfigurationException;
import org.tei.utils.FileUtils;
import org.tei.utils.SaxonProcFactory;
import org.tei.utils.XMLUtils;

import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.configuration.EGEConstants;

import org.apache.log4j.Logger;

/**
 * <p>
 * Abstract Class offering tools for complex conversions involving archive-like formats, e.g. odt and docx
 * </p>
 * Gives possibility to easily implement new transofrations, which are similar to odt and docx 
 * 
 * @author Lukas Platinsky based on code by mariuszs
 * 
 */

public abstract class ComplexConverter {

	private static final Logger LOGGER = Logger.getLogger(ComplexConverter.class);

	protected final String profile;

	protected final String fileName;

	protected File tempDirectory;

	protected String tempDirectoryName;

	protected String tempDirectoryNameURI;

	protected File zipFile;

	protected File teiArchive;

	/**
	 * Defines which directories are copied to and from the archive
	 */
	protected String[] archiveDirectoriesToCopy;

	/**
	 * Constructs converter for conversion from TEI
	 * 
	 * @param profile String representing the profile name
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public ComplexConverter (String profile)
			throws IOException, ConfigurationException {
		this.profile = profile;
		this.fileName = null;
		initTemplate();
	}

	/**
	 * Constructs converter for conversion to TEI
	 * 
	 * @param profile String representing the profile name
	 * @param fileName String holding the name of file we are converting
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public ComplexConverter (String profile, String fileName)
			throws IOException, ConfigurationException {
		this.profile = profile;
		this.fileName = fileName;
		tempDirectory = prepareTempDir();
		tempDirectoryName = tempDirectory.toString();
		tempDirectoryNameURI = tempDirectory.toURI().toString();
	}

	/**
	 * Returns path to the template file
	 */
	protected abstract String getTemplateFile();

	/**
	 * Returns array of directories, which we need to copy after conversion
	 */
	protected abstract String[] getDirectoriesToCopy();

	/**
	 * Returns the name of the file containing main content used in conversion into TEI
	 */
	protected abstract String getContentsFileNameToTEI();

	/**
	 * Returns the name of the file containing main content used in conversion from TEI
	 */
	protected abstract String getContentsFileNameFromTEI();

	/**
	 * Returns stylesheet for conversion into TEI
	 */
	protected abstract StreamSource getStylesheetToTEI();

	/**
	 * Returns stylesheet for conversion from TEI
	 */
	protected abstract StreamSource getStylesheetFromTEI();

	/**
	 * Sets all relevant XSLT parameters needed for conversion into TEI
	 */
	protected abstract void setParametersToTEI(XsltTransformer transformer);

	/**
	 * Sets all relevant XSLT parameters needed for conversion from TEI
	 */
	protected abstract void setParametersFromTEI(XsltTransformer transformer);

	/**
	 * Returns path to the directory containing images
	 */
	protected abstract String getImagesDirectoryName();
	
	/**
	 * Returns path to the directory containing images relative to the main document file
	 */
	protected abstract String getImagesDirectoryNameRelativeToDocument();

	/*
	 * Initialization for transformation into XML - unpacking template file.
	 */
	protected void initTemplate() throws IOException, ConfigurationException {
		// copy template somewhere
		File templateFile = new File(getTemplateFile());
		InputStream in = new FileInputStream(templateFile);
		try {
			unzipData(in);
		} catch (FileNotFoundException e) {
			ConfigurationException ic = new ConfigurationException(
					"Could not load template at: " + getTemplateFile());
			ic.initCause(e);
			throw ic;
		}
		finally {
			in.close();
		}
	}

	/**
	 * Unzips the document file
	 * 
	 * @param in
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected void unzipData(InputStream in) throws FileNotFoundException,
			IOException {
		tempDirectory = prepareTempDir();
		tempDirectoryName = tempDirectory.toString();
		tempDirectoryNameURI = tempDirectory.toURI().toString();
		FileUtils.unzipFile(in, tempDirectory);
	}

	/**
	 * Constructs XML TEI document from the original
	 * 
	 * @param is
	 * @param os
	 * @return
	 * @throws SaxonApiException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void toTEI(InputStream is, OutputStream os)
			throws SaxonApiException, IOException {
		File tmpArchiveDir = prepareTempDir();
		String tmpArchiveDirName = tmpArchiveDir.toString();
		try{
			XdmNode tei = getTEI(is);
			try {
				XMLUtils.storeDocument(tei, new File(tmpArchiveDirName + File.separator + "tei.xml"));
			} catch (IOException ex) {
				throw ex;
			}
			// copy directories
			archiveDirectoriesToCopy = getDirectoriesToCopy();
			for (String dirName : archiveDirectoriesToCopy) {
				File dir = new File(tempDirectoryName + File.separator + dirName);
				if (dir.exists() && dir.isDirectory()) {
					// try to create necessary directories
					if (dirName.indexOf('/') != -1 && !dirName.substring(0, dirName.lastIndexOf('/')).equals("")) {
						File dirToCreate = new File(tmpArchiveDirName + File.separator + 
										dirName.substring(0, dirName.lastIndexOf('/')));
						if (!dirToCreate.isDirectory())
							dirToCreate.mkdirs();
					}
					// copy directory
					try {
						FileUtils.copyDir(dir, new File(tmpArchiveDirName + File.separator
								+ dirName.substring(dirName.lastIndexOf('/'),dirName.length())));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			// pack tmp dir to zip and send it to output stream.
			zipToStream(os, tmpArchiveDir);
		} finally {
			EGEIOUtils.deleteDirectory(tmpArchiveDir);
		}
	}

	/*
	 * Gets xml TEI document from the original document
	 */
	protected XdmNode getTEI(InputStream is) throws FileNotFoundException,
			IOException, SaxonApiException {
	    	FileUtils.unzipFile(is, tempDirectory);
		File dxF = new File(tempDirectoryName + File.separator + getContentsFileNameToTEI());
		Processor proc = SaxonProcFactory.getProcessor();
		net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
		XdmNode document = builder.build(dxF);
		XsltCompiler comp = proc.newXsltCompiler();
		XsltExecutable exec = comp.compile(getStylesheetToTEI());
		XsltTransformer transformer = exec.load();
		setParametersToTEI(transformer);
		if(fileName!=null) transformer.setParameter(new QName("fileName"), new XdmAtomicValue(fileName));
		// transform
		transformer.setInitialContextNode(document);
		XdmDestination result = new XdmDestination();
		transformer.setDestination(result);
		transformer.transform();

		return result.getXdmNode();
	}

	/**
	 * Creates a document from TEI document
	 * 
	 * @param teiDoc
	 */
	public void mergeTEI(XdmNode tei) throws SaxonApiException,
			FileNotFoundException, IOException {
		// prepare transformation
		Processor proc = SaxonProcFactory.getProcessor();
		XsltCompiler comp = proc.newXsltCompiler();
		XsltExecutable exec = comp.compile(getStylesheetFromTEI());
		XsltTransformer transformer = exec.load();
		setParametersFromTEI(transformer);
		// transform and write back to document
		File contentsFile = new File(tempDirectoryName + File.separator + getContentsFileNameFromTEI());
		Serializer result = new Serializer();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(contentsFile), "UTF-8"));
	    try {
		result.setOutputWriter(writer);
		transformer.setInitialContextNode(tei);
		transformer.setDestination(result);
		transformer.transform();
	    }
	    finally {
		writer.close();
	    }
	}

	/**
	 * Packs selected directory into streamed zip archive.
	 * 
	 * @param os
	 * @param dir
	 * @throws IOException
	 */
	public void zipToStream(OutputStream os, File dir) throws IOException {
		ZipOutputStream zipOs = new ZipOutputStream(
				new BufferedOutputStream(os));
		EGEIOUtils.constructZip(dir, zipOs, "");
		zipOs.close();
	}

	public void cleanUp() {
		// delete temporary dir
		EGEIOUtils.deleteDirectory(tempDirectory);

		// delete zip file
		if (null != zipFile && zipFile.exists())
			zipFile.delete();

		// delete archive
		if (null != teiArchive && teiArchive.exists())
			teiArchive.delete();

	}

	public String getDirectoryName() {
		return tempDirectoryName;
	}


	/**
	 * Creates a new temp directory
	 */
	protected File prepareTempDir() {
		String tmpDir = EGEConstants.TEMP_PATH;
		String uid = UUID.randomUUID().toString();
		File inTempDir = new File(tmpDir + File.separator + uid
				+ File.separator);
		inTempDir.mkdir();
		return inTempDir;
	}
}
