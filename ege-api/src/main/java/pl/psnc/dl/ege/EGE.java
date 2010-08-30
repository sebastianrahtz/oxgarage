package pl.psnc.dl.ege;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.exception.RecognizerException;
import pl.psnc.dl.ege.exception.ValidatorException;
import pl.psnc.dl.ege.types.ConversionsPath;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;

/**
 * <p>
 * Main Enrich Garage Engine(EGE) interface.
 * </p>
 * <br/>
 * EGE implementation provides functionality for performing conversion based on
 * loaded converters. Converters are extensions based on plugin mechanism of
 * JPF(Java Plugin Framework). <br/>
 * Each converter can provide multiple conversions described as
 * <code>ConverterAction</code>. Converters actions altogether creates directed
 * graph of possible conversions - where each node(<code>ConverterAction</code>)
 * is connected with directed edge by rule : A is connected with B (A->B), if
 * and only if A output <code>DataType</code> equals B input
 * <code>DataType</code>. Mechanism enables possibility of conversion from one
 * data type to another even if is not possible by direct execution of a
 * <code>Converter</code> function. <br/>
 * Base on this model user can find through EGE functionality :
 * <ul>
 * <li>every possible path of conversion for his <code>DataType</code>;
 * <li>every possible path of conversion for his specified input
 * <code>DataType</code> and result <code>DataType</code>;
 * </ul>
 * Standard implementation should skip paths with cycles but should consider
 * paths with loops, e.g. when loop conversion performs cleaning of a data. <br/>
 * Having the list of paths of possible conversions user can perform chained
 * convert of a data to receive expected result.
 * 
 * @author mariuszs
 */
public interface EGE
{

	/**
	 * Method returns every possible conversion path for specified input
	 * <code>DataType</code>. One of the received paths can be then used to
	 * perform chained conversion.
	 * 
	 * @param sourceDataType
	 *          	input data type
	 * @return list of possible convert paths.
	 */
	public List<ConversionsPath> findConversionPaths(DataType sourceDataType);


	/**
	 * Method return every possible/unique convert path for specified input type
	 * data with pointed output type data.
	 * 
	 * @param sourceDataType
	 *            input data type
	 * @param resultDataType
	 *            expected output data type
	 * @return list of possible convert paths.
	 */
	public List<ConversionsPath> findConversionPaths(DataType sourceDataType,
			DataType resultDataType);


	/**
	 * <p>Method performs validation using all loaded through extension mechanism
	 * {@link Validator} implementations.</p>
	 * Method returns instance of {@link ValidationResult} which contains validation
	 * status and error/warning messages.<br/>
	 * If there is no validator that supports specified data type, then 
	 * ValidatorException will be throw.<br/>
	 * If some unexpected errors occurs during validation, method will throw   
	 * EGEException.
	 * 
	 * @param inputData
	 *            input stream that contains necessary data
	 * @param inputDataType
	 *            validation argument
	 * @return instance of {@link ValidationResult}
	 * @throws IOException
	 * @throws {@link ValidatorException}
	 * @throws {@link EGEException}
	 */
	public ValidationResult performValidation(final InputStream inputData,
			final DataType inputDataType)
		throws IOException, ValidatorException, EGEException;
	
	
	/**
	 * Method performs recognition of the MIME type of an input data. If any of
	 * the loaded {@link Recognizer} implementations recognizes MIME type,
	 * method returns String value of this MIME type, otherwise method throws
	 * exception.
	 * 
	 * @param inputData
	 *            input stream that contains necessary data
	 * @return MIME type as String
	 * @throws RecognizerException
	 * @throws IOException
	 */
	public String performRecognition(InputStream inputData)
		throws RecognizerException, IOException;
	
	/**
	 * Performs sequence of conversions based on specified convert path.<br/>
	 * Data is taken from selected input stream and after all sequenced
	 * conversions sent to pointed output stream.
	 * 
	 * @param inputStream
	 *            source of data to convert
	 * @param outputStream
	 *            output stream for converted data
	 * @param path
	 *            defines sequence of conversion.
	 * @throws EGEException
	 *             if unexpected error occurred within method.
	 * @throws ConverterException
	 *             if during conversion method an exception occurred.
	 */
	public void performConversion(final InputStream inputStream,
			OutputStream outputStream, ConversionsPath path)
		throws ConverterException, EGEException, IOException;
	
	/**
	 * <p>Returns set of data types that are supported for validation.</p> 
	 * 
	 * @return set of data types
	 */
	public Set<DataType> returnSupportedValidationFormats();
	
	/**
	 * <p>Returns all supported by EGE input formats
	 * - entry points for conversion.</p>
	 * 
	 * @return set of a supported input formats
	 */
	public Set<DataType> returnSupportedInputFormats();
}
