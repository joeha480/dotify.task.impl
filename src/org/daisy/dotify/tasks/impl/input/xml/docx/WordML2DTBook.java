/*
 * Daisy Pipeline (C) 2005-2008 Daisy Consortium
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.dotify.tasks.impl.input.xml.docx;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.execution.Command;
import org.daisy.util.execution.ExecutionException;
import org.daisy.util.file.FileJuggler;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.TempFile;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.xslt.Stylesheet;

/**
 * 
 * Transforms a Microsoft Office 2003 WordML document into DTBook.
 * 
 * Version 2007-april-11 
 * Changed a few constructs to reflect changes in the Pipeline core API.
 * 
 * @author  Joel Håkansson
 * @version 2007-april-11
 * @since 1.0
 */
public class WordML2DTBook extends Transformer {


	/**
	 * 
	 * @param inListener
	 * @param isInteractive
	 */
	public WordML2DTBook(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	protected boolean execute(Map<String,String> parameters) throws TransformerRunException {
		progress(0);

		// program parameters
		String factory = parameters.remove("factory");
		String input = parameters.remove("xml");
        String xslt = parameters.remove("xslt");
        File outdir = new File(parameters.remove("out"));
        String filename = parameters.remove("filename");
        String images = parameters.remove("images");
        String overwrite = parameters.remove("overwrite");
        String version = parameters.remove("dtbook-version");
 
        String forceJPEG = parameters.get("forceJPEG");
		if ("true".equals(forceJPEG) && "true".equals(images)) {
			try {
				String converter = System.getProperty("pipeline.imageMagick.converter.path");
				ArrayList<String> arg = new ArrayList<String>();
				arg.add(converter);
				Command.execute((arg.toArray(new String[arg.size()])), true);
			} catch (ExecutionException e) {
        		sendMessage("ImageMagick is not available. Verify that ImageMagick is installed and that Daisy Pipeline can find it (paths setup).", MessageEvent.Type.ERROR);
			}
		}        
        // xslt parameters
        String uid = parameters.get("uid");
        String stylesheet = parameters.get("stylesheet");

        final File dtbook2xthml = new File(this.getTransformerDirectory(), "./lib/dtbook2xhtml.xsl");
        final File defaultcss = new File(this.getTransformerDirectory(), "./lib/default.css");
        
        final File inputValidator = new File(this.getTransformerDirectory(), "./xslt/pre-input-validator.xsl");
        final File countCharsWml = new File(this.getTransformerDirectory(), "./xslt/post-count-characters-wml.xsl");
        final File countCharsDTBook = new File(this.getTransformerDirectory(), "./xslt/post-count-characters-dtbook.xsl");
        final File removeTempStructure = new File(this.getTransformerDirectory(), "./xslt/post-remove-temporary-structure.xsl");
        final File defragmentSub = new File(this.getTransformerDirectory(), "./xslt/post-defragment-sub.xsl");
        final File defragmentSup = new File(this.getTransformerDirectory(), "./xslt/post-defragment-sup.xsl");
        final File defragmentEm = new File(this.getTransformerDirectory(), "./xslt/post-defragment-em.xsl");
        final File defragmentStrong = new File(this.getTransformerDirectory(), "./xslt/post-defragment-strong.xsl");
//        final File addAuthorTitle = new File(this.getTransformerDirectory(), "./xslt/post-add-author-title.xsl");
//        final File indent = new File(this.getTransformerDirectory(), "./xslt/post-indent.xsl");
        File doctypeXsl;
        if ("2005-2".equals(version)) {
        	doctypeXsl = new File(this.getTransformerDirectory(), "./xslt/dtbook-2005-2.xsl");
        } else {
        	doctypeXsl = new File(this.getTransformerDirectory(), "./xslt/dtbook-2005-1.xsl");
        }
        
        Map<String,Object> xslParams = new HashMap<String,Object>();
        xslParams.putAll(parameters);
        
//      validate input
		try {
			TempFile t0 = new TempFile();
			Stylesheet.apply(input, inputValidator.getAbsolutePath(), t0.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
		} catch (Exception e) {
			sendMessage("Input is not a WordML file", MessageEvent.Type.ERROR);
			return false;
		}
		// validate custom and default mapsets		
		// new SimpleValidator();

		// 
        if (filename==null || filename.equals(""))
        	filename = new File(input).getName() + ".dtbook.xml";
        
        if (!outdir.exists()) outdir.mkdirs();
        else if (outdir.list(new FilenameFilter() {
        	public boolean accept(File f, String s) {
        		return !(new File(f, s).isDirectory());
        	}
        }).length>0) {
        	if ("true".equals(overwrite)) sendMessage("Directory is not empty. Files could be overwritten!", MessageEvent.Type.WARNING);
        	else {
        		sendMessage("Directory is not empty. Aborting process.", MessageEvent.Type.ERROR);
        		return false;
        	}
        }
        File result = new File(outdir, filename);
		if ("true".equals(images)) {
			sendMessage("Extracting images...");
			decodeImages(new File(input), outdir, forceJPEG);
		} 
		progress(0.4);
		if (uid==null || "".equals(uid)) {
			String s = (new Long((Math.round(Math.random() * 1000000000)))).toString();
			char[] chars = s.toCharArray();
			char[] dest = new char[] {'0','0','0','0','0','0','0','0','0'};
			System.arraycopy(chars, 0, dest, 9-chars.length, chars.length);
			parameters.put("uid", "AUTO-UID-" + new String(dest));
		}
		try {
			if ("dtbook2xhtml.xsl".equals(stylesheet)) {
				FileUtils.copy(dtbook2xthml, new File(outdir, "dtbook2xhtml.xsl"));
				FileUtils.copy(defaultcss, new File(outdir, "default.css"));
			}
			TempFile t1 = new TempFile();
			TempFile t2 = new TempFile();
			TempFile t3 = new TempFile();
			TempFile t4 = new TempFile();
			TempFile t5 = new TempFile();
			TempFile t6 = new TempFile();
//			TempFile t7 = new TempFile();
//			TempFile t8 = new TempFile();
			
			sendMessage("Tempfolder: " + t1.getFile().getParent(), MessageEvent.Type.DEBUG);


			sendMessage("Converting to DTBook...");
			FileJuggler juggler = new FileJuggler(new File(input), t1.getFile());
			Stylesheet.apply(juggler.getInput().getAbsolutePath(), xslt, juggler.getOutput().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			juggler.swap();
			progress(0.5);
			// Add blockwrapper ns (xslt 2.0)
			Stylesheet.apply(juggler.getInput().getAbsolutePath(), new File(this.getTransformerDirectory(), "./xslt/post-add-blockwrapper-ns.xsl").getAbsolutePath(), juggler.getOutput().getAbsolutePath(), "net.sf.saxon.TransformerFactoryImpl", xslParams, CatalogEntityResolver.getInstance());
			juggler.swap();
			progress(0.55);
			// Blockwrapper (xslt 2.0)
			Stylesheet.apply(juggler.getInput().getAbsolutePath(), new File(this.getTransformerDirectory(), "./xslt/post-blockwrapper.xsl").getAbsolutePath(), juggler.getOutput().getAbsolutePath(), "net.sf.saxon.TransformerFactoryImpl", xslParams, CatalogEntityResolver.getInstance());
			juggler.close();
			progress(0.6);
			// check character count
			TempFile tc1 = new TempFile();
			TempFile tc2 = new TempFile();
			sendMessage("Verifying result...");
			Stylesheet.apply(input, countCharsWml.getAbsolutePath(), tc1.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			progress(0.7);
			Stylesheet.apply(t1.getFile().getAbsolutePath(), countCharsDTBook.getAbsolutePath(), tc2.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			
			if (tc1.getFile().length()!=tc2.getFile().length()) {
				long diff = (tc2.getFile().length() - tc1.getFile().length());
				String sign = "";
				if (diff>0) sign = "+";
				sendMessage("The text size has changed (" + sign + diff + "). Check the result for errors.", MessageEvent.Type.WARNING);
			} else {
				sendMessage("Text size ok.");
			}
			progress(0.79);
			sendMessage("Post processing...");
			// Must match the order in wordml2dtbook.xsl
			Stylesheet.apply(t1.getFile().getAbsolutePath(), removeTempStructure.getAbsolutePath(), t2.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			progress(0.82);
			Stylesheet.apply(t2.getFile().getAbsolutePath(), defragmentSub.getAbsolutePath(), t3.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			progress(0.85);
			Stylesheet.apply(t3.getFile().getAbsolutePath(), defragmentSup.getAbsolutePath(), t4.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			progress(0.88);
			Stylesheet.apply(t4.getFile().getAbsolutePath(), defragmentEm.getAbsolutePath(), t5.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			progress(0.91);
			Stylesheet.apply(t5.getFile().getAbsolutePath(), defragmentStrong.getAbsolutePath(), t6.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			progress(0.94);
			//Stylesheet.apply(t6.getFile().getAbsolutePath(), addAuthorTitle.getAbsolutePath(), t7.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			//progress(0.97);
			//Stylesheet.apply(t6.getFile().getAbsolutePath(), indent.getAbsolutePath(), t8.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			//progress(0.99);
			Stylesheet.apply(t6.getFile().getAbsolutePath(), doctypeXsl.getAbsolutePath(), result.getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
        } catch (Exception e) {
            throw new TransformerRunException(e.getMessage(), e);
		}
        progress(1);
		return true;
	}

	private void decodeImages(File input, File outdir, String forceJPEG) throws TransformerRunException {
		Date start = new Date();
	  	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			ImageDecodeHandler handler = new ImageDecodeHandler(input.getParentFile(), outdir);
			spf.newSAXParser().parse(input, handler);
			progress(0.2);
			if ("true".equals(forceJPEG)) {
				convertToJPEG(handler.getFilesToConvert(), outdir);
			}
	  	} catch (Exception e) {
	  		throw new TransformerRunException(e.getMessage(), e);
	  	}
	  	String msg = "Time to decode ";
	  	if ("true".equals(forceJPEG)) msg += "and convert ";
	  	msg += "images: " + (new Date().getTime()-start.getTime())+ " ms";
	  	sendMessage(msg);
	}
	
	private void convertToJPEG(File[] imageFiles, File outdir) {
		if (imageFiles.length>0) {
			sendMessage("Converting images...");
		}
		int i = 0;
		for (File f : imageFiles) {
			if (f.exists()) {
				String name;
				int index;
				ImageConverter ic = new ImageConverter();
				name = f.getName();
				index = name.lastIndexOf('.');
				try {
					ic.convert(f, new File(outdir, name.substring(0, index) + ".jpg"));
					while (!f.delete()) {
						try { Thread.sleep(200); } catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (ExecutionException e) {
					sendMessage("Error: Could not convert image " + f, MessageEvent.Type.ERROR);
				}
			} else {
				sendMessage("Warning: Could not find image " +f, MessageEvent.Type.WARNING);
			}
			i++;
			progress(0.2 + ((float)i/imageFiles.length) * 0.2);
		}
	}

	public void sendMessage(MessageEvent.Type type, String idstr, Object[] params) {
		if (params!=null && params.length>0) {
			super.sendMessage(new MessageFormat(i18n(idstr)).format(params), type);
		} else {
			super.sendMessage(i18n(idstr), type);
		}
	}
}
