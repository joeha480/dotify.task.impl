package org.daisy.dotify.tasks.impl.identity;

import org.daisy.streamline.api.identity.Identifier;
import org.daisy.streamline.api.identity.IdentifierFactory;
import org.daisy.streamline.api.media.FileDetails;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a factory for identifying BRL files.
 * @author Joel Håkansson
 */
@Component
public class BrfIdentifierFactory implements IdentifierFactory {

	@Override
	public Identifier newIdentifier() {
		return new BrfIdentifier();
	}

	@Override
	public boolean accepts(FileDetails type) {
		return "brf".equalsIgnoreCase(type.getExtension());
	}

}
