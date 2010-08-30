package pl.psnc.dl.ege.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.validator.xml.DTDValidator;
import pl.psnc.dl.ege.validator.xml.SchemaValidator;
import pl.psnc.dl.ege.validator.xml.XmlValidator;

/**
 * Singleton - prepares available XML validators.<br/>
 * Basic configuration of every XML validator is parsed from 'validators.xml'
 * file - provided with this implementation.
 * 
 * @author mariuszs
 */
public class XmlValidatorsProvider extends DefaultHandler {

	/**
	 * URL suffixes - for default DTD`s and Schema`s paths.
	 */
	private static final String EAD_URL_SUFFIX = "!/ead-dtd/ead.dtd";

	private static final String MASTER_URL_SUFFIX = "!/master-dtd/masterx.dtd";

	private static final String TEI_URL_SUFFIX = "!/tei-schema/enrich.xsd";

	/**
	 * Aliases for EGE default addresses to DTD and Schema
	 */
	private static final String TEI_ID = "ege_tei";

	private static final String MASTER_ID = "ege_master";

	private static final String EAD_ID = "ege_ead";

	private static final Logger logger = Logger
			.getLogger(XmlValidatorsProvider.class);

	/*
	 * One XML validator for data type.
	 */
	private final Map<DataType, XmlValidator> xmlValidators = new HashMap<DataType, XmlValidator>();

	/*
	 * Temporary data type parsed from configuration file.
	 */
	private DataType cDataType;

	/**
	 * XML configuration : validators element
	 */
	public static final String T_VALIDATORS = "validators";

	/**
	 * XML configuration : validator element
	 */
	public static final String T_VALIDATOR = "validator";

	/**
	 * XML configuration : format attribute
	 */
	public static final String A_FORMAT = "format";

	/**
	 * XML configuration : mimeType attribute
	 */
	public static final String A_MIME = "mimeType";

	/**
	 * XML configuration : scheme attribute
	 */
	public static final String T_SCHEME = "scheme";

	/**
	 * XML configuration : dtd attribute
	 */
	public static final String T_DTD = "dtd";

	/**
	 * XML configuration : url attribute
	 */
	public static final String A_URL = "url";

	/**
	 * XML configuration : root attribute
	 */
	public static final String A_ROOT = "root";

	/**
	 * XML configuration : systemId attribute
	 */
	public static final String A_SYSTEM_ID = "systemId";

	/**
	 * Informs provider that it has to use default EAD DTD reference (contained
	 * in .jar file most likely)
	 */
	public static final String EGE_EAD = EAD_ID;

	private XmlValidatorsProvider() {
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.parse(new InputSource(this.getClass()
					.getResourceAsStream("/validators.xml")));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/*
	 * Thread safe singleton.
	 */
	private static class XmlValidatorsProviderHolder {
		private static final XmlValidatorsProvider INSTANCE = new XmlValidatorsProvider();
	}

	/**
	 * Provider as singleton - method returns instance.
	 * 
	 * @return
	 */
	public static XmlValidatorsProvider getInstance() {
		return XmlValidatorsProviderHolder.INSTANCE;
	}

	/* TODO : properly handled errors - empty xml attributes etc. */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		try {
			if (localName.equals(T_VALIDATOR)) {
				cDataType = new DataType(attributes.getValue(A_FORMAT),
						attributes.getValue(A_MIME));
			} else {
				if (cDataType == null && !localName.equals(T_VALIDATORS)) {
					throw new SAXException("Unproperly formed validation.xml");
				}
				if (localName.equals(T_DTD)) {
					String systemId = attributes.getValue(A_SYSTEM_ID);
					if (systemId.indexOf(EAD_ID) > -1) {
						systemId = generateAliasURL(EAD_URL_SUFFIX);
					} else if (systemId.equals(MASTER_ID)) {
						systemId = generateAliasURL(MASTER_URL_SUFFIX);
					}
					XmlValidator val = new DTDValidator(systemId, attributes
							.getValue(A_ROOT));
					xmlValidators.put(cDataType, val);
				} else if (localName.equals(T_SCHEME)) {
					String schemaUrl = attributes.getValue(A_URL);
					String defaultUrl = null;
					if (schemaUrl.equals(TEI_ID)) {
						schemaUrl = generateAliasURL(TEI_URL_SUFFIX);
					}
					defaultUrl = generateAliasURL(TEI_URL_SUFFIX);
					XmlValidator val = new SchemaValidator(schemaUrl,
							defaultUrl);
					xmlValidators.put(cDataType, val);
				}
			}
		} catch (Exception ex) {
			throw new SAXException("Configuration errors occured.");
		}
	}

	/*
	 * Retrieves default URLs of aliases from .jar file 'suffix' marks relative
	 * path in .jar to gramma file.
	 */
	private String generateAliasURL(String suffix) {
		StringBuffer sb = new StringBuffer();
		sb.append("jar:file:");
		sb.append(getClass().getProtectionDomain().getCodeSource()
				.getLocation().getFile());
		sb.append(suffix);
		return sb.toString();
	}

	/**
	 * Returns XML validator by selected data type (null if data type is not
	 * supported).
	 * 
	 * @param dataType
	 * @return
	 */
	public XmlValidator getValidator(DataType dataType) {
		return xmlValidators.get(dataType);
	}

	/**
	 * Returns supported data types.
	 * 
	 * @return
	 */
	public List<DataType> getSupportedDataTypes() {
		return new ArrayList<DataType>(xmlValidators.keySet());
	}

}
