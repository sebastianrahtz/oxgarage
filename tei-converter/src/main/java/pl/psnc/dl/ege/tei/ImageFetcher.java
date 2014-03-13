package pl.psnc.dl.ege.tei;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
/*
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
*/
import java.util.*;
import java.awt.image.BufferedImage;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Processor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import org.tei.utils.XMLUtils;
import org.tei.utils.SaxonProcFactory;

import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.utils.EGEIOUtils;

/**
 * <p>
 * Image fetcher for TEI EGE Converter
 * </p>
 * 
 * Provides facilities for fetching images from internet and also from files submitted via form fields.
 * 
 * @author Lukas Platinsky
 * 
 */

public class ImageFetcher {

	private static final Logger LOGGER = Logger.getLogger(ImageFetcher.class);

	/**
	 * Changes the graphics in TEI so that the images have width and height information included and also copies the images into new directory, changing their names.
	 */
	public static XdmNode getChangedNode(File inputFile, String imgDir, String imgDirRelativeToDoc, 
							File inputDir, File outputDir, String conversion,
							Map<String,String> properties) 
		throws IOException, ConverterException {
		try {
			boolean copy = true;
			boolean download = true;
			boolean textOnly = false;
			if(properties!=null) {
				if(properties.get(ConverterConfiguration.IMAGES_KEY)!=null) 
					copy = properties.get(ConverterConfiguration.IMAGES_KEY).equals("true");
				if(properties.get(ConverterConfiguration.FETCHIMAGES_KEY)!=null)
					download = properties.get(ConverterConfiguration.FETCHIMAGES_KEY).equals("true");
				if(properties.get(ConverterConfiguration.TEXTONLY_KEY)!=null) 
					textOnly = properties.get(ConverterConfiguration.TEXTONLY_KEY).equals("true");
			}
			Document dom = XMLUtils.readInputFileIntoJAXPDoc(inputFile);
	
			if(conversion.equals("toEpub")) {
				if(properties == null) properties = new HashMap<String,String>();
				NodeList headers = dom.getElementsByTagNameNS("http://www.tei-c.org/ns/1.0", "teiHeader");		
				Element header = (Element) headers.item(0);
				NodeList titles = header.getElementsByTagNameNS("http://www.tei-c.org/ns/1.0", "title");
				NodeList authors = header.getElementsByTagNameNS("http://www.tei-c.org/ns/1.0", "author");
				Element title = (Element) titles.item(0);
				Element author = (Element) authors.item(0);
				if(title!=null) properties.put("title", title.getTextContent());
				else properties.put("title", "");
				if(author!=null) properties.put("author", author.getTextContent());
				else properties.put("author", "");
			}

			NodeList graphics = dom.getElementsByTagNameNS("http://www.tei-c.org/ns/1.0", "graphic");		

			int multiplier = 1;
			if(conversion.equals("toDocx")) {	
				multiplier = 3;
			}
			multiplier = 1428 * multiplier;
			if (copy || download || textOnly) {
				for (int i = 0; i < graphics.getLength(); i++) {
	      				Element graphic = (Element) graphics.item(i);
					if(textOnly) {
						graphic.getParentNode().removeChild(graphic);
						i--;
					} else {
						String graphicUrl = graphic.getAttribute("url");
						File imageFile = fetchImage(graphicUrl, inputDir, outputDir, i, copy, download);
						if(imageFile!=null){
							graphic.setAttribute("url", imgDirRelativeToDoc + imageFile.getName());

							int width = 1;
							int height = 1;
							/*
							String widthAsString = graphic.getAttribute("width");
							String heightAsString = graphic.getAttribute("height");
							if(widthAsString.length()!=0 && heightAsString.length()!=0) {
								if(widthAsString.substring(widthAsString.length()-2).equals("cm"))
									width = 183;
								if(heightAsString.substring(heightAsString.length()-2).equals("cm"))
									height = 183;							
								widthAsString = widthAsString.replaceAll("[a-zA-Z]", "");
								heightAsString = heightAsString.replaceAll("[a-zA-Z]", "");	
								width = Math.round(width * Float.valueOf(widthAsString));
								height = Math.round(height * Float.valueOf(heightAsString));
								
								  if(conversion.equals("toDocx")) {							
									graphic.removeAttribute("width");
									graphic.removeAttribute("height");
								}
								
							}
							else {
							*/
							BufferedImage img = javax.imageio.ImageIO.read(imageFile);			
							width = img.getWidth();
							height = img.getHeight();
							/* } */
							height = (height / 72) * multiplier;
							width = (width / 72) * multiplier;
							graphic.setAttributeNS("http://www.tei-c.org/ns/teidocx/1.0", "teidocx:width", "" + width);
							graphic.setAttributeNS("http://www.tei-c.org/ns/teidocx/1.0", "teidocx:height", "" + height);
							
						}
					}
				}
			}
			Processor proc = SaxonProcFactory.getProcessor();
			net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
			return builder.wrap(dom);//XmlUtils.buildXdmNode(dom);
		} catch(NullPointerException e){
			LOGGER.debug("Exception: " + e.toString());
			e.printStackTrace();
			throw new ConverterException("Couldn't find images directory.");
		} catch(IOException e) {
			throw e;
		} catch(ConverterException e) {
			throw e;
		} catch(Exception e) {
			LOGGER.debug("Exception: " + e.toString());
			e.printStackTrace();
			throw new ConverterException("Something went wrong with copying and downloading images. Please try again and if the problem persists, contact support or try converting your document with option \"Convert text only\"");
		}
		//return null;
	}

