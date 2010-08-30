package pl.psnc.dl.ege.validator.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>Standard EGE XML validation interface.</p>
 * Implemented class should receive and possibly record errors through ErrorHandler implementation. 
 * 
 * @author mariuszs
 */
public interface XmlValidator
{
	/**
	 * Performs validation over stream of XML data.
	 * Validation messages and status can be reported through {@link ErrorHandler} implementation.
	 *  
	 * @param errorHandler 
	 * @param inputData
	 * @throws SAXException
	 * @throws IOException
	 */
	public void validateXml(InputStream inputData, ErrorHandler errorHandler) throws SAXException, FileNotFoundException, IOException, Exception;
	
}
