package pl.psnc.dl.ege.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.types.ConversionActionArguments;

/**
 * <p>Main interface of EGE Converter.</p> 
 * 
 * <p>Converter: this component is responsible for converting the input data. It may be, for
 * example, conversion from XML to Word, conversion from Word to PDF, conversion of the
 * XML from one form to another (e.g. TEI P4/MASTER -> ENRICH TEI P5) or even cleaning the input data
 * (e.g. removing redundant information).</p>
 * 
 * <p>Each {@link Converter} can perform conversion of data based on 
 * its list of {@link ConversionActionArguments}. Each ConversionActionArguments specifies one input data type - from which
 * we can convert and one output data type - to which we can convert from input data type.
 * E.g. Having {@link ConvertionActionArguments} which contains input data type of : ENRICH TEI P5 (text/xml) and
 * output data type : ENRICH TEI P5 (application/msword), we can perform conversion from ENRICH TEI P5(text/xml)
 * to ENRICH TEI P5(application/msword).</p>    
 * 
 * 
 * @author mariuszs
 */
public interface Converter{
	
	/**
	 * Method performs conversion of streamed input data and puts converted data to
	 * specified <code>OutputStream</code>. 
	 * Method returns {@link ConverterException} if an error occur during 
	 * conversion, e.g. when input {@link DataType} was not recognized as supported.  
	 * <br/> 
	 * <p><b>Important:</b> within body of a <code>convert()</code> method it is not allowed to use
	 * close() or open() methods on either input or output stream. By definition
	 * input and output streams are provided and managed from 'outside' method.</p> 
	 * 
	 * @param inputStream source of data to convert.
	 * @param outputStream output stream for converted data.  
	 * @param conversionDataTypes data types for both input and output data.
	 * @throws ConverterException if an error occurred during convert operation.   
	 * @throws IOException
	 */
	public void convert(InputStream inputStream, OutputStream outputStream, ConversionActionArguments conversionDataTypes) throws ConverterException, IOException;
	
	/**
	 * Returns all supported convert configurations: as {@link ConversionActionArguments}.
	 * Each {@link ConversionActionArguments} specifies from which <code>DataType</code> to
	 * which {@link DataType} we can convert.
	 * 
	 * @return list of {@link ConversionActionArguments}
	 */
	public List<ConversionActionArguments> getPossibleConversions();
	
	

}

