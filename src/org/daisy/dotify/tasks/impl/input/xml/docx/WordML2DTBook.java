package org.daisy.dotify.tasks.impl.input.xml.docx;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.tasks.tools.XsltTask;
import org.daisy.streamline.api.tasks.InternalTask;

/**
 * Transforms a Microsoft Office 2003 WordML document into DTBook.
 * 
 * @author  Joel Håkansson
 */
public class WordML2DTBook {

	private static String orDefault(Object in, String def) {
		return in!=null?in.toString():def;
	}
	
	private static URL toURL(Object in, URL def) {
		if (in!=null) {
			try {
				return new URL(in.toString());
			} catch (MalformedURLException e) {
				// return default
			}
		}
		return def;
	}

	public List<InternalTask> compile(Map<String, Object> params) {
		Map<String, Object> parameters = new HashMap<>(params);
		// program parameters
		URL xslt = toURL(parameters.remove("xslt"), this.getClass().getResource("wordml2dtbook.xsl"));
		String images = orDefault(parameters.remove("images"), "true");
		//String overwrite = orDefault(parameters.remove("overwrite"), "true");
		String version = orDefault(parameters.remove("dtbook-version"), "2005-1");
		String forceJPEG = orDefault(parameters.get("forceJPEG"), "false");
		
		if ("true".equals(forceJPEG) && "true".equals(images)) {
			//FIXME:
			/*
			try {
				String converter = System.getProperty("pipeline.imageMagick.converter.path");
				ArrayList<String> arg = new ArrayList<String>();
				arg.add(converter);
 
				Command.execute((arg.toArray(new String[arg.size()])), true);
			} catch (ExecutionException e) {
				sendMessage("ImageMagick is not available. Verify that ImageMagick is installed and that Daisy Pipeline can find it (paths setup).", MessageEvent.Type.ERROR);
			}*/
		}
		// xslt parameters
		String uid = orDefault(parameters.get("uid"), "");
		String stylesheet = orDefault(parameters.get("stylesheet"), "");
		//FIXME:
/*
		final File dtbook2xthml = new File(this.getClass().getResource("lib/dtbook2xhtml.xsl").toURI());
		final File defaultcss = new File(this.getClass().getResource("lib/default.css").toURI());
*/
		final URL inputValidator = this.getClass().getResource("xslt/pre-input-validator.xsl");
		final URL removeTempStructure = this.getClass().getResource("xslt/post-remove-temporary-structure.xsl");
		final URL defragmentSub = this.getClass().getResource("xslt/post-defragment-sub.xsl");
		final URL defragmentSup = this.getClass().getResource("xslt/post-defragment-sup.xsl");
		final URL defragmentEm = this.getClass().getResource("xslt/post-defragment-em.xsl");
		final URL defragmentStrong = this.getClass().getResource("xslt/post-defragment-strong.xsl");
		//final URL addAuthorTitle = this.getClass().getResource("xslt/post-add-author-title.xsl");
		//final URL indent = this.getClass().getResource("xslt/post-indent.xsl");

		URL doctypeXsl;
		if ("2005-2".equals(version)) {
			doctypeXsl = this.getClass().getResource("xslt/dtbook-2005-2.xsl");
		} else {
			doctypeXsl = this.getClass().getResource("xslt/dtbook-2005-1.xsl");
		}

		Map<String,Object> xslParams = new HashMap<String,Object>();
		xslParams.putAll(parameters);
		List<InternalTask> ret = new ArrayList<>();
		// validate input
		ret.add(new XsltValidateTask("WordML validator", inputValidator, xslParams));
		// TODO: validate custom and default mapsets

		//FIXME:
		/*
		if (!outdir.exists()) { 
			outdir.mkdirs();
		} else if (outdir.list((File f, String s) -> !(new File(f, s).isDirectory())).length>0) {

			if ("true".equals(overwrite)) {
				sendMessage("Directory is not empty. Files could be overwritten!", MessageEvent.Type.WARNING);
			} else {
				sendMessage("Directory is not empty. Aborting process.", MessageEvent.Type.ERROR);
				return false;
			}
		}
		File result = new File(outdir, filename);
		*/
		if ("true".equals(images)) {
			//FIXME:
			/*
			sendMessage("Extracting images...");
			decodeImages(new File(input), outdir, forceJPEG);
			*/
		}
		
		if (uid==null || "".equals(uid)) {
			String s = (new Long((Math.round(Math.random() * 1000000000)))).toString();
			char[] chars = s.toCharArray();
			char[] dest = new char[] {'0','0','0','0','0','0','0','0','0'};
			System.arraycopy(chars, 0, dest, 9-chars.length, chars.length);
			parameters.put("uid", "AUTO-UID-" + new String(dest));
		}
		//ret.add(new XsltTask("", this.getClass().getResource(""), xslParams));
		//FIXME:
		if ("dtbook2xhtml.xsl".equals(stylesheet)) {
			/*
			FileUtils.copy(dtbook2xthml, new File(outdir, "dtbook2xhtml.xsl"));
			FileUtils.copy(defaultcss, new File(outdir, "default.css"));*/
		}
		ret.add(new XsltTask("Converting to DTBook...", xslt, xslParams));
		
		//Add blockwrapper ns (xslt 2.0)
		ret.add(new XsltTask("Add blockwrapper ns", this.getClass().getResource("xslt/post-add-blockwrapper-ns.xsl"), xslParams));
		
		// Blockwrapper (xslt 2.0)
		ret.add(new XsltTask("Blockwrapper", this.getClass().getResource("xslt/post-blockwrapper.xsl"), xslParams));
		
		// FIXME: input is not available!
		//ret.add(new CharCountTask("Character count", new File(input), xslParams));
		//sendMessage("Post processing...");
		// Must match the order in wordml2dtbook.xsl
		ret.add(new XsltTask("", removeTempStructure, xslParams));
		ret.add(new XsltTask("", defragmentSub, xslParams));
		ret.add(new XsltTask("", defragmentSup, xslParams));
		ret.add(new XsltTask("", defragmentEm, xslParams));
		ret.add(new XsltTask("", defragmentStrong, xslParams));
		//ret.add(new XsltTask("", addAuthorTitle, xslParams));
		//ret.add(new XsltTask("", indent, xslParams));
		ret.add(new XsltTask("", doctypeXsl, xslParams));

		return ret;
	}
/*
	private void decodeImages(File input, File outdir, String forceJPEG) throws TransformerRunException {
		Date start = new Date();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			ImageDecodeHandler handler = new ImageDecodeHandler(input.getParentFile(), outdir);
			spf.newSAXParser().parse(input, handler);
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
*/
}
