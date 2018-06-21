package org.daisy.dotify.tasks.impl.input.xml.docx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.dotify.tasks.tools.XsltTask;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadOnlyTask;

class CharCountTask extends ReadOnlyTask {
	private static final Logger logger = Logger.getLogger(CharCountTask.class.getCanonicalName());
	private final URL countCharsWml = this.getClass().getResource("./xslt/post-count-characters-wml.xsl");
	private final URL countCharsDTBook = this.getClass().getResource("./xslt/post-count-characters-dtbook.xsl");
	final File source;
	final Map<String, Object> options;
	
	public CharCountTask(String name, File source, Map<String, Object> options) {
		super(name);
		this.source = source;
		this.options = options;
	}

	@Override
	@Deprecated
	public void execute(File input) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build());
	}

	@Override
	public void execute(AnnotatedFile input) throws InternalTaskException {
		File tc1;
		File tc2;
		try {
			tc1 = File.createTempFile(this.getClass().getCanonicalName(), ".tmp");
			tc2 = File.createTempFile(this.getClass().getCanonicalName(), ".tmp");
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}
		try {
			tc1.deleteOnExit();
			tc2.deleteOnExit();
			logger.info("Verifying result...");
			//input, countCharsWml, tc1.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance();
			XMLTools.transform(source, tc1, countCharsWml, options);
			//t1.getFile().getAbsolutePath(), countCharsDTBook.getAbsolutePath(), tc2.getFile().getAbsolutePath(), factory, xslParams, CatalogEntityResolver.getInstance());
			XMLTools.transform(input, tc2, countCharsDTBook, options);
	
			if (tc1.length()!=tc2.length()) {
				long diff = (tc2.length() - tc1.length());
				String sign = "";
				if (diff>0) sign = "+";
				logger.warning("The text size has changed (" + sign + diff + "). Check the result for errors.");
			} else if (logger.isLoggable(Level.FINE)) {
				logger.fine("Text size ok.");
			}
		} catch (XMLToolsException e) {
			throw new InternalTaskException(e);
		} finally {
			tc1.delete();
			tc2.delete();
		}
	}

}
