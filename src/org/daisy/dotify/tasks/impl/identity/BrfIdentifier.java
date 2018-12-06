package org.daisy.dotify.tasks.impl.identity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.daisy.streamline.api.identity.IdentificationFailedException;
import org.daisy.streamline.api.identity.Identifier;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.AnnotatedInputStream;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedInputStream;
import org.daisy.streamline.api.media.DefaultFileDetails;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.media.InputStreamSupplier;

public class BrfIdentifier implements Identifier {
	private static final int MAX_LINE_LENGTH = 100;
	private static final int MAX_PAGE_HEIGHT = 50;
	private static final int LF = 10;
	private static final int FF = 12;
	private static final int CR = 13;
	private static final FileDetails DETAILS = new DefaultFileDetails.Builder()
			.formatName("Formatted Braille")
			.mediaType("text/plain")
			.extension("brf")
			.build();
	static final Pattern BRF_PATTERN = Pattern.compile(".*\\.[bB][rR][fF]\\z");

	@Override
	public AnnotatedFile identify(AnnotatedFile f) throws IdentificationFailedException {
		if ("brf".equalsIgnoreCase(f.getExtension()) && check(f.getPath())) {
			return new DefaultAnnotatedFile.Builder(f.getPath())
					.formatName(DETAILS.getFormatName())
					.mediaType(DETAILS.getMediaType())
					.extension(DETAILS.getExtension())
					.build();
		} else {
			throw new IdentificationFailedException(); 
		}
	}

	@Override
	public AnnotatedInputStream identify(InputStreamSupplier source) throws IdentificationFailedException {
		if (source.getSystemId()!=null && BRF_PATTERN.matcher(source.getSystemId()).matches() && check(source)) {
			return new DefaultAnnotatedInputStream.Builder(source).details(DETAILS).build();
		} else {
			throw new IdentificationFailedException(); 
		}
	}
	
	private boolean check(Path p) {
		try (InputStream is = Files.newInputStream(p)) {
			return check(is);
		} catch (IOException e) {
			return false;
		}
	}
	
	private boolean check(InputStreamSupplier source) {
		try (InputStream is = source.newInputStream()) {
			return check(is);
		} catch (IOException e) {
			return false;
		}
		
	}
	
	private boolean check(InputStream is) {
		try (BufferedInputStream buf = new BufferedInputStream(is)) {
			int c;
			int width = 0;
			//Count CR/LF separately. On windows these will have the same value, on linux, only LF will be increased, the old mac format uses only CR
			int heightLF = 0;
			int heightCR = 0;
			while ((c=is.read())>-1) {
				if (c==LF) {
					width = 0;
					heightLF++;
				} else if (c==CR) {
					width = 0;
					heightCR++;
				} else if (c==FF) {
					heightLF = 0;
					heightCR = 0;
				} else {
					width++;
				}
				if (width>MAX_LINE_LENGTH || heightLF>MAX_PAGE_HEIGHT || heightCR>MAX_PAGE_HEIGHT) {
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
