package org.biopax.paxtools.causality.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collections;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Match implements Cloneable
{
	private BioPAXElement[] variables;

	/**
	 * For cloning only.
	 */
	private Match()
	{
	}

	public Match(int size)
	{
		this.variables = new BioPAXElement[size];
	}

	public BioPAXElement get(int index)
	{
		return variables[index];
	}

	public void set(BioPAXElement ele, int index)
	{
		variables[index] = ele;
	}

	public boolean varsPresent(int ... ind)
	{
		for (int i : ind)
		{
			if (variables[i] == null) return false;
		}
		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
