package pl.psnc.dl.ege.webapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Singleton class which provides EGE web application
 * with access to local labels and names.
 * 
 * @author mariuszs
 *
 */
public final class LabelProvider
{
	private static LabelProvider instance = null;
	
	private static final String DEFAULT_LOCALE = "en"; 
	
	private static final String REGEXP_LABEL_CONT = "labels_[a-z][a-z].xml";
	
	private static final Logger LOGGER = Logger.getLogger(LabelProvider.class);
	
	private Map<String,Properties> labels; 
	
	private LabelProvider(String path){
		File dir = new File(path);
		FilenameFilter fnf = new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name)
			{
				if(name.matches(REGEXP_LABEL_CONT)){
					return true;
				}
				return false;
			}
			
		};
		labels = new LinkedHashMap<String,Properties>();
		for(File lc : dir.listFiles(fnf)){
			Properties props = new Properties();
			try{
				props.loadFromXML(new FileInputStream(lc));
				String loc = lc.toString();
				loc = loc.substring(loc.length() - 6, loc.length() - 4);
				labels.put(loc, props);
			}
			catch(Exception ex){
				LOGGER.error(ex.getMessage(), ex);
			}
		}
	}
	
	public static LabelProvider getInstance(String path){
		if(instance == null){
			instance = new LabelProvider(path); 
		}
		return instance;
	}
	
	/**
	 * Returns specified label
	 * of default locale.<br/> 
	 * If label does not exists method
	 * will return an empty String.  
	 * 
	 * @param key
	 * @return
	 */
	public String getLabel(String key){
		return getLabel(key,DEFAULT_LOCALE);
	}
	
	/**
	 * Returns label 
	 * in language specified by locale.<br/> 
	 * If label does not exists method
	 * will return an empty String.  
	 * 
	 * @param key
	 * @param locale
	 * @return
	 */
	public String getLabel(String key, String locale){
		Properties props = labels.get(locale);
		if(props != null){
			return props.getProperty(key);
		}
		return "";
	}
	
	/**
	 * Returns label 
	 * in language specified by locale.<br/>
	 * If label does not exists method
	 * will return an empty String.     
	 * 
	 * @param key
	 * @param locale
	 * @return
	 */
	public String getLabel(String key, Locale locale){
		return getLabel(key,locale.getCountry());
	}
	
	
	
}
