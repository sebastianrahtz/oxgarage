package pl.psnc.dl.ege.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.exception.ValidatorException;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;

/**
 * <p>Main EGE validator interface.</p> 
 * 
 * Validator: this component is responsible for validation of the input data. For example it may
 * be used to validate the ENRICH TEI 5 data in particular Internet Media Type (e.g. text/xml)
 * either received from end user or created by one of the converters. We will assume the
 * following notation: ENRICH TEI 5 (text/xml) - it means that validator is able to validate
 * ENRICH TEI 5 format encoded in text/xml Internet Media Type.
 * 
 * @author mariuszs
 */
public interface Validator{
	
	/**
	 * <p>Validation method</p>
	 * <p></p>
	 * Implemented method should return as result an instance
	 * of {@link ValidationResult} class. Result contains validation status and messages
	 * assigned during validation process.<br/>
	 * <br/>
	 * If DataType was not recognized as supported by implementation, method
	 * throws ValidatorException.<br/>
	 * If some unexpected errors occurs during validation, an instance of EGEException
	 * will be throw.
	 *  
	 * @param inputData data to validate as {@link InputStream}
	 * @param inputFormat specified {@link DataType} of streamed data.
	 * @return instance of {@link ValidationResult} 
	 * @throws IOException
	 * @throws {@link ValidatorException}
	 * @throws {@link EGEException}
	 */
	public ValidationResult validate(InputStream inputData, DataType inputDataType) throws IOException, ValidatorException, EGEException;
	
	/**
	 * <p>Supported data types</p>
	 * 
	 * Method returns list of all supported validators <code>DataType</code> formats.
	 * 
	 * @return list of <code>DataType</code>.
	 */
	public List<DataType> getSupportedValidationTypes();
	
}
