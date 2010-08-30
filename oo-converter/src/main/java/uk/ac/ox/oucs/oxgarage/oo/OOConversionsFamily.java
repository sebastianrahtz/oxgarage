package uk.ac.ox.oucs.oxgarage.oo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ConversionActionArguments;
import pl.psnc.dl.ege.configuration.EGEConstants;

/**
 * <p>
 * Configuration for OOConverter
 * </p>
 * 
 * Provides lists of actual conversion families and tools to get all possible conversions and extensions
 * 
 * @author Lukas Platinsky
 * 
 */

public class OOConversionsFamily {

	private static final Logger LOGGER = Logger.getLogger(OOConversionsFamily.class);

	public static OOConversionsFamily TEXT;

	public static OOConversionsFamily SPREADSHEETS;

	public static OOConversionsFamily PRESENTATIONS;

	private final List<OOType> inputs;

	private final List<OOType> outputs;

	static {
		TEXT = new OOConversionsFamily();
		SPREADSHEETS = new OOConversionsFamily();
		PRESENTATIONS = new OOConversionsFamily();

		for(InputTextFormat format : InputTextFormat.values()) {
			TEXT.addInput(new OOType(new DataType(format.getName(), format.getMimeType(), format.getDescription(), EGEConstants.TEXTFAMILY),
					format.getCost()));
		}
		for(OutputTextFormat format : OutputTextFormat.values()) {
			TEXT.addOutput(new OOType(new DataType(format.getName(), format.getMimeType(), 
							format.getDescription(), EGEConstants.TEXTFAMILY),
					format.getCost()));
		}
		for(InputSpreadsheetFormat format : InputSpreadsheetFormat.values()) {
			SPREADSHEETS.addInput(new OOType(new DataType(format.getName(), format.getMimeType(), 
						format.getDescription(), EGEConstants.SPREADSHEETFAMILY),
					format.getCost()));
		}
		for(OutputSpreadsheetFormat format : OutputSpreadsheetFormat.values()) {
			SPREADSHEETS.addOutput(new OOType(new DataType(format.getName(), format.getMimeType(), 
						format.getDescription(), EGEConstants.SPREADSHEETFAMILY),
					format.getCost()));
		}
		for(InputPresentationFormat format : InputPresentationFormat.values()) {
			PRESENTATIONS.addInput(new OOType(new DataType(format.getName(), format.getMimeType(), 
						format.getDescription(), EGEConstants.PRESENTATIONFAMILY),
					format.getCost()));
		}
		for(OutputPresentationFormat format : OutputPresentationFormat.values()) {
			PRESENTATIONS.addOutput(new OOType(new DataType(format.getName(), format.getMimeType(), 
						format.getDescription(), EGEConstants.PRESENTATIONFAMILY),
					format.getCost()));
		}

	}

	public OOConversionsFamily() {
		inputs = new ArrayList<OOType>();
		outputs = new ArrayList<OOType>();
	}

	public void addConversions(List<ConversionActionArguments> conversions) {
		int cost;
		for (OOType in : inputs) {
			for(OOType out : outputs) {
				if(!in.equals(out)) {
					cost = in.cost + out.cost;
					//LOGGER.debug(in.type.toString() + " -> " + out.type.toString() + ": " + cost);
					conversions.add(new ConversionActionArguments(in.type, out.type, "", true, cost));
				}
			}
		}
	}

	public void addExtensions(HashMap<String, String> extensions) {
		for(InputTextFormat format : InputTextFormat.values()) {
			extensions.put(format.toString(), format.getExtension());
		}
		for(OutputTextFormat format : OutputTextFormat.values()) {
			extensions.put(format.toString(), format.getExtension());
		}
		for(InputSpreadsheetFormat format : InputSpreadsheetFormat.values()) {
			extensions.put(format.toString(), format.getExtension());
		}
		for(OutputSpreadsheetFormat format : OutputSpreadsheetFormat.values()) {
			extensions.put(format.toString(), format.getExtension());
		}
		for(InputPresentationFormat format : InputPresentationFormat.values()) {
			extensions.put(format.toString(), format.getExtension());
		}
		for(OutputPresentationFormat format : OutputPresentationFormat.values()) {
			extensions.put(format.toString(), format.getExtension());
		}
	}

	public static List<OOConversionsFamily> getFamilies() {
		List<OOConversionsFamily> families = new ArrayList<OOConversionsFamily>();
		families.add(TEXT);
		families.add(SPREADSHEETS);
		families.add(PRESENTATIONS);
		return families;
	}

	public void addInput(OOType type) {
		inputs.add(type);
	}

	public void addOutput(OOType type) {
		outputs.add(type);
	}

	private static class OOType{
		public final int cost;
		public final DataType type;

		public OOType (DataType type, int cost) {
			this.type = type;
			this.cost = cost;
		}
	}

}