	private static File fetchImage(String url, File inputDir, File outputDir, int imageIndex, boolean copy, boolean download)
					throws IOException, ConverterException {
		File imageFile = null;
		try {
			String output = outputDir + File.separator + "image" + imageIndex + url.substring(url.lastIndexOf('.'));
			if(url.substring(0, 4).equals("http")) {
				if(download) {
					downloadFile(url, output);
					imageFile = new File(output);
				}
			}
				else if(url.substring(0, 4).equals("file")) {
				    if(download) {
					downloadFile(url, output);
					imageFile = new File(output);
				    }
				} else if(copy && (url.charAt(0)=='/' || Character.isLetter(url.charAt(0)))) {
				    String[] filePath = url.split(File.separator);
				int filePathLength = filePath.length;
				List<File> files = new ArrayList<File>();
				searchForData(inputDir, filePath[filePathLength-1], files);
				int results = files.size();	
				if(results == 1) {
					copyFile(files.get(0), output);
					imageFile = new File(output);
				}
				// More than one file matching the name
				else if (results > 1) {
					// We have to choose the right file
					int i, index1, index2;
					int level = 1;
					String[] resPath;
					while(results > 1 && level<filePath.length) {
						for(i=results-1; i>=0; i--) {
							resPath = files.get(i).toString().split(File.separator);
							index1 = resPath.length - level;
							index2 = filePath.length - level;				
							if(index1>=0 && !resPath[index1].equals(filePath[index2]))
								files.remove(i);
						}
						level++;
						results = files.size();
					}
					if(results == 1) {
						copyFile(files.get(0), output);	
						imageFile = new File(output);
					}
					else {// We couldn't identify the file uniquely
						throw new IOException("Image " + url + " couldn't be uniquely identified. Please check that the urls in graphic tags match the file hierarchy in zip.");
						}			
				}
				else {// There was no file with matching filename
					throw new IOException("Image " + url + " couldn't be found. Please upload it with your file in order to proceed.");
				}
			}
		} catch(NullPointerException e) {
			throw e;//ConverterException("Couldn't find images directory.");
		}
		return imageFile;
		}


	private static void copyFile(File srFile, String destination) throws IOException {
		try{
			File f2 = new File(destination);
			InputStream in = new FileInputStream(srFile);
      
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.flush();
			out.close();
		}
		catch(IOException e){
			LOGGER.debug("IMAGES DEBUG: copyFile Exception " + e.toString());	
			throw e;
		}
	}

