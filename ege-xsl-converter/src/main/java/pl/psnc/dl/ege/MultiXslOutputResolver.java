package pl.psnc.dl.ege;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Result;

import net.sf.saxon.event.StandardOutputResolver;
import net.sf.saxon.trans.XPathException;

/**
 * Alternative output uri resolver for
 * xsl transformation - controls output of xslt operation
 * of "xsl:result-document". 
 * 
 * @author mariuszs
 */
public class MultiXslOutputResolver
	extends StandardOutputResolver
{
	private String subPath;
	
	private static final URI NEW_BASE;
	static {
		String tmp = System.getProperty("java.io.tmpdir");
		if(!(tmp.endsWith("/") || tmp.endsWith("\\"))){
			tmp = tmp + File.separator;
		}
		File tmpFil = new File(tmp);
		NEW_BASE = tmpFil.toURI();
	}
	
	/**
	 * Constructor : output data is written
	 * to unique sub-directory for each
	 * different input data transformation.
	 * 
	 * @param subPath
	 */
	public MultiXslOutputResolver(String subPath)
	{
		this.subPath = subPath;
	}

	@Override
	public Result resolve(String href, String base)
		throws XPathException
	{
		base = NEW_BASE.toString();
		try {
			URI absoluteURI;
			if (href.length() == 0) {
				if (base == null) {
					throw new XPathException(
							"The system identifier of the principal output file is unknown");
				}
				absoluteURI = new URI(base);
			}
			else {
				absoluteURI = new URI(href);
			}
			if (!absoluteURI.isAbsolute()) {
				if (base == null) {
					throw new XPathException(
							"The system identifier of the principal output file is unknown");
				}
				URI baseURI = new URI(base);
				absoluteURI = baseURI.resolve(href);
			}
			StringBuffer sb = new StringBuffer();
			String newURI = absoluteURI.toString();
			sb.append(newURI.substring(0,newURI.lastIndexOf("/")));
			sb.append("/");
			sb.append(subPath);
			sb.append(newURI.substring(newURI.lastIndexOf("/"),newURI.length()));
			return makeOutputFile(new URI(sb.toString()));

		}
		catch (URISyntaxException err) {
			throw new XPathException("Invalid syntax for base URI", err);
		}
		catch (IllegalArgumentException err2) {
			throw new XPathException("Invalid URI syntax", err2);
		}
	}
	
}
