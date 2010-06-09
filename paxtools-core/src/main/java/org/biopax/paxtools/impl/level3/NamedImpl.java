package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.util.SetStringBridge;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import java.util.*;

/**
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class NamedImpl extends XReferrableImpl
{

	private String standardName;
	private String displayName;
	private Set<String> allNames;

	public NamedImpl()
	{
		allNames = new HashSet<String>();
		displayName = null;
		standardName = null;
	}

	@Basic
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_NAME, index = Index.TOKENIZED)
	public String getStandardName()
	{
		return standardName;
	}

	public void setStandardName(String name)
	{
		addName(standardName = name);
	}

	@Basic
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_NAME, index = Index.TOKENIZED)
	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String name)
	{
		addName(displayName = name);
	}

	@ElementCollection
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_NAME, index = Index.TOKENIZED)
	@FieldBridge(impl = SetStringBridge.class)
	public Set<String> getName()
	{
		return allNames;
	}

	public void setName(Set<String> names)
	{
		allNames = names;
		
		if (!names.contains(standardName))
		{
			standardName = null;
		}
		if (!names.contains(displayName))
		{
			displayName = null;
		}
		
	}

	public void addName(String name)
	{
		if (name != null)
			allNames.add(name);
	}

	public void removeName(String name)
	{
		if (name != null)
		{
			allNames.remove(name);
			if (name.equals(standardName))
			{
				standardName = null;
			}
			if (name.equals(displayName))
			{
				displayName = null;
			}
		}
	}

}