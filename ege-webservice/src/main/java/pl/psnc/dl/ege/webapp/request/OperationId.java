package pl.psnc.dl.ege.webapp.request;

/**
 * <p>Enum class used mainly by RequestResolvers.</p>
 * Each value identifies operation that can be performed by web service. 
 * 
 * @author mariuszs
 */
public enum OperationId {
	
	/**
	 * Express the operation of printing available input types for conversion 
	 */
	PRINT_INPUT_TYPES,
	
	/**
	 * Marks operation of printing conversions paths
	 */
	PRINT_CONVERSIONS_PATHS,
	
	/**
	 * Identify conversion operation 
	 */
	PERFORM_CONVERSION,
	
	/**
	 * Express operation of data types available for validation
	 */
	PRINT_VALIDATIONS,
	
	/**
	 * Identify validation operation
	 */
	PERFORM_VALIDATION
	
}
