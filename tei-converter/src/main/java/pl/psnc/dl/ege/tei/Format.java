package pl.psnc.dl.ege.tei;


public enum Format {
	
    /*
      supported formats

     Name ("id", "mime type for target", "name in profile", "format name", "input description", "input type", "output description", "output type", "visible as input", "cost")

     */
	LITE("TEI","text/xml","lite","ODDC", "Compiled TEI ODD Document", "text", "ODD documentation as TEI Lite", "text", false, 10),

	ODDHTML("oddhtml","application/xhtml+xml","oddhtml","ODDC", "Compiled TEI ODD Document", "text", "ODD documentation as HTML", "text", false, 5),

	DTD("dtd","application/xml-dtd","dtd","ODDC", "Compiled TEI ODD Document", "text", "DTD created from ODD", "text", false, 10),

	RELAXNG("relaxng","application/xml-relaxng","relaxng","ODDC", "Compiled TEI ODD Document", "text", "RELAXNG schema created from ODD", "text", false, 10),

	XHTML("xhtml","application/xhtml+xml","html","TEI", "TEI P5 XML Document", "text", "xHTML Document", "text", true, 9),

	DOCX("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document","docx","TEI", "TEI P5 XML Document", "text", "Microsoft Word Document (.docx)", "text", true, 9),

	ODT("odt","application/vnd.oasis.opendocument.text","odt","TEI", "TEI P5 XML Document", "text", "Open Office Text Document (.odt)", "text", true, 8),

	LATEX("latex","application/x-latex","latex","TEI", "TEI P5 XML Document", "text", "LaTeX Document", "text", true, 10),
	
	FO("fo","application/xslfo+xml","fo","TEI", "TEI P5 XML Document", "text", "XSL-FO Document", "text", true, 10),

	EPUB("epub","application/epub+zip","epub","TEI", "TEI P5 XML Document", "text", "ePub Document", "text", true, 9);

	private String id;
	private String mimeType;
	private String profile;
	private String formatName;
	private String iDescription;
	private String iType;
	private String oDescription;
	private String oType;
	private boolean visible;
	private int cost;
	
	Format(String id, String mimeType, String profile, String formatName, String iDescription, String iType, String oDescription, String oType, boolean visible, int cost){
		this.id = id;
		this.mimeType = mimeType;
		this.profile = profile;
		this.formatName = formatName;
		this.iDescription = iDescription;
		this.iType = iType;
		this.oDescription = oDescription;
		this.oType = oType;
		this.visible = visible;
		this.cost = cost;
	}
	
	public String getId(){
		return id;
	}
	
	public String getMimeType(){
		return mimeType;
	}
	
	public String getProfile(){
		return profile;
	}
	
	public String getFormatName(){
		return formatName;
	}

	public String getInputDescription() {
		return iDescription;
	}

	public String getInputType() {
		return iType;
	}
	
	public String getOutputDescription() {
		return oDescription;
	}

	public String getOutputType() {
		return oType;
	}


	public boolean getVisible() {
		return visible;
	}

	public int getCost() {
		return cost;
	}
}
