package pl.psnc.dl.ege.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

/**
 * Pack of usable EGE IO utilities.
 * 
 * TODO : switch to external IO Utilities (e.g. from Vesta or Apache Commons)
 * 
 * @author mariuszs
 */
public final class EGEIOUtils {

	private static final Logger LOGGER = Logger.getLogger(EGEIOUtils.class);

	private EGEIOUtils() {
	}

	private static final int BUFFER = 2048;

	/**
	 * Construct zip file from specified dir location. Result is transfered into
	 * ZipOutputStream.
	 * 
	 * @param file
	 *            directory to pack
	 * @param out
	 *            zip output stream
	 * @param dir
	 *            used for zip entries
	 * @throws IOException
	 */
	public static void constructZip(File file, ZipOutputStream out, String dir)
			throws IOException {
		BufferedInputStream origin = null;
		FileInputStream fi = null;

		byte data[] = new byte[BUFFER];

		File[] files = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				constructZip(files[i], out, dir + files[i].getName() + "/");
				continue;
			}

			// read file
			try {
				fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);

				// create zip entry
				ZipEntry entry = new ZipEntry(dir + files[i].getName());

				// add entries to ZIP file
				out.putNextEntry(entry);

				// write data
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
			} finally {
				if (fi != null) {
					try {
						fi.close();
					} catch (IOException ex) {
						LOGGER.error(ex.getMessage());
					}
				}
				if (origin != null) {
					try {
						origin.close();
					} catch (IOException ex) {
						LOGGER.error(ex.getMessage());
					}
				}
			}
			// close

		}
	}

	/**
	 * Construct zip file from specified dir location. Result is transfered into
	 * ZipOutputStream. The file specified in last parameter is stored as a first entry and uncompressed.
	 * This is needed for creating valid epub files.
	 * 
	 * @param file
	 *            directory to pack

	 * @param out
	 *            zip output stream
	 * @param dir
	 *            used for zip entries
	 * @param mime
	 *            mimetype of archive, necessary for epub format
	 * @throws IOException

	 */
	public static void constructZip(File file, ZipOutputStream out, String dir, File mime)
			throws IOException {
			Scanner scan = new Scanner(mime);
		try {
			byte[] bytes = scan.nextLine().trim().getBytes("UTF-8");
			ZipEntry mimetype = new ZipEntry(mime.getName());
			mimetype.setMethod(ZipOutputStream.STORED);
			mimetype.setSize(bytes.length);
			mimetype.setCompressedSize(mimetype.getSize());
			CRC32 crc = new CRC32();
			crc.update(bytes);
			mimetype.setCrc(crc.getValue());
			out.putNextEntry(mimetype);
			out.write(bytes);
			out.closeEntry();
			mime.delete();
			constructZip(file, out, dir);
		} catch (Exception e) {
			LOGGER.debug("Error in storing mimetype: " + e.toString());
		}
		finally {
			scan.close();
		}
	}

	/**
	 * Deletes a directory.
	 * 
	 * @param dir
	 *            The directory to delete
	 * @return Returns true on success.
	 */
	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory())
			for (String child : dir.list())
				if (!deleteDirectory(new File(dir, child))) {
					return false;
				}
		return dir.delete();
	}

	/**
	 * Perform copy from input stream to output stream.
	 * 
	 * @param is
	 *            source stream
	 * @param os
	 *            result stream
	 * @throws IOException
	 */
	public static void copyStream(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[131072];
		int bytesRead;

		while ((bytesRead = is.read(buffer)) > 0) {
			os.write(buffer, 0, bytesRead);
		}
	}

	/**
	 * Extracts content of the zipFile to given destination directory
	 * 
	 * @param zipFile
	 *            the file to unzip
	 * @param destinationDir
	 *            destination directory for the content of the zipFile
	 * @throws FileNotFoundException
	 *             if a file could not be created
	 * @throws IOException
	 *             if IO error occurs
	 */
	public static void unzipFile(ZipFile zipFile, File destinationDir)
			throws FileNotFoundException, IOException {

		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory()) {

				File file = new File(destinationDir, entry.getName());
				File parentFile = file.getParentFile();
				if (parentFile != null) {
					parentFile.mkdirs();
				}
				InputStream inputStream = zipFile.getInputStream(entry);
				FileOutputStream outputStream = new FileOutputStream(file);

				try {
					copyStream(inputStream, outputStream);
				} finally {
					inputStream.close();
					outputStream.close();
				}
			}
		}
	}

	/**
	 * Unzips a zip compressed file to some output directory.
	 * 
	 * @param in
	 *            The InputStream.
	 * @param outputDir
	 *            The output directory.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void unzipStream(InputStream in, File outputDir)
			throws FileNotFoundException, IOException {
		// name of the directory
		String directoryName = outputDir.getAbsolutePath();

		// buffer
		int BUFFER = 2048;
		// output
		BufferedOutputStream dest;

		// create tmp directory
		if (!outputDir.isDirectory())
			outputDir.mkdirs();

		// read file
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));

		// decompress file
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			// if it is a directory, create it
			if (entry.isDirectory()) {
				File dir = new File(directoryName + File.separator
						+ entry.getName());
				if (!dir.exists())
					dir.mkdirs();
				continue;
			}
			// create directories if necessary
			new File(new File(directoryName + File.separator + entry.getName())
					.getParent()).mkdirs();

			int count;
			byte data[] = new byte[BUFFER];
			// write the files to the disk
			FileOutputStream fos = new FileOutputStream(directoryName
					+ File.separator + entry.getName());
			dest = new BufferedOutputStream(fos, BUFFER);
			try {
			    while ((count = zis.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, count);
			    }
			}
			finally {
			    dest.flush();
			    dest.close();
			}
		}

	}

	/**
	 * Checks if specified zip content contains more than one file.
	 * 
	 * @param zipFile
	 * @return
	 * @throws IOException
	 */
	public static boolean isComplexZip(File zipFile) throws IOException {

		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		try {
			int count = 0;
			ZipEntry zipEntry = null;
			while ((zipEntry = zis.getNextEntry()) != null) {
				if (!zipEntry.isDirectory()) {
					count++;
				}
				if (count > 1) {
					return true;
				}
			}
			return false;

		} finally {
		    zis.close();
		}
	}

	/**
	 * Unpacks single file data to stream. Should be only used when expecting
	 * single file in zip package.
	 * 
	 * @param zipFile
	 * @param os
	 */
	public static void unzipSingleFile(ZipFile zipFile, OutputStream os)
			throws IOException {
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					InputStream is = zipFile.getInputStream(entry);
					try{
						copyStream(is, os);
					}finally{
						is.close();
					}
					return;
				}
			}
		} finally {
			zipFile.close();
		}
	}

}
