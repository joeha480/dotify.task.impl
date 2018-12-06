package org.daisy.dotify.tasks.impl.identity;

import java.io.IOException;
import java.io.InputStream;

import org.daisy.dotify.tasks.impl.identity.BrfIdentifier;
import org.daisy.streamline.api.identity.IdentificationFailedException;
import org.daisy.streamline.api.media.InputStreamSupplier;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrfIdentifierTest {
	
	@Test
	public void testRegex() {
		assertTrue(BrfIdentifier.BRF_PATTERN.matcher("brf.brf").matches());
	}

	@Test
	public void testBrfIdentifier() throws IdentificationFailedException {
		BrfIdentifier id = new BrfIdentifier();
		InputStreamSupplier is = new InputStreamSupplier() {

			@Override
			public InputStream newInputStream() throws IOException {
				return this.getClass().getResourceAsStream("resource-files/brf.brf");
			}

			@Override
			public String getSystemId() {
				return "brf.brf";
			}
			
		};
		assertEquals("brf", id.identify(is).getExtension());
	}
}
