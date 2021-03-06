package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.Method;

/**
 * Provides an ENUM class compatible editor by extending the {@link PropertyEditor}.
 *
 * @see PropertyEditor
 */
public class EnumeratedPropertyEditor<D extends BioPAXElement, R extends Enum>
		extends AbstractPropertyEditor<D, R> implements DataPropertyEditor<D,R>
{
// --------------------------- CONSTRUCTORS ---------------------------

	public EnumeratedPropertyEditor(String property, Method getMethod,
	                                Class<D> domain,
	                                Class<R> range,
	                                boolean multipleCardinality)
	{
		super(property,
				getMethod,
				domain,
				range,
				multipleCardinality);
	}

// -------------------------- OTHER METHODS --------------------------

	@Override
	protected R parseValueFromString(String value)
	{


        value = value.replaceAll("-", "_");
        value = value.replaceAll("^\\s+","");
        value = value.replaceAll("\\s+$","");
		return (R)Enum.valueOf(this.getRange(), value);
	}
}

