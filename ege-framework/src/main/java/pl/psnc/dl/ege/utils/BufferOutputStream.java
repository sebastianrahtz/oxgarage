package pl.psnc.dl.ege.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.output.DeferredFileOutputStream;

import pl.psnc.dl.ege.utils.DataBuffer.Item;


/*
 * Wrapper class for DefferedFileOutputStream.
 * Commits state of buffer data item, when output stream is closed.
 */
class BufferOutputStream
	extends DeferredFileOutputStream
{

	/* referenced item */
	private Item bufferItem;


	public BufferOutputStream(int threshold, File outputFile, Item itemRef)
	{
		super(threshold, outputFile);
		this.bufferItem = itemRef;
	}


	public BufferOutputStream(int threshold, String prefix, String suffix,
			File directory, Item itemRef)
	{
		super(threshold, prefix, suffix, directory);
		this.bufferItem = itemRef;
	}


	@Override
	public void close()
		throws IOException
	{
		try {
			super.close();
		}
		catch (IOException ex) {
			bufferItem.commit();
			throw ex;
		}
	}

}
