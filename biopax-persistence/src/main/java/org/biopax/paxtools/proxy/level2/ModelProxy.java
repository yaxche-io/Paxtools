package org.biopax.paxtools.proxy.level2;

import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import org.biopax.paxtools.model.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;

/**
 * L2 Paxtools BioPAX Persistent Model
 * 
 * @author rodche
 *
 */
@Entity(name="l2model")
public class ModelProxy implements Model {

	private static final long serialVersionUID = 6089833704743927622L;

	@Transient
	private Model model;
	
	public ModelProxy() {
		this(new BioPAXFactoryForPersistence()); // with L2 factory
	}
	
	ModelProxy(BioPAXFactory factory)
	{
		if(factory instanceof BioPAXFactoryForPersistence) {
			this.model = BioPAXLevel.L2.getDefaultFactory().createModel();
			this.model.setFactory(factory);
		} else {
			throw new IllegalArgumentException("Unsupported BioPAX Factory!");
		}
	}
	
	private Long id = 0L;
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long value) {
		id = value;
	}
	
	
	public void add(BioPAXElement aBioPAXElement) {
		model.add(aBioPAXElement);
	}

	public <T extends BioPAXElement> T addNew(Class<T> aClass, String id) {
		return model.addNew(aClass, id);
	}

	public boolean contains(BioPAXElement aBioPAXElement) {
		return model.contains(aBioPAXElement);
	}
	
	public boolean containsID(String id) {
		return model.containsID(id);
	}

	@Transient
	public BioPAXElement getByID(String id) {
		return model.getByID(id);
	}

	/**
	 * @deprecated
	 */
	@Transient
	public Map<String, BioPAXElement> getIdMap() {
		return model.getIdMap();
	}

	@Basic
	public BioPAXLevel getLevel() {
		return BioPAXLevel.L2;
	}

	@CollectionOfElements
	@JoinTable(name="l2model_ns", joinColumns = @JoinColumn(name="model_id"))
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@org.hibernate.annotations.MapKey( columns = { @Column( name="ns" ) } )
	@Column(name="namespace")
	public Map<String, String> getNameSpacePrefixMap() {
		return model.getNameSpacePrefixMap();
	}
	
	public void setNameSpacePrefixMap(Map<String, String> nspMap) {
		model.getNameSpacePrefixMap().clear();
		model.getNameSpacePrefixMap().putAll(nspMap);
	}

    @ManyToMany(cascade = {CascadeType.ALL}, targetEntity = Level2ElementProxy.class)
	@JoinColumn(name="l2model_l2element")
	public Set<BioPAXElement> getObjects() {
		return model.getObjects();
	}

    public void setObjects(Set<BioPAXElement> objects) {
    	for(BioPAXElement bpe : model.getObjects()) 
    		model.remove(bpe);
    	for(BioPAXElement bpe : objects)
    		model.add(bpe);
    }
    
	public <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy) {
		return model.getObjects(filterBy);
	}

    @Transient
	public boolean isAddDependencies() {
		return model.isAddDependencies();
	}

	public void remove(BioPAXElement aBioPAXElement) {
		model.remove(aBioPAXElement);
	}

	public void setAddDependencies(boolean value) {
		model.setAddDependencies(value);
	}

	@Transient
	public void setFactory(BioPAXFactory factory) {
		model.setFactory(factory);
	}

	public void updateID(String oldID, String newID) {
		model.updateID(oldID, newID);
	}

}