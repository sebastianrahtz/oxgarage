package uk.ac.ox.oucs.oxgarage.oo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ConversionActionArguments;

/**
 * <p>
 * Configuration for OOConverter
 * </p>
 * 
 * Provides configuration for the OpenOffice Converter, containing path to Open Office home directory and possible conversions families
 * 
 * @author Lukas Platinsky
 * 
 */

public class OOConfiguration {

	// path to the OpenOffice.org directory
	public static final String PATHTOOFFICE;

	// default path to the OpenOffice.org directory
	public static final String DEFAULTPATH = "/usr/lib/openoffice/";

	// list of conversion families
	private static final List<OOConversionsFamily> ConversionFamilies;

	// hashmap of extensions for fast look-up
	private static HashMap<String, String> extensions;

	// list of all possible conversions
	public static List<ConversionActionArguments> CONVERSIONS;

	static {
		ConversionFamilies = OOConversionsFamily.getFamilies();		
		getConversions();
		constructExtensionsMap();
		String pref = OOConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String SLASH = "/";
		String PATH = pref.substring(0, pref.lastIndexOf(SLASH));
		String CONFIG_PATH = PATH + File.separator + "tei-config" + File.separator + "configuration" + File.separator + "OpenOfficeConfig";
		String pathToOffice = null;
		try { 
			Scanner scanner = new Scanner(new File(CONFIG_PATH));
			pathToOffice = scanner.nextLine();			
		} catch(FileNotFoundException e) {
			pathToOffice = DEFAULTPATH;
		} finally {
			PATHTOOFFICE = pathToOffice;
		}
	}

	public static void getConversions () {
		CONVERSIONS = new ArrayList<ConversionActionArguments>();		
		for (OOConversionsFamily family : ConversionFamilies) {
			family.addConversions(CONVERSIONS);
		}
	}

	public static String getExtension(DataType dataType) {
		return extensions.get(dataType.getFormat() + "/" + dataType.getMimeType());
	}

	private static void constructExtensionsMap() {
		extensions = new HashMap<String, String>();
		for (OOConversionsFamily family : ConversionFamilies) {
			family.addExtensions(extensions);
		}
	}
}