	private static void downloadFile(String url, String dtFile) throws IOException {
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			int contentLength = uc.getContentLength();
			InputStream raw = uc.getInputStream();
			InputStream in = new BufferedInputStream(raw);
			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();
			if (offset != contentLength) {
				throw new IOException("Image from url " + url + " couldn't be downloaded.");
			}
			FileOutputStream out = new FileOutputStream(dtFile);
			out.write(data);
			out.flush();
			out.close();
		}
		catch (IOException e) {
			LOGGER.debug("IMAGES DEBUG: Exception " + e.toString());
			throw e;
		}
	}

	private static List<File> searchForData(File dir, String fileName, List<File> files) {
		if (dir == null) return new ArrayList<File>();
		if (dir.listFiles() == null) return new ArrayList<File>();				
		for (File f : dir.listFiles()) {
			if (!f.isDirectory() && fileName.equals(f.getName())) {
				files.add(f);
			} else if (f.isDirectory()) {
				searchForData(f, fileName, files);
			}
		}
		return files;
	}

	
	public static String generateCover(File coverTemplate, String outputDir, Map<String,String> properties) 
					throws ConverterException {
		try{
			String coverImage = "cover.jpg";
			File cover = new File(outputDir + coverImage);	
		
			String author = properties.get("author").replaceAll("[ \n]+", " ");
			String title = properties.get("title").replaceAll("[ \n]+", " ");
			if (author.matches("([^,]+), ([^,]+), (.+)")) {

		            author = author.replaceAll("([^,]+), ([^,]+), (.+)","$2 $1");
			}
			int tLen = title.length();
			if (tLen > 60) {
			    int firstTarget = 40;
			    String Rest= title.substring(firstTarget,tLen);
			    int stop = Rest.indexOf(' ');
			    if (stop > -1) {
				firstTarget = firstTarget + stop;
				}
			    int secondTarget = 10;
			    String reverse = new StringBuffer(title).reverse().toString();
			    Rest= reverse.substring(secondTarget,tLen);
			    stop = Rest.indexOf(' ');
			    if (stop > -1) {
				secondTarget = secondTarget + stop;
				}
			    int Gap = (tLen - secondTarget) - firstTarget;
			    if ( Gap > 9) {
				    title= title.substring(0, firstTarget) + "..." + title.substring(tLen - secondTarget - 1, tLen);
				}
			}
			BufferedImage img = ImageIO.read(coverTemplate);
			Graphics2D g = (Graphics2D) img.getGraphics();

			g.setColor(Color.white);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int maxAuthorFontSize = 73;
			FontMarginTuple authorProps = calculateFontMargin(new Font("MinionPro", Font.PLAIN, maxAuthorFontSize), 565, g, author, 2);

			int maxTitleFontSize = 73;
			FontMarginTuple titleProps = calculateFontMargin(new Font("MinionPro", Font.BOLD, maxTitleFontSize), 575, g, title, 5);

			Font authorFont = authorProps.font;
			int[] authorMargin = authorProps.margin;
			String authorText = authorProps.text;					
			Font titleFont = titleProps.font;
			int[] titleMargin = titleProps.margin;
			String titleText = titleProps.text;

			int marginY;
			int authorFontSize = authorFont.getSize();
			if(author.length()==0) authorFontSize = 0;
			//if(title.contains("\n")) marginY = (310 - authorFontSize - 2*titleFont.getSize())/2;
			//else marginY = (230 - authorFontSize - titleFont.getSize())/2;
			marginY = 50;

			g.setFont(titleFont);	
			String[] lines = titleText.split("\n");
			int topMargin = marginY + 120;
			if (lines.length == 1) {
			    topMargin = marginY + 250;
			}
			else 
			    { 
				if (lines.length == 2) 
				    topMargin = marginY + 200;
			    }
			for(int i = 0; i < lines.length; i++)
			     g.drawString(lines[i], titleMargin[i] + 10 ,topMargin + i * 75 );
			

			g.setColor(Color.black);
			g.setFont(authorFont);
			lines = authorText.split("\n");
			topMargin = marginY + 565;
			for(int i = 0; i < lines.length; i++)
			    g.drawString(lines[i], authorMargin[i] + 10, topMargin + i * 75 );

			ImageIO.write(img, "jpg", cover);
			return coverImage;
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new ConverterException("Error occured while generating cover image.");
		}
	}
	
	private static FontMarginTuple calculateFontMargin(Font font, int widthToFit, Graphics g, String s, int lines) {
		
		FontMetrics fm = g.getFontMetrics(font);			
		int width = fm.stringWidth(s);
		int size = font.getSize();
		String newText = s;
		while (width>lines*widthToFit) {		
			size = (size*19)/20;
			font = new Font(font.getName(), font.getStyle(), size);
			fm = g.getFontMetrics(font);			
			width = fm.stringWidth(s);				
		}
		int margin[] = new int[lines];
		if(lines==1) margin[0] = (widthToFit-width)/2;		
		else {
			newText = "";
			String[] words = s.split(" ");
			int wordIndex = 0;
			for(int i = 0; i<lines && wordIndex<words.length; i++) {
				String currentLine = words[wordIndex];
				String fittingLine = "";
				while(fm.stringWidth(currentLine)<widthToFit && wordIndex<words.length) {
					fittingLine = currentLine;				
					wordIndex++;				
					if(wordIndex<words.length) currentLine = currentLine + " " + words[wordIndex];
				}
				if((fittingLine.length()==0  && wordIndex<words.length) || (i==lines-1 && !fittingLine.equals(currentLine))) {
					return calculateFontMargin(new Font(font.getName(), font.getStyle(), (font.getSize()*4)/5),
												widthToFit, g, s, lines);
				}
				margin[i] = (widthToFit-fm.stringWidth(fittingLine))/2;
				if(newText.length()==0) newText = fittingLine;
				else newText = newText + "\n" + fittingLine;
			}
		}
		return new FontMarginTuple(font, margin, newText);
	}

	private static class FontMarginTuple {
		
		public final Font font;
		public final int[] margin;
		public final String text;

		public FontMarginTuple(Font font, int[] margin, String text) {
			this.font = font;
			this.margin = margin;
			this.text = text;
		}
	}
}
