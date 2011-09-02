package uk.ac.ox.oucs.oxgarage.oo;

public enum InputTextFormat {
	
    /*
      supported formats

     Name ("name", "mime type for target", "description", "extension", visible)

     */
	ODT("odt", "application/vnd.oasis.opendocument.text", "Open Office Text Document (.odt)", "odt", true, 4),
	DOC("doc", "application/msword", "Microsoft Word Document (.doc)", "doc", true, 7),
	DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Microsoft Word Document (.docx)", "docx", true, 7),
	RTF("rtf", "application/rtf", "Rich Text Format (.rtf)", "rtf", true, 6),
	TXT("txt", "text/plain", "Plain Text (.txt)", "txt", true, 5),
	SXW("sxw", "application/vnd.sun.xml.writer", "OpenOffice 1.0 Text Document (.sxw)", "sxw", true, 4),
	HTML("xhtml", "application/xhtml+xml", "xHTML Document", "html", true, 9),  
	WPD("wpd", "application/wordperfect", "WordPerfect Document (.wpd)", "wpd", true, 5);

	private String name;
	private String mimeType;
	private String description;
	private String extension;
	private boolean visible;
	private int cost;
	
	InputTextFormat(String name, String mimeType, String description, String extension, boolean visible, int cost){
		this.name = name;
		this.mimeType = mimeType;
		this.description = description;
		this.extension = extension;
		this.visible = visible;
		this.cost = cost;
	}
	
	public String getName(){
		return name;
	}
	
	public String getMimeType(){
		return mimeType;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getExtension() {
		return extension;
	}

	public boolean getVisible() {
		return visible;
	}
	
	public int getCost() {
		return cost;
	}

	public String toString() {
		return name + "/" + mimeType;
	}
}
