package pl.psnc.dl.ege;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import pl.psnc.dl.ege.component.Converter;
import pl.psnc.dl.ege.component.Recognizer;
import pl.psnc.dl.ege.component.Validator;
import pl.psnc.dl.ege.configuration.EGEConfigurationManager;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.exception.RecognizerException;
import pl.psnc.dl.ege.exception.ValidatorException;
import pl.psnc.dl.ege.types.ConversionAction;
import pl.psnc.dl.ege.types.ConversionActionArguments;
import pl.psnc.dl.ege.types.ConversionsPath;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * <p>
 * Standard Enrich Garage Engine(EGE) implementation.
 * </p>
 * Implementation uses JUNG library for generating graph of conversions.
 * 
 * @author mariuszs
 */
public class EGEImpl
	implements EGE, ExceptionListener
{

	public final static int BUFFER_SIZE = 131072;

	private static final Logger LOGGER = Logger.getLogger(EGEImpl.class.getName());

	static {
		try {
			String pathToProps = EGEConstants.OXGAPP + "log4j.xml";
			File conf = new File(pathToProps);
			if (conf.exists())
				DOMConfigurator.configure(pathToProps);
			else {
				BasicConfigurator.configure();
				Logger.getRootLogger().setLevel(Level.ERROR);
			}
		}
		catch (Exception e1) {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ERROR);
		}
	}

	/*
	 * Contains last thrown exception from ConversionPerformer thread.
	 */
	private List<Exception> exceptions = new LinkedList<Exception>();

	/*
	 * Reference to singleton - extension manager.
	 */
	private EGEConfigurationManager em;

	/*
	 * List of available validator plugins : loaded through extension manager.
	 */
	private List<Validator> validators;

	/*
	 * List of available recognizer plugins : loaded through extension manager.
	 */
	private List<Recognizer> recognizers;

	/*
	 * List of available converter plugins : loaded through extension manager.
	 */
	private List<Converter> converters;

	/*
	 * Directed graph of connections between available converter plugins.
	 */
	private final Graph<ConversionAction, Integer> graph = new DirectedSparseMultigraph<ConversionAction, Integer>();

	/**
	 * Default Constructor : initializes basic structures.
	 */
	public EGEImpl()
	{
		initialize();
	}


	/*
	 * Basic initialization : reading list of available converter plugins and
	 * creation of converters graph.
	 */
	private void initialize()
	{
		em = EGEConfigurationManager.getInstance();
		this.converters = em.getAvailableConverters();
		this.validators = em.getAvailableValidators();
		this.recognizers = em.getAvailableRecognizers();
		int size = converters.size();
		Set<ConversionAction> nodes = new HashSet<ConversionAction>();
		for (int i = 0; i < size; i++) {
			Converter conv = converters.get(i);
			for (ConversionActionArguments ac : conv.getPossibleConversions()) {
				ConversionAction ca = new ConversionAction(ac, conv);
				nodes.add(ca);
			}
		}
		size = nodes.size();
		List<ConversionAction> nodesList = new ArrayList<ConversionAction>(
				nodes);
		int index = 0;
		for (int i = 0; i < size; i++) {
			ConversionAction ca = nodesList.get(i);
			graph.addVertex(ca);
			for (int j = 0; j < size; j++) {
				ConversionAction sca = nodesList.get(j);
				if (ca.getConversionOutputType().equals(
					sca.getConversionInputType())) {
					graph.addEdge(index, ca, sca);
					index++;
				}
			}
		}
	}


	/**
	 * Method returns every possible conversion path for specified input
	 * <code>DataType</code>. One of the received paths can be then used to
	 * perform chained conversion.
	 * 
	 * @param sourceDataType
	 *          	input data type
	 * @return list of possible convert paths.
	 */
	public List<ConversionsPath> findConversionPaths(
			final DataType sourceDataType)
	{
		List<ConversionAction> startNodes = getStartNodes(sourceDataType);
		List<ConversionsPath> paths = new ArrayList<ConversionsPath>();
		for (ConversionAction ca : startNodes) {
			expandPathsSet(new ConversionsPath(
					new ArrayList<ConversionAction>()), ca, paths, null);
		}
		Collections.sort(paths);	
		return paths;
	}


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
	public List<ConversionsPath> findConversionPaths(
			final DataType sourceDataType, final DataType resultDataType)
	{
		List<ConversionAction> startNodes = getStartNodes(sourceDataType);
		List<ConversionsPath> paths = new ArrayList<ConversionsPath>();
		for (ConversionAction ca : startNodes) {
			expandPathsSet(new ConversionsPath(
					new ArrayList<ConversionAction>()), ca, paths,
				resultDataType);
		}
		Collections.sort(paths);		
		return paths;
	}


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
		throws IOException, ValidatorException, EGEException
	{
		for (Validator v : validators) {
			for (DataType dt : v.getSupportedValidationTypes()) {
				if (dt.equals(inputDataType)) {
					return v.validate(inputData, inputDataType);
				}
			}
		}
		throw new ValidatorException(inputDataType);
	}
	
	

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
		throws RecognizerException, IOException
	{
		ByteArrayInputStream is = new ByteArrayInputStream(
				inputStreamToByteArray(inputData));
		String mimeType;
		for (Recognizer r : recognizers) {
			try {
				mimeType = r.recognize(is);
				is.reset();
				return mimeType;
			}
			catch (RecognizerException ex) {
				LOGGER.debug("RecognizerException:" + ex.getMessage());
			}
		}
		throw new RecognizerException(
				"MIME type of specified data was not recognized!");
	}


	private byte[] inputStreamToByteArray(InputStream inputStream)
		throws IOException
	{
		byte[] out = new byte[0];
		BufferedInputStream is = new BufferedInputStream(inputStream);
		int len = 0;
		byte[] buf = new byte[BUFFER_SIZE];
		byte[] tmp = null;
		while ((len = is.read(buf)) != -1) {
			tmp = new byte[out.length + len];
			System.arraycopy(out, 0, tmp, 0, out.length);
			System.arraycopy(buf, 0, tmp, out.length, len);
			out = tmp;
			tmp = null;
		}
		return out;
	}

	
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
		throws ConverterException, EGEException, IOException
	{
		try {
			clearExceptionsStack();
			final PipedOutputStream os = new PipedOutputStream();
			PipedInputStream is = new PipedInputStream(os);
			int size = 0;
			if (path != null && path.getPath() != null) {
				size = path.getPath().size();
			}
			// uses inner class ReWriter
			ReWriter cr = new ReWriter(inputStream, os);
			cr.start();
			Thread last = null;
			for (int i = 0; i < size; i++) {
				ConversionAction ca = path.getPath().get(i);
				PipedOutputStream os2 = new PipedOutputStream();
				PipedInputStream is2 = new PipedInputStream(os2);
				Thread convt = new Thread(new ConversionPerformer(ca, is, os2,
						this));
				convt.start();
				last = convt;
				is = is2;
			}
			byte[] buf = new byte[BUFFER_SIZE];
			int b = 0;
			while ((b = is.read(buf)) != -1) {
				outputStream.write(buf, 0, b);
			}
			last.join();
			// catches exception reported in ConversionPerfomer thread
			Exception ex = throwException();
			if (ex != null) {
				throw ex;
			}
		}
		catch (ConverterException ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw ex;
		}
		catch (IOException ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw ex;
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new EGEException(ex.getMessage());
		}
	}

	/**
	 * <p>Returns set of data types that are supported for validation.</p> 
	 * 
	 * @return set of data types
	 */
	public Set<DataType> returnSupportedValidationFormats(){
		Set<DataType> supported = new TreeSet<DataType>();
		for(Validator v : validators){
			supported.addAll(v.getSupportedValidationTypes());
		}
		return supported;
	}
	
	/**
	 * <p>Returns all supported by EGE input formats
	 * - entry points for conversion.</p>
	 * 
	 * @return set of a supported input formats
	 */
	public Set<DataType> returnSupportedInputFormats()
	{
		// sort alphabetically (to keep the documents from same family together)
		Set<DataType> inputTypes = new TreeSet<DataType>();
		for (ConversionAction ca : graph.getVertices()) {
			if(ca.getConversionActionArguments().getVisible()) inputTypes.add(ca.getConversionInputType());
		}
		return inputTypes;
	}


	/*
	 * Gets all nodes considered as starting points for provided input type. 
	 */
	private List<ConversionAction> getStartNodes(DataType inputType)
	{
		List<ConversionAction> nodes = new ArrayList<ConversionAction>(graph
				.getVertices());
		List<ConversionAction> startNodes = new ArrayList<ConversionAction>();
		for (ConversionAction ca : nodes) {
			if (ca.getConversionInputType() != null
					&& ca.getConversionInputType().equals(inputType)) {
				startNodes.add(ca);
			}
		}
		return startNodes;
	}


	/*
	 * Recursive algorithm for adding paths to paths sequence.
	 */
	private void expandPathsSet(ConversionsPath currentPath,
			ConversionAction node, List<ConversionsPath> paths,
			DataType outputType)
	{
		int size = currentPath.getPath().size();
		boolean loop = false;
		// check for loops and cycles : deny cycles.
		for (int i = 0; i < size; i++) {
			ConversionAction ca = currentPath.getPath().get(i);
			if (ca.getConversionInputType().equals(node.getConversionInputType()) || ca.getConversionOutputType().equals(node.getConversionOutputType())) {
				if (i == (size - 1)) {
					if (i > 0) {
						ConversionAction ca2 = currentPath.getPath().get(i - 1);
						if (!ca2.equals(node)) {
							currentPath.getPath().add(node);
							addPath(currentPath, paths, outputType, node
									.getConversionOutputType());
							loop = true;
						}
						else {
							return;
						}
					}
					else {
						currentPath.getPath().add(node);
						addPath(currentPath, paths, outputType, node
								.getConversionOutputType());
						loop = true;
					}
				}
				else {
					return;
				}
			}
		}
		if (!loop) {
			currentPath.getPath().add(node);
			if (currentPath.getPath().size() > 0) {
				if (!(currentPath.getPath().get(0).getConversionInputType()
						.equals(node.getConversionOutputType()))) {
					addPath(currentPath, paths, outputType, node
							.getConversionOutputType());
				}
			}
			else {
				addPath(currentPath, paths, outputType, node
						.getConversionOutputType());
			}
		}
		// only search other paths, if the path we currently have is not longer than equal path already stored in the list of paths
		// if we search all the paths, it takes too long
		int indexOfPath = paths.indexOf(currentPath);
		if(indexOfPath==-1 || paths.get(indexOfPath).getCost()>=currentPath.getCost()) {
			List<ConversionAction> succs = new ArrayList<ConversionAction>(graph
					.getSuccessors(node));
			for (ConversionAction ca : succs) {
				expandPathsSet(new ConversionsPath(new ArrayList<ConversionAction>(
						currentPath.getPath())), ca, paths, outputType);
			}
		}
	}


	/*
	 * Add path if current node output type equals expected output type. If no
	 * output type was specified add path by default - finding all connections.
	 */
	private void addPath(ConversionsPath path, List<ConversionsPath> paths,
			DataType destinOutputType, DataType currentOutputType)
	{
		if(!path.getInputDataType().equals(currentOutputType)){
			if (destinOutputType != null) {
				if (currentOutputType.equals(destinOutputType)) {
					int indexOfPath = paths.indexOf(path);
					if(indexOfPath==-1 || paths.get(indexOfPath).getCost()>path.getCost()) {
						if(indexOfPath!=-1) { 
							paths.remove(indexOfPath);					
							paths.add(indexOfPath, path);
						}
						else paths.add(path);
					}
				}
			}
			else {
				int indexOfPath = paths.indexOf(path);
				if(indexOfPath==-1 || paths.get(indexOfPath).getCost()>path.getCost()) {
					if(indexOfPath!=-1) { 
						paths.remove(indexOfPath);					
						paths.add(indexOfPath, path);
					}
					else paths.add(path);
				}
			}
		}
	}


	/**
	 * Returns conversion graph.
	 * 
	 * @return JUNG graph structure of conversion actions
	 */
	public Graph<ConversionAction, Integer> getConvertersGraph()
	{
		return (Graph<ConversionAction, Integer>) graph;
	}


	/*
	 * Method used by conversion threads to report exceptions caught in run() method. 
	 * (non-Javadoc)
	 * @see pl.psnc.dl.ege.ExceptionListener#catchException(java.lang.Exception)
	 */
	public synchronized void catchException(Exception ex)
	{
		exceptions.add(ex);
	}


	private synchronized void clearExceptionsStack()
	{
		exceptions.clear();
	}


	private synchronized Exception throwException()
	{
		try {
			return exceptions.remove(0);
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	/*
	 * ReWriting streams utility - triggering piped input/output streaming.
	 */
	private static class ReWriter
		extends Thread
	{

		private final InputStream is;

		private final OutputStream os;


		public ReWriter(InputStream is, OutputStream os)
		{
			super();
			this.is = is;
			this.os = os;
		}


		@Override
		public void run()
		{
			int b;
			byte[] buf = new byte[BUFFER_SIZE];
			try {
				while ((b = is.read(buf)) != -1) {
					os.write(buf, 0, b);
					os.flush();

				}
			}
			catch (IOException ex) {
				LOGGER.error(ex.getMessage());
				ex.printStackTrace();
			}
			finally {
				if (os != null) {
					try {
						os.close();
					}
					catch (IOException ex) {
						LOGGER.error(ex.getMessage());
					}
				}
			}

		}

	}

}
