package uk.ac.ox.oucs.oxgarage.oo;

public enum InputSpreadsheetFormat {
	
    /*
      supported formats

     Name ("name", "mime type for target", "description", "extension", visible)

     */
	ODS("ods", "application/vnd.oasis.opendocument.spreadsheet", "Open Office (.ods)", "ods", true, 4),
	SXC("sxc", "application/vnd.sun.xml.calc", "OpenOffice.org 1.0 (.sxc)", "sxc", true, 4),
	XLS("xls", "application/vnd.ms-excel", "Microsoft Excel (.xls)", "xls", true, 5),
	XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Microsoft Excel (.xlsx)", "xlsx", true, 5),
	CSV("csv", "text/csv", "Comma-Separated Values (.csv)", "csv", true, 5),
	TSV("tsv", "text/tsv", "Tab-Separated Values (.tsv)", "tsv", true, 5);


	private String name;
	private String mimeType;
	private String description;
	private String extension;
	private boolean visible;
	private int cost;
	
	InputSpreadsheetFormat(String name, String mimeType, String description, String extension, boolean visible, int cost){
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
