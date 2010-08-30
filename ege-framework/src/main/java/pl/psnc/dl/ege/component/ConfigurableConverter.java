package pl.psnc.dl.ege.component;

import java.util.Map;

import pl.psnc.dl.ege.exception.EGEException;

/**
 * <p>Implemented by converters with additional parameters
 * taken from plugin.xml configuration file.<br/>
 * Full list of plugin parameters can be obtained from {@link ExtensionParam} 
 * enum class.
 * </p>  
 *  
 * @author mariuszs
 */
public interface ConfigurableConverter extends Converter {
	
	/**
	 * <p>Provided through EGE configuration mechanism parameters
	 * can be compared with {@link ExtensionParam} values.<br/> 
	 * When expected parameters are missing or with errors one can implement
	 * throwing of a EGEException in case when component can`t be
	 * properly setup without them - EGE configuration manager will exclude 
	 * extension when catching exception.
	 * </p>
	 *
	 * @param params converters parameters
	 * @throws EGEException
	 */
	public void configure(Map<String,String> params) throws EGEException; 
	
}
