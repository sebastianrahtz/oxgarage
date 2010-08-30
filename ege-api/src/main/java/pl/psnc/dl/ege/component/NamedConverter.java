package pl.psnc.dl.ege.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.types.ConversionActionArguments;

/**
 * Wrapper class of Converter implementation with additional parameter of a
 * converter name.
 * 
 * @author mariuszs
 */
public class NamedConverter implements Converter {

	private final Converter converter;
	private final String converterName;

	/**
	 * Default constructor.
	 * 
	 * @param converter
	 *            reference to a {@link Converter} interface implementation
	 * @param converterName
	 *            name of a converter.
	 */
	public NamedConverter(Converter converter, String converterName) throws IllegalArgumentException {
		if(converter == null){
			throw new IllegalArgumentException();
		}
		this.converter = converter;
		this.converterName = converterName;
	}
	
	/**
	 * Implemented method of {@link Converter} interface.
	 */
	public void convert(InputStream inputStream, OutputStream outputStream,
			ConversionActionArguments conversionDataTypes)
			throws ConverterException, IOException {
		converter.convert(inputStream, outputStream, conversionDataTypes);
	}

	/**
	 * Implemented method of {@link Converter} interface.
	 */
	public List<ConversionActionArguments> getPossibleConversions() {
		return converter.getPossibleConversions();
	}

	/**
	 * Returns contained converter name.
	 * 
	 * @return name
	 */
	public String getName() {
		return converterName;
	}

	/**
	 * Both objects are equal when they`re referencing the same
	 * {@link Converter} interface implementation.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof NamedConverter)) {
			return false;
		}
		NamedConverter cw = (NamedConverter) o;
		if (this.converter == cw.converter) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = 7;
		hashCode = (converterName == null ? 0 : converterName.hashCode());
		hashCode = hashCode + (converter == null ? 0 : converter.hashCode());
		return hashCode;
	}
	
	@Override
	public String toString(){
		return this.getName();
	}
}
