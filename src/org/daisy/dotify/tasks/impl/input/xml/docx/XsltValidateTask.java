package org.daisy.dotify.tasks.impl.input.xml.docx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadOnlyTask;

class XsltValidateTask extends ReadOnlyTask {
	final URL url;
	final Map<String, Object> options;
	
	public XsltValidateTask(String name, URL url, Map<String, Object> options) {
		super(name);
		this.url = url;
		this.options = options;
	}

	@Override
	@Deprecated
	public void execute(File input) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build());
	}

	@Override
	public void execute(AnnotatedFile input) throws InternalTaskException {
		File output;
		try {
			output = File.createTempFile(this.getClass().getCanonicalName(), ".tmp");
			output.deleteOnExit();
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}
		try {
			XMLTools.transform(input.getPath().toFile(), output, url, options, new net.sf.saxon.TransformerFactoryImpl());
		} catch (XMLToolsException e) {
			throw new InternalTaskException("Input is not a WordML file: " + input.getPath().toFile(), e);
		} finally {
			output.delete();
		}
	}

}
