package org.biopax.paxtools.impl;

import javax.persistence.*;

import org.biopax.paxtools.model.BioPAXElement;


@Entity
@NamedQueries({
	@NamedQuery(name="org.biopax.paxtools.impl.elementByRdfId",
		query="from org.biopax.paxtools.model.BioPAXElement as el where upper(el.RDFId) = upper(:rdfid)"),
	@NamedQuery(name="org.biopax.paxtools.impl.elementByRdfIdEager",
		query="from org.biopax.paxtools.model.BioPAXElement as el fetch all properties where upper(el.RDFId) = upper(:rdfid)")
})
public abstract class BioPAXElementImpl implements BioPAXElement
{
	// ----------------- Index Names and Search Fields --------------------
	public final static String SEARCH_FIELD_SOURCE_NAME = "source_name";
	public final static String SEARCH_FIELD_KEYWORD ="keyword";
	public final static String SEARCH_FIELD_NAME = "name";
	public final static String SEARCH_FIELD_SYNONYMS = "synonyms";
	public final static String SEARCH_FIELD_TERM = "term";
	public final static String SEARCH_FIELD_EC_NUMBER = "ec_number";
	public final static String SEARCH_FIELD_SEQUENCE = "sequence";
	public final static String SEARCH_FIELD_XREF_DB = "xref_db";
	public final static String SEARCH_FIELD_XREF_ID = "xref_id";
	public final static String SEARCH_FIELD_AVAILABILITY = "availability";
	public final static String SEARCH_FIELD_COMMENT = "comment";
	public final static String SEARCH_FIELD_NAMESPACE = "namespace";
	public final static String SEARCH_INDEX_FOR_ENTITY = "entities";
	public final static String SEARCH_INDEX_FOR_UTILILTY_CLASS = "utilityClasses";
	
	// ------------------------------ FIELDS ------------------------------

	private String id;
	
	private Long proxyId = 0L;

	@Id
    @GeneratedValue
    private Long getProxyId() {
        return proxyId;
    }

    private void setProxyId(Long value) {
        proxyId = value;
    }
    
    
	protected BioPAXElementImpl(){};
	
	
	public int hashCode()
    {
        return id == null ? super.hashCode() : id.hashCode();
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
        return this.equals(element) || this.getModelInterface().isInstance(element) &&
                this.semanticallyEquivalent(element);
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

