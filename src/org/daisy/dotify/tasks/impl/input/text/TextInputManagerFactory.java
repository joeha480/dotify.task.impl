package org.daisy.dotify.tasks.impl.input.text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.tasks.impl.input.text.TextInputManager.Type;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupFactory;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a task group factory for text input.
 * @author Joel Håkansson
 */
@Component
public class TextInputManagerFactory implements TaskGroupFactory {
	private final Set<TaskGroupInformation> information;
	private static final String XHTML = "xhtml";

	/**
	 * Creates a new text input manager factory.
	 */
	public TextInputManagerFactory() {
		String text = "text";
		String obfl = "obfl";
		
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder(text, obfl).build());
		tmp.add(TaskGroupInformation.newConvertBuilder(text, XHTML).build());
		information = Collections.unmodifiableSet(tmp);
	}
	
	@Override
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		if (XHTML.equalsIgnoreCase(spec.getOutputType().getIdentifier())) {
			return new TextInputManager(spec.getLocale(), Type.XHTML);
		} else {
			return new TextInputManager(spec.getLocale(), Type.OBFL);
		}
	}

	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

}
