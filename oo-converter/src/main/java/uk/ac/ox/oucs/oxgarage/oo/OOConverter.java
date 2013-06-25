package uk.ac.ox.oucs.oxgarage.oo;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;

import org.tei.exceptions.ConfigurationException;

import pl.psnc.dl.ege.component.Converter;
import pl.psnc.dl.ege.configuration.EGEConfigurationManager;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.types.ConversionActionArguments;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.utils.IOResolver;

/**
 * <p>
 * EGE Converter interface implementation
 * </p>
 * 
 * Provides conversions using OpenOffice installed on the computer
 * <b>Important : </b> the converter expects only compressed data. Data is
 * compressed with standard EGE IOResolver received from
 * EGEConfigurationManager.
 * 
 * @author Lukas Platinsky
 * 
 */

public class OOConverter implements Converter {

	private static final Logger LOGGER = Logger.getLogger(OOConverter.class);

	// Array of portNumbers that the converter can use to launch OpenOffice. 
	// The ones at the beginning are likely to be used more often.
	private static final int[] portNumbers = new int[] {2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011};

	private static final int numberOfPorts = portNumbers.length;

	private static boolean[] busy = new boolean[numberOfPorts];

	private IOResolver ior = EGEConfigurationManager.getInstance().getStandardIOResolver();

	private static OfficeManager officeManager[] = new OfficeManager[numberOfPorts];

	private static OfficeDocumentConverter converter[] = new OfficeDocumentConverter[numberOfPorts];

	private static int waiting = 0;

	private static Object lock = new Object();

	static {
		for(int i=0; i<numberOfPorts; i++) {
			busy[i] = false;
		}
		for(int i=0; i<numberOfPorts; i++) {
			officeManager[i] = null;
		}
		for(int i=0; i<numberOfPorts; i++) {
			converter[i] = null;
		}
	}

	public void convert(InputStream inputStream, OutputStream outputStream,
			final ConversionActionArguments conversionDataTypes)
			throws ConverterException, IOException {
		try {
			transform(inputStream, outputStream, conversionDataTypes.getInputType(),  conversionDataTypes.getOutputType());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConverterException(e.getMessage());
		}
	}	

	public List<ConversionActionArguments> getPossibleConversions() {
		return OOConfiguration.CONVERSIONS;
	}

	private File prepareTempDir() {
		File inTempDir = null;
		String uid = UUID.randomUUID().toString();
		inTempDir = new File(EGEConstants.TEMP_PATH + File.separator + uid
				+ File.separator);
		inTempDir.mkdir();
		return inTempDir;
	}

	/*
	 * prepares received data - decompress, search for file to convert and return it.
	 */
	private File prepareInputData(InputStream inputStream, File inTempDir, String extension)
			throws IOException, ConverterException {
		ior.decompressStream(inputStream, inTempDir);
		File sFile = searchForData(inTempDir, "^.*\\.((?i)"+ extension +")$");
		if (sFile == null) {
			//search for any file
			sFile = searchForData(inTempDir, "^.*");
			if(sFile == null){
				throw new ConverterException("No file data was found for conversion");
			}
		}
		return sFile;
	}

	/*
	 * Search for file specified by regex 
	 */
	private File searchForData(File dir, String regex) {
		for (File f : dir.listFiles()) {
			if (!f.isDirectory() && Pattern.matches(regex, f.getName())) {
				return f;
			} else if (f.isDirectory()) {
				File sf = searchForData(f, regex);
				if (sf != null) {
					return sf;
				}
			}
		}
		return null;
	}

	private void transform(InputStream inputStream, OutputStream outputStream, DataType input, DataType output) 
			throws IOException, ConverterException {
		File inTmpDir = prepareTempDir();
		File outTmpDir = prepareTempDir();
		try {
			String inputExt = OOConfiguration.getExtension(input);
			File inTmpFile = prepareInputData(inputStream, inTmpDir, inputExt);
			File inputFile = new File(inTmpDir + File.separator + "input." + inputExt);			
			inTmpFile.renameTo(inputFile);
			String outputExt = OOConfiguration.getExtension(output);
			File outputFile = new File(outTmpDir + File.separator + "result." + outputExt);
			LOGGER.debug("OOCONVERTER: " + OOConfiguration.PATHTOOFFICE + ": Converting from: " + inputFile.getName() + " to: " + outputFile.getName());
			int portNum = -2;
			try {
				synchronized (this) {
					waiting++;
					while ((portNum = getAvailablePort())==-1) wait();
					waiting--;
					busy[portNum] = true;
					officeManager[portNum] = new DefaultOfficeManagerConfiguration()
							.setOfficeHome(OOConfiguration.PATHTOOFFICE)
							.setPortNumber(portNumbers[portNum]).buildOfficeManager();
					converter[portNum] = new OfficeDocumentConverter(officeManager[portNum]);
					officeManager[portNum].start();		    			
					converter[portNum].convert(inputFile, outputFile);
					officeManager[portNum].stop();
					officeManager[portNum] = null;
					converter[portNum] = null;
					busy[portNum] = false;
					if (waiting>0) notify();			        
				}
			} catch(Exception e) {
				LOGGER.debug("OOConverter Exception " + e.toString());
				e.printStackTrace();
				if(portNum!=-2) {
					officeManager[portNum].stop();
					officeManager[portNum] = null;
					converter[portNum] = null;
					busy[portNum] = false;
				}
				throw new ConverterException(e.getMessage());
			}
			ior.compressData(outTmpDir, outputStream);
		} finally {
			EGEIOUtils.deleteDirectory(inTmpDir);
			EGEIOUtils.deleteDirectory(outTmpDir);
		}
	}

	/**
	 * Returns array index of the first available port number, or -1 if all ports are currently being used for conversions
	 */
	private static int getAvailablePort() {
		int i = 0;
		while (i<numberOfPorts && busy[i]) {
			i++;
		}
		if(i==numberOfPorts) return -1;
		return i;
	}
}
