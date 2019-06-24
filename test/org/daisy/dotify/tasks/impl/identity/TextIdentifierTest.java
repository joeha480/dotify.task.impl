package org.daisy.dotify.tasks.impl.identity;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.daisy.dotify.tasks.impl.identity.TextIdentifier;
import org.daisy.streamline.api.identity.IdentificationFailedException;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.junit.Test;

public class TextIdentifierTest {

	@Test
	public void testIdentifier() throws IdentificationFailedException, URISyntaxException {
		TextIdentifier id = new TextIdentifier();
		AnnotatedFile f = id.identify(DefaultAnnotatedFile.with(Paths.get(this.getClass().getResource("resource-files/text.txt").toURI())).build());
		assertEquals("text/plain", f.getMediaType());
	}
}
