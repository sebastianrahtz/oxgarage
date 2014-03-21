package pl.psnc.dl.ege.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * Recovers data from ZIP input/output streams.
 * </p>
 * 
 * @author mariuszs
 */
public class ZipIOResolver implements IOResolver {

	private int level = 1;

	/**
	 * Constructor : level of compression is 1 by default.
	 */
	public ZipIOResolver() {

	}

	/**
	 * Constructor with parameter of level of ZIP compression.
	 * 
	 * @param level
	 */
	public ZipIOResolver(int level) {
		if (level >= 0 && level <= 9) {
			this.level = level;
		}
	}

	public void compressData(File sourceDir, OutputStream os)
			throws IOException {

	    ZipOutputStream zipOs = new ZipOutputStream(
				new BufferedOutputStream(os));
	    try {
		zipOs.setLevel(level);
		EGEIOUtils.constructZip(sourceDir, zipOs, "");}
	    finally {
		zipOs.close();
	    }
	}

	public void decompressStream(InputStream is, File destDir)
			throws IOException {

		EGEIOUtils.unzipStream(is, destDir);

	}

	/**
	 * Returns level of ZIP compression.
	 * 
	 * @return
	 */
	public int getLevel() {
		return Integer.valueOf(level);
	}

}
