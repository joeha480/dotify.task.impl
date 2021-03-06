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
package org.daisy.dotify.tasks.impl.input;

import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;

import org.daisy.dotify.common.text.StringFilter;
import org.daisy.dotify.common.xml.StaxFilter;

/**
 * 
 * StaxFilter that runs an ArrayList of StringFilter on all text nodes.
 * 
 * @author  Joel Hakansson
 * @version 4 maj 2009
 * @since 1.0
 */
public class TextNodeFilter extends StaxFilter {
	private StringFilter filters;

	/**
	 * Create a new TextNodeFilter
	 * @param input an xml event reader
	 * @param output the output stream
	 * @param filters ArrayList of regular expressions
	 */
	public TextNodeFilter(XMLEventReader input, OutputStream output, StringFilter filters) {
		super(input, output);
		this.filters = filters;
	}
	
    @Override
	protected Characters characters(Characters event) {
    	return getEventFactory().createCharacters(filters.filter(event.getData()));
    }

}
