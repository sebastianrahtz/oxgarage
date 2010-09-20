package pl.psnc.dl.ege.tei;

import java.io.File;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Resolves URI addresses from include and import 
 * methods of each xsl transformation scheme. 
 * 
 * @author mariuszs
 */
public final class TEIConverterURIResolver
	implements URIResolver
{

	private String basePath;

	private TEIConverterURIResolver(String basePath)
	{
		this.basePath = basePath;
	}

	
	public static TEIConverterURIResolver newInstance(String path)
	{
		return new TEIConverterURIResolver(path);
	}


	public Source resolve(String href, String base)
		throws TransformerException
	{
		try {
			URI bpuri;
			if (base.equals("")) {
				bpuri = new URI(basePath + "/");
			}
			else {
				bpuri = new URI(base);
			}
			URI rel = new URI(href);
			URI fin = bpuri.resolve(rel);
			File file = new File(fin.toString());
			if (file.exists())
				return new StreamSource(file);
		}
		catch (Exception ex) {
			throw new TransformerException(ex.getMessage());
		}
		return null;
	}
}
