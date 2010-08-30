package pl.psnc.dl.ege.configuration;

import java.io.File;

/**
 * Additional useful static data.
 * 
 * @author mariuszs
 */
public final class EGEConstants {
	
	private EGEConstants(){
	}

	// name for document family consisting of text documents
	public static final String TEXTFAMILY = "Text documents";
	public static final String TEXTFAMILYCODE = "text";

	// name for document family consisting of spreadsheet documents
	public static final String SPREADSHEETFAMILY = "Spreadsheet documents";
	public static final String SPREADSHEETFAMILYCODE = "spreadsheet";

	// name for document family consisting of presentation documents
	public static final String PRESENTATIONFAMILY = "Presentation documents";
	public static final String PRESENTATIONFAMILYCODE = "presentation";

	// default name for documents from unrecognized family
	public static final String DEFAULTFAMILY = "Other documents";
	
	/** User home directory */
	public final static String userHome = System.getProperty("user.home");

	/** OS specific file separator */
	public final static String fS = System.getProperty("file.separator");

	/** Name of the directory where the ege will hold its files */
	public final static String egeDataDirectory = ".ege";

	/** EGE data directory without file separator at the end */
	public static final String FULL_DATA_DIRECTORY = userHome + fS + egeDataDirectory;

	/** EGE extension directory */
	public static final String EGE_EXT_DIRECTORY = FULL_DATA_DIRECTORY + fS + "extensions";
	
	/**
	 * EGE temporary files directory
	 */
	public static final String TEMP_PATH = FULL_DATA_DIRECTORY + fS + "temp";
	
	static {
		File dir = new File(TEMP_PATH);
		if(!dir.exists()){
			dir.mkdir();
		}
	}
	
	/**
	 * EGE data buffer temporary files directory 
	 */
	public static final String BUFFER_TEMP_PATH = TEMP_PATH + fS + "buff";

	/**
	 * Returns appropriate name of text family based on its code name
	 */
	public static String getType(String typeCode) {
		if(typeCode.equals(TEXTFAMILYCODE)) return TEXTFAMILY;
		if(typeCode.equals(SPREADSHEETFAMILYCODE)) return SPREADSHEETFAMILY;
		if(typeCode.equals(PRESENTATIONFAMILYCODE)) return PRESENTATIONFAMILY;
		return DEFAULTFAMILY;
	}

}
