package pl.psnc.dl.ege.validator.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import pl.psnc.dl.ege.ExceptionListener;
import pl.psnc.dl.ege.validator.StandardErrorHandler;

/**
 * Validates XML data against referenced external DTD.<br/><br/>
 *  
 * If streamed XML data has no !DOCTYPE declaration, validator adds it by default
 * with specified external reference to DTD and root element.<br/><br/>   
 *  
 * @author mariuszs
 */
public class DTDValidator
	implements XmlValidator
{

	private static final Logger LOGGER = Logger.getLogger(DTDValidator.class);

	/*
	 * DTD declaration systemId 
	 */
	private final String mainDTD;

	/*
	 * Root element for DTD declaration
	 */
	private final String root;


	/**
	 * Default constructor.
	 * 
	 * @param systemId - "systemId" of !DOCTYPE declaration
	 * @param root - root-element of !DOCTYPE declaration
	 */
	public DTDValidator(String systemId, String root)
	{
		if (systemId == null || root == null) {
			throw new IllegalArgumentException();
		}
		this.root = root;
		this.mainDTD = systemId;
	}

	/**
	 * Performs XML stream validation.<br/>
	 * Validation results can be stored within {@link ErrorHandler} implementation
	 * ({@link StandardErrorHandler} by default).
	 * 
	 * @param inputData - streamed XML data.
	 */
	public void validateXml(InputStream inputData, ErrorHandler errorHandler)
		throws SAXException, FileNotFoundException, IOException, Exception
	{
		PipedOutputStream os = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(os);
		ExceptionListenerImpl el = new ExceptionListenerImpl();
		DoctypeFilter filter = new DoctypeFilter(inputData, os, el, errorHandler);
		filter.start();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		try {
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setErrorHandler(errorHandler);
			reader.parse(new InputSource(is));
		}
		catch (ParserConfigurationException ex) {
			throw new SAXException(ex.getMessage());
		}catch (SAXParseException ex){
			filter.join();
			Exception exe = el.throwException();
			if(exe != null){
				throw exe;
			}
		}
	}

	/*
	 * Adds !DOCTYPE declaration to XML stream (if it does not exists)
	 * with validation DTD.
	 */
	private class DoctypeFilter
		extends Thread
	{

		private final InputStream inputStream;

		private final OutputStream outputStream;

		private final ExceptionListener el;
		
		private final ErrorHandler eh;

		public DoctypeFilter(InputStream is, OutputStream os,
				ExceptionListener el, ErrorHandler eh)
		{
			super();
			inputStream = is;
			outputStream = os;
			this.el = el;
			this.eh = eh;
		}


		@Override
		public void run()
		{
			try {
				SAXBuilder sb = new SAXBuilder();
				Document doc = sb.build(inputStream);
				if (doc.getDocType() == null) {
					doc.setDocType(new DocType(root, mainDTD));
				}
				XMLOutputter outp = new XMLOutputter();
				outp.output(doc, outputStream);
				outputStream.flush();
			}
			catch(FileNotFoundException ex){
				el.catchException(ex);
			}
			catch (JDOMException ex) {
				el.catchException(ex);
			}
			catch (IOException ex) {
				LOGGER.error(ex.getMessage(), ex);
				el.catchException(ex);
			}
			finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					}
					catch (IOException ex) {
						LOGGER.error(ex.getMessage());
					}
				}
			}

		}

	}

}
