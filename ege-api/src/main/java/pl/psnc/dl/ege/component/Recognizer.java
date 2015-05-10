package pl.psnc.dl.ege.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pl.psnc.dl.ege.exception.RecognizerException;

/**
 * <p>Main EGE Recognizer interface.</p>
 * 
 * Recognizer: this component is responsible for the recognition of the Internet Media Type
 * (MIME type) of the given input data. For example, it will receive the input data and state
 * that the input data has text/xml Internet Media Type (IMT). The recognized data may be
 * further validated to check the format of the data.
 * 
 * @author mariuszs
 */
public interface Recognizer{
	
	/**
	 * Implemented method returns MIME type of received input data 
	 * or throws exception if MIME type was not recognized.  
	 *   
	 * @param inputData streamed data to recognize
	 * @return MIME type as String
	 * @throws IOException
	 * @throws {@link RecognizerException}
	 */
	public String recognize(InputStream inputData) throws RecognizerException, IOException;
	
	/**
	 * Implemented method returns recognizer supported MIME types.
	 * 
	 * @return list of supported MIME types as list of String
	 */
	public List<String> getRecognizableMimeTypes();
	
}
