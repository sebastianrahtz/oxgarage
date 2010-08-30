package pl.psnc.dl.ege.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.log4j.Logger;

import pl.psnc.dl.ege.EGEImpl;

/**
 * Keeps collection of buffered byte data, which is used mainly by conversion
 * with validation process.<br/><br/> 
 * 
 * Each singular data item can be kept in memory - if it does not reaches specified
 * threshold (in number of bytes). If it does DataBuffer is permitted to save data
 * as temporary file.<br/><br/>
 * 
 * <b>Important:</b> while buffer is referenced it is not cleared from data;
 * assigning new items indefinitely may result in stack overflow.<br/>
 * Memory can be relieved by class methods.<br/>
 * 
 * Each allocation in buffer is of type : single-write/multiple-read.<br/>
 * 
 *    
 * @author mariuszs
 *
 */
/*
 * TODO : Przerobki - nie zwracac 'id' alokacji, ale Item. Pozwolic na wielokrotny zapis (?).
 */
public class DataBuffer
{

	private static final Logger LOGGER = Logger.getLogger(DataBuffer.class);

	/**
	 * Default value : max size of item (in number of bytes), that allows to keep it in memory.
	 */
	public static final int DEFAULT_ITEM_MAX_SIZE = 102400;

	/*
	 * List of buffered items
	 */
	private Map<String, Item> items = new HashMap<String, Item>();

	/*
	 * Maximum size of data item (in number of bytes)
	 */
	private int itemMaxSize = DEFAULT_ITEM_MAX_SIZE;

	/*
	 * Directory for temporary files.
	 */
	private String tmpDirectory;

	/*
	 * Tracker of temporary files - which are deleted, when reference to data item is dropped.
	 */
	private final FileCleaningTracker tracker = new FileCleaningTracker();


	/**
	 * Creates instance of data buffer with specified temporary files directory
	 * - where overwhelmed data is kept. 
	 * 
	 * @param tmpDirectory temporary files directory
	 */
	public DataBuffer(String tmpDirectory)
	{
		this.tmpDirectory = tmpDirectory;
		File dir = new File(tmpDirectory);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}


	/**
	 * Creates instance of data buffer with specified temporary files directory
	 * and threshold for every contained data item. 
	 * 
	 * @param itemMaxSize maximum size of data item
	 * @param tmpDirectory temporary files directory
	 */
	public DataBuffer(int itemMaxSize, String tmpDirectory)
	{
		this(tmpDirectory);
		this.itemMaxSize = itemMaxSize;
	}


	/**
	 * <p>Allocates clean data item in buffer.</p>
	 * Method returns id of allocated data item.  
	 * 
	 * @return 'id' of allocated data item.
	 */
	public String allocate()
	{
		String id = UUID.randomUUID().toString();
		Item item = new Item(id);
		items.put(id, item);
		tracker.track(item.getFile(), item);
		return id;
	}
	
	/**
	 * <p>Returns buffer allocation output stream.</p> 
	 * If output stream of selected item was closed, using
	 * this method again on the same item will result in IllegalStateException.
	 * 
	 * @param id of buffer item
	 * @return buffer item output stream
	 * @throws IllegalStateException
	 */
	public OutputStream getElementOutputStream(String id) throws IllegalStateException {
		Item item = items.get(id);
		if(item.isCommited()){
			throw new IllegalStateException("Buffer element already filled.");
		}
		return item.getOutputStream();
	}
	
	/**
	 * Reads data from specified input stream and creates
	 * single data item.<br/> 
	 * If item maximum size is reached, data is written to temporary file.   
	 * Method returns unique id of created item.
	 * 
	 * @param inputStream streamed input data
	 * @return 'id' of allocated data item
	 */
	public String allocate(InputStream inputStream)
	{
		String id = UUID.randomUUID().toString();
		Item item = new Item(id);
		item.write(inputStream);
		items.put(id, item);
		tracker.track(item.getFile(), item);
		return id;
	}
	
	/**
	 * Reads data from specified input stream and creates
	 * single data item.<br> 
	 * <p>If item maximum size is reached, data is written to temporary file
	 * with name of 'itemName'.</p>   
	 * Method returns unique id of created item.
	 *  
	 * @param inputStream
	 * @param itemName
	 * @return
	 */
	public String allocate(InputStream inputStream, String itemName){
		String id = UUID.randomUUID().toString();
		Item item = new Item(id, itemName);
		item.write(inputStream);
		items.put(id, item);
		tracker.track(item.getFile(), item);
		return id;
	}
	
