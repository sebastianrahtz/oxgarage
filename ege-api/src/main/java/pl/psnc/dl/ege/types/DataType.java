package pl.psnc.dl.ege.types;

/**
 * EGE Input/Output data type contains
 * format name, e.g. <b>ENRICH TEI P5, TEI P4/MASTER</b> connected with
 * specified MIME Type, e.g. <b>text/html, application/pdf</b> etc.
 * 
 * @author mariuszs
 */
public class DataType implements Comparable {
	
	private final String format;
	private final String mimeType;
	private final String description;
	private final String documentFamily;
	
	/**
	 * Constructor
	 * 
	 * @param format String value, e.g. ENRICH TEI P5
	 * @param mimeType MIME type as String e.g. text/xml
	 */
	public DataType(String format, String mimeType){
		if(format == null || mimeType == null){
			throw new IllegalArgumentException();
		}
		this.format = format;
		this.mimeType = mimeType;
		description = format + "," + mimeType;
		documentFamily = "Document formats";
	}

	
	/**
	 * Constructor
	 * 
	 * @param format String value, e.g. ENRICH TEI P5
	 * @param mimeType MIME type as String e.g. text/xml
	 * @param description Description of a format, e.g. Microsoft Word Document
	 */
	public DataType(String format, String mimeType, String description){
		if(format == null || mimeType == null){
			throw new IllegalArgumentException();
		}
		this.format = format;
		this.mimeType = mimeType;
		this.description = description;
		documentFamily = "Document formats";
	}

	/**
	 * Constructor
	 * 
	 * @param format String value, e.g. ENRICH TEI P5
	 * @param mimeType MIME type as String e.g. text/xml
	 * @param description Description of a format, e.g. Microsoft Word Document
	 */
	public DataType(String format, String mimeType, String description, String documentFamily){
		if(format == null || mimeType == null){
			throw new IllegalArgumentException();
		}
		this.format = format;
		this.mimeType = mimeType;
		this.description = description;
		this.documentFamily = documentFamily;
	}


	
	/**
	 * Returns format name, e.g. ENRICH TEI P5
	 * @return format name as String
	 */
	public String getFormat(){
		return format;
	}
	
	/**
	 * Return MIME type, e.g. <code>text/xml</code> or <code>application/pdf</code>
	 * @return MIME type as String
	 */
	public String getMimeType(){
		return mimeType;
	}

	/**
	 * Return description of the format
	 * @return description as String
	 */
	public String getDescription(){
		return description;
	}

	@Override
	/**
	 * Method returns <b>'false'</b> if:
	 * <ol>
	 * <li>input object is null</li>
	 * <li>input object is not an instance of IOTypedef</li>
	 * <li>either format or mimeType of both objects are not equal</li>
	 * </ol>
	 */
	public boolean equals(Object typedef) {
		if(this == typedef){
			return true;
		}
		if(typedef == null){
			return false;
		}
		if(!(typedef instanceof DataType)){
			return false;
		}
		DataType conv = (DataType)typedef;
		if((conv.getFormat() != null && this.format != null && conv.getFormat().equals(this.format)) && 
				(conv.getMimeType() != null && this.mimeType != null && conv.getMimeType().equals(this.getMimeType()))){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 7;
		hashCode = hashCode + (null == format ? 0 : format.hashCode());
		hashCode = hashCode + (null == mimeType ? 0 : mimeType.hashCode());
		return hashCode;
	}
	
	
	@Override
	public String toString() {
		//return format + "," + mimeType;
		return documentFamily + ':' + description + ':' + format + "," + mimeType;
	}

	public int compareTo(Object d) {
		return toString().compareToIgnoreCase(d.toString());
	}
	
}	

