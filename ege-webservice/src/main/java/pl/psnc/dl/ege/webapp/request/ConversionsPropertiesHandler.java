package pl.psnc.dl.ege.webapp.request;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import pl.psnc.dl.ege.types.ConversionsPath;

/**
 * Provides SAX parsing functionality - for decoding
 * xml conversion properties.
 * <br/><br/>
 * Translates contained properties into format understandable 
 * for converter. 
 * 
 * @author mariuszs
 *
 */
public class ConversionsPropertiesHandler
	extends DefaultHandler
{

	Map<Integer, Map<String, String>> properties = new HashMap<Integer, Map<String, String>>();

	private Integer currentIndex;

	private String currentId;

	private String fileName;

	/**
	 * Constructor.
	 * 
	 * @param xmlProperties - xml data as String
	 * @throws RequestResolvingException
	 */
	public ConversionsPropertiesHandler(String xmlProperties)
		throws RequestResolvingException
	{
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(this);
			if(xmlProperties != null){
				xmlReader.parse(new InputSource(new ByteArrayInputStream(
					xmlProperties.getBytes())));
			}
		}
		catch (Throwable e) {
			e.printStackTrace();			
			throw new RequestResolvingException(e.getMessage());
		}
	}


	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes)
		throws SAXException
	{
		if (localName.equals("conversion")) {
			currentIndex = Integer.parseInt((attributes.getValue("index")));
			Map<String,String> props = properties.get(currentIndex);
			if(props!=null){
				throw new SAXException("Error reading properties: vertices with same index ("+currentIndex+") found!");
			}
			else{
				props = new LinkedHashMap<String,String>();
				properties.put(currentIndex,props);
			}
		}
		if (localName.equals("property")) {
			currentId = attributes.getValue("id");
		}
		if (localName.equals("fileInfo")) {
			fileName = attributes.getValue("name");
			for(Map<String,String> map : properties.values()) {
				map.put("fileName", fileName);
			}
		}		
	}


	@Override
	public void characters(char buf[], int offset, int len)
		throws SAXException
	{
		StringBuffer val = new StringBuffer();
		for (int i = offset; i < offset + len; i++) {
			switch (buf[i]) {
				case '\\':
					break;
				case '"':
					break;
				case '\n':
					break;
				case '\r':
					break;
				case '\t':
					break;
				default:
					val.append(buf[i]);
					break;
			}
		}
		Map<String, String> props = properties.get(currentIndex);
		props.put(currentId, val.toString());
	}

	/**
	 * Assigns properties to conversions path.<br/>
	 * Trying to assign properties to wrong conversions path will result in exception.
	 * 
	 * @param cp - conversions path
	 * @throws RequestResolvingException
	 */
	public void applyPathProperties(ConversionsPath cp) throws RequestResolvingException
	{
		for (int i = 0; i < cp.getPath().size(); i++) {
			cp.getPath().get(i).getConversionActionArguments().setProperties(
				properties.get(i));
		}
	}

}
