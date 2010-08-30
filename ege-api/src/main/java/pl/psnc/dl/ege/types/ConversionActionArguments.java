package pl.psnc.dl.ege.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class describes input, output data types
 * and conversion properties for a conversion action. 
 * <br/><br/>
 * Conversion properties definitions are provided by converter
 * and should be properly described within converters usage documentation.<br/>
 * Client application can then translate properties definitions in order to provide interface 
 * for conversion configuration.<br/>
 * Properly formed definition of property should contain at least:
 * <ul>
 * <li> unique id of property; </li>
 * <li> property data type. </li>
 * </ul> 
 * Configured properties are then provided for converter
 * within Map, where <i>key</i> is an "id" of property.
 *  
 * @author mariuszs
 */
public class ConversionActionArguments {
	
	private final String propertiesDefinitions;
	private final DataType inputType;
	private final DataType outputType;
	private Map<String,String> properties;  
	private final boolean visible;
	
	// states cost of using this edge in conversion path. Default cost is 10. 
	// The higher the cost, the worse the conversion is and hence it will probably not be used in the path.
	private final int cost;
	
	/**
	 * Constructor - specified input and output data types.
	 * 
	 * @param inputType specified input data type
	 * @param outputType specified output data type
	 */
	public ConversionActionArguments(DataType inputType, DataType outputType){
		if(inputType == null || outputType == null){
			throw new IllegalArgumentException();
		}
		this.inputType = inputType;
		this.outputType = outputType;
		this.propertiesDefinitions = "";
		this.visible = true;
		this.cost = 10;
	}
	
	/**
	 * Constructor - specified input and output data types and properties definitions.
	 * 
	 * @param inputType specified input data type
	 * @param outputType specified output data type
	 * @param propertiesDefinitions definitions of properties
	 */
	public ConversionActionArguments(DataType inputType, DataType outputType, String propertiesDefinitions){
		if(inputType == null || outputType == null){
			throw new IllegalArgumentException();
		}
		this.inputType = inputType;
		this.outputType = outputType;
		if(propertiesDefinitions == null){
			this.propertiesDefinitions = "";
		}
		else{
			this.propertiesDefinitions = propertiesDefinitions;
		}
		this.visible = true;
		this.cost = 10;
	}

	/**
	 * Constructor - specified input and output data types and whether the input type should be offered as an input format
	 * 
	 * @param inputType specified input data type
	 * @param outputType specified output data type
	 * @param visible specifies whether the input type should be offered

	 */
	public ConversionActionArguments(DataType inputType, DataType outputType, boolean visible){
		if(inputType == null || outputType == null){
			throw new IllegalArgumentException();
		}
		this.inputType = inputType;
		this.outputType = outputType;
		this.propertiesDefinitions = "";
		this.visible = visible;
		this.cost = 10;
	}

	/**
	 * Constructor - specified input and output data types, properties definitions and whether the input type should be offered as an input format
	 * 
	 * @param inputType specified input data type
	 * @param outputType specified output data type
	 * @param propertiesDefinitions definitions of properties
	 * @param visible specifies whether the input type should be offered
	 */
	public ConversionActionArguments(DataType inputType, DataType outputType, String propertiesDefinitions, boolean visible){
		if(inputType == null || outputType == null){
			throw new IllegalArgumentException();
		}
		this.inputType = inputType;
		this.outputType = outputType;
		if(propertiesDefinitions == null){
			this.propertiesDefinitions = "";
		}
		else{
			this.propertiesDefinitions = propertiesDefinitions;
		}
		this.visible = visible;
		this.cost = 10;
	}

/**
	 * Constructor - specified input and output data types and whether the input type should be offered as an input format.
	 * Also specifies the cost, which is used in calculating the best path in the conversions graph. The better the conversion is,
	 * The better the conversion is, the lower the cost should be. Cost is set manually for each conversion. 
	 *
	 * @param inputType specified input data type
	 * @param outputType specified output data type
	 * @param visible specifies whether the input type should be offered
	 * @param cost specifies the cost of using the conversion
	 */
	public ConversionActionArguments(DataType inputType, DataType outputType, boolean visible, int cost){
		if(inputType == null || outputType == null){
			throw new IllegalArgumentException();
		}
		this.inputType = inputType;
		this.outputType = outputType;
		this.propertiesDefinitions = "";
		this.visible = visible;
		this.cost = cost;
	}

	/**
	 * Constructor - specified input and output data types, properties definitions and whether the input type should be offered as an input format
	 * Also specifies the cost, which is used in calculating the best path in the conversions graph. The better the conversion is,
	 * The better the conversion is, the lower the cost should be. Cost is set manually for each conversion. 	 
	 * 
	 * @param inputType specified input data type
	 * @param outputType specified output data type
	 * @param propertiesDefinitions definitions of properties
	 * @param visible specifies whether the input type should be offered
	 */
	public ConversionActionArguments(DataType inputType, DataType outputType, String propertiesDefinitions, boolean visible, int cost){
		if(inputType == null || outputType == null){
			throw new IllegalArgumentException();
		}
		this.inputType = inputType;
		this.outputType = outputType;
		if(propertiesDefinitions == null){
			this.propertiesDefinitions = "";
		}
		else{
			this.propertiesDefinitions = propertiesDefinitions;
		}
		this.visible = visible;
		this.cost = cost;
	}
	
	/**
	 * Returns definitions of conversion parameters.
	 * <br/><br/>
	 * Definitions are returned as standard String and therefore 
	 * its syntax should be properly documented through converter API. 
	 *  
	 * @return definitions of properties
	 */
	public String getPropertiesDefinitions(){
		return propertiesDefinitions;
	}
	
	/**
	 * Returns conversion parameters.
	 * 
	 * @return conversion parameters
	 */
	public Map<String,String> getProperties(){
		if(properties == null) return new LinkedHashMap<String,String>();
		return properties;
	}
	
	/**
	 * Sets conversion parameters. 
	 * Parameters are written in Map according to syntax : key is id of parameter,
	 * value is value of the parameter.  
	 *   
	 * @param properties map of properties
	 */
	public void setProperties(Map<String,String> properties){
		this.properties = properties;
	}
	
	/**
	 * Returns input <code>DataType</code>
	 * 
	 * @return input <code>DataType</code>
	 */
	public DataType getInputType() {
		return inputType;
	}

	/**
	 * Returns output <code>DataType</code>
	 * 
	 * @return output <code>DataType</code>
	 */
	public DataType getOutputType() {
		return outputType;
	}

	/**
	 * Returns whether the conversion should be visible

	 * 
	 * @return visible
	 */
	public boolean getVisible() {
		return visible;
	}

	/**
	 * Returns the cost of this conversion

	 * 
	 * @return cost
	 */
	public int getCost() {
		return cost;
	}


	
	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof ConversionActionArguments)) {
			return false;
		}
		ConversionActionArguments ca = (ConversionActionArguments) o;
		if (ca.getInputType() != null
				&& ca.getInputType().equals(this.inputType)
				&& ca.getOutputType() != null && ca.getOutputType().equals(this.outputType)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 7;
		hashCode = hashCode + (this.inputType == null ? 0 : this.inputType.hashCode());
		hashCode = hashCode + (this.outputType == null ? 0 : this.outputType.hashCode());
		return hashCode;
	}
	
	@Override
	public String toString(){
		return "I:"+this.inputType+"/O:"+this.outputType;
	}
	
}
