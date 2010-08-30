package pl.psnc.dl.ege.validator.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import pl.psnc.dl.ege.validator.StandardErrorHandler;

/**
 * Performs XML document validation over specified XML schema
 * (according to - W3 XML Schema, specification).
 * 
 * @author mariuszs
 * 
 */
public class SchemaValidator implements XmlValidator
{
	
	private static final Logger LOGGER = Logger.getLogger(SchemaValidator.class);
	
	private final String schemeUrl;
	
	private String defaultUrl = null;
	
	/**
	 * Default constructor.<br> 
	 * Sets {@link StandardErrorHandler} as default error handler.
	 * 
	 * @param schemeUrl - URL reference to XML Scheme
	 */
	public SchemaValidator(String schemeUrl){
		if(schemeUrl == null){
			throw new IllegalArgumentException();
		}
		this.schemeUrl = schemeUrl;
	}
	
	
	public SchemaValidator(String schemeUrl, String defaultUrl){
		this(schemeUrl);
		this.defaultUrl = defaultUrl;	
	}
	
	
	/**
	 * Validates streamed XML data according to XML scheme rules.<br>
	 * Validation result and messages can be stored by error handler
	 * (StandardErrorHandler by default). <br>
	 * 
	 * @param inputData - streamed XML data.
	 * @throws SAXException
	 * @throws {@link IOException}
	 */
	public void validateXml(InputStream inputData, ErrorHandler errorHandler) throws SAXException, IOException, Exception 
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(true);
		try {
			SchemaFactory schemaFactory = SchemaFactory
			.newInstance("http://www.w3.org/2001/XMLSchema");
			URL schemaURL = new URL(schemeUrl);
			//try to download schema by external URL 
			InputStream urlStream = null;
			try{
				urlStream = schemaURL.openStream();
			}catch(IOException ex){
				// in case of any problem use default schema
				if(defaultUrl != null){
					schemaURL = new URL(defaultUrl); 
					urlStream = schemaURL.openStream();
				}else{
					throw ex;
				}
			}
			LOGGER.debug("Uses schema url : " + schemaURL);
			StreamSource sss = new StreamSource(urlStream);
			Schema schema =  schemaFactory.newSchema(sss);
			spf.setSchema(schema);
			SAXParser parser = spf.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setErrorHandler(errorHandler);
			reader.parse(new InputSource(inputData));
		}
		catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
	}
	
	
	
}
