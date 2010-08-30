package pl.psnc.dl.ege.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Interface responsible for compressing and decompressing streamed EGE data.</p>
 * Each implementation can provide its own compression algorithm.<br>
 * It is however vital that sender and receiver should base on the same algorithm.  
 * 
 * 
 * @author mariuszs
 * 
 */
public interface IOResolver {
	
	/**
	 * Method should decompress stream of input data and save it to
	 * selected directory.    
	 * 
	 * @param is
	 * @param dir
	 * @throws IOException
	 */
	public void decompressStream(InputStream is, File dir) throws IOException;
	
	/**
	 * Method should be responsible for compressing output data received from any of
	 * the EGE operations.
	 * 
	 * @param os
	 * @return
	 * @throws IOException
	 */
	public void compressData(File dir, OutputStream os) throws IOException;
	
}
