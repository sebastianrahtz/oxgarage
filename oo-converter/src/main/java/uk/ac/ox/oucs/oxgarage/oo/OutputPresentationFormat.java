package uk.ac.ox.oucs.oxgarage.oo;

public enum OutputPresentationFormat {
	
    /*
      supported formats

     Name ("name", "mime type for target", "description", "extension", visible)

     */
	PDF("pdf","application/pdf", "PDF Document", "pdf", true, 4),
	ODP("odp", "application/vnd.oasis.opendocument.presentation", "Open Office (.odp)", "odp", true, 4),
	SXI("sxi", "application/vnd.sun.xml.impress", "OpenOffice.org 1.0 (.sxi)", "sxi", true, 4),
	PPT("ppt", "application/vnd.ms-powerpoint", "Microsoft PowerPoint (.ppt)", "ppt", true, 5),
	SWF("swf", "application/x-shockwave-flash", "Macromedia Flash (.swf)", "swf", true, 6);

	private String name;
	private String mimeType;
	private String description;
	private String extension;
	private boolean visible;
	private int cost;
	
	OutputPresentationFormat(String name, String mimeType, String description, String extension, boolean visible, int cost){
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
