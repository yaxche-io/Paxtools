package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 */
public abstract class BioPAXElementImpl implements BioPAXElement
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID = -4176374334987351368L;
	private String id;
	private Model m_model;


    public int hashCode()
    {
        return (id == null ? super.hashCode() : id.hashCode());
    }

    public boolean equals(Object o)
    {
        boolean value = false;

        if (this == o)
        {
            value = true;
        }
        else if (o != null && o instanceof BioPAXElement)
        {

            final BioPAXElement that = (BioPAXElement) o;
            value = this.getRDFId().equals(that.getRDFId());
        }
        return value;
    }

    public boolean isEquivalent(BioPAXElement element)
    {
        return (this.equals(element) || (
                this.getModelInterface().isInstance(element) &&
                this.semanticallyEquivalent(element)));
    }

   protected boolean semanticallyEquivalent(BioPAXElement element)
    {
        return false;
    }

    public int equivalenceCode()
    {
        return hashCode();
    }

// --------------------- ACCESORS and MUTATORS---------------------


    public String getRDFId()
    {
        return id;
    }

    public void setRDFId(String id)
    {
        this.id = id;
    }

    public String toString()
    {
        return id;
    }
}