	/**
	 * <p>Returns specified by id - data item as input stream.</p>
	 * If item does not exists in buffer method returns 'null'.
	 *  
	 * @param 'id' of a data item.
	 * @return streamed data item
	 */
	public InputStream getDataAsStream(String id)
	{
		try {
			Item item = items.get(id);
			if (item != null) {
				return item.getStream();
			}
			return null;
		}
		catch (FileNotFoundException ex) {
			throw new RuntimeException("Temporary file generator exception!");
		}

	}
	
	/**
	 * Returns temp dir of allocated item - can be null if item
	 * is stored in memory.
	 * 
	 * @param id
	 * @return
	 */
	public String getDataDir(String id){
		Item item = items.get(id);
		if(item!=null){
			return item.getDir();
		}
		return null;
	}

	/**
	 * <p>Relieves selected data item.</p>
	 * If 'forceDelete' parameter is set to 'true' temporary file (if it was created)
	 * will be deleted immediately, otherwise it will be deleted after release 
	 * of memory by garbage collector.<br/>   
	 * Method returns 'false' if selected item does not exists in buffer, 
	 * otherwise it returns 'true'.
	 * 
	 * @param 'id' of data item
	 */
	public boolean removeData(String id, boolean forceDelete)
	{
		Item item = items.get(id);
		if (item == null)
			return false;
		if (forceDelete) {
			item.deleteDir();
		}
		items.remove(id);
		return true;
	}


	/**
	 * <p>Relieves all stored in buffer data items</p>
	 * If 'forceDelete' parameter is set to 'true' all temporary files 
	 * will be deleted immediately, otherwise they will be deleted
	 * after release of memory by garbage collector.
	 * 
	 */
	public void clear(boolean forceDelete)
	{
		if (forceDelete) {
			for (Item item : items.values()) {
				item.deleteDir();
			}
		}
		items = new HashMap<String,Item>();
	}
	
	public String getTemporaryDir(){
		return this.tmpDirectory;
	}
	
	/*
	 * Inner class : represents single item of data. 
	 */
	class Item
	{

		private BufferOutputStream os;

		private File tmpFile;

		private String tmpDir;
		
		private boolean commited = false;


		public Item(String id)
		{
			this.tmpDir = tmpDirectory + File.separator + id;
			File tempDir = new File(tmpDir);
			tempDir.mkdir();
			this.tmpFile = new File(tmpDir + File.separator + "backup.ebu");
			os = new BufferOutputStream(itemMaxSize, tmpFile, this);
		}
		
		public Item(String id, String itemName){
			this.tmpDir = tmpDirectory + File.separator + id;
			File tempDir = new File(tmpDir);
			tempDir.mkdir();
			this.tmpFile = new File(tmpDir + File.separator + itemName);
			os = new BufferOutputStream(itemMaxSize, tmpFile, this);
		}
		
		public void write(InputStream is)
		{
			int b;
			byte[] buf = new byte[EGEImpl.BUFFER_SIZE];
			try {
				while ((b = is.read(buf)) != -1) {
					os.write(buf, 0, b);
				}
			}
			catch (IOException ex) {
				LOGGER.error(ex.getMessage());
			}
			finally {
				try {
					os.close();
				}
				catch (IOException ex) {
					LOGGER.error(ex.getMessage());
				}
			}
		}


		public InputStream getStream()
			throws FileNotFoundException
		{
			byte[] data = os.getData();
			if (data == null) {
				return new FileInputStream(os.getFile());
			}
			else {
				return new ByteArrayInputStream(data);
			}
		}


		/*
		 * Deletes temporary file
		 */
		public void deleteFile()
		{
			if (!os.isInMemory()) {
				if (os.getFile().exists()) {
					LOGGER.debug("Removing tmp file : " + os.getFile());
					os.getFile().delete();
				}
			}
		}
		
		public void deleteDir(){
			if (!os.isInMemory()) {
				File dir = new File(tmpDir);
				if(dir.exists()){
					EGEIOUtils.deleteDirectory(dir);
				}
			}
		}
		
		public OutputStream getOutputStream(){
			return os;
		}

		public File getFile()
		{
			return tmpFile;
		}
		
		private String getDir(){
			return tmpDir;
		}

		public boolean isCommited()
		{
			return Boolean.valueOf(commited);
		}
		
		public void commit(){
			this.commited = true;
		}

	}

}
