package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;

import java.util.HashSet;
import java.util.Set;

/**
 */
class ComplexImpl extends PhysicalEntityImpl implements Complex
{
// ------------------------------ FIELDS ------------------------------

	private Set<PhysicalEntity> component;
	private Set<Stoichiometry> componentStoichiometry;

// --------------------------- CONSTRUCTORS ---------------------------

	public ComplexImpl()
	{
		this.component = new HashSet<PhysicalEntity>();
		this.componentStoichiometry = new HashSet<Stoichiometry>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends Complex> getModelInterface()
	{
		return Complex.class;
	}

// --------------------- Interface Complex ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<PhysicalEntity> getComponent()
	{
		return component;
	}

	public void addComponent(PhysicalEntity component)
	{
		this.component.add(component);
		component.getComponentOf().add(this);
	}

	public void removeComponent(PhysicalEntity component)
	{
		this.component.remove(component);
		component.getComponentOf().add(null);
	}

	public void setComponent(Set<PhysicalEntity> component)
	{
		this.component = component;
		for (PhysicalEntity PhysicalEntity : component)
		{
			PhysicalEntity.getComponentOf().add(this);
		}
	}

	public Set<Stoichiometry> getComponentStoichiometry()
	{
		return componentStoichiometry;
	}

	public void addComponentStoichiometry(
			Stoichiometry ComponentStoichiometry)
	{

		this.componentStoichiometry.add(ComponentStoichiometry);
	}

	public void removeComponentStoichiometry(
			Stoichiometry ComponentStoichiometry)
	{
		this.componentStoichiometry.remove(ComponentStoichiometry);
	}

	public void setComponentStoichiometry(
			Set<Stoichiometry> ComponentStoichiometry)
	{
		this.componentStoichiometry = ComponentStoichiometry;
	}

	public Class<? extends PhysicalEntity> getPhysicalEntityClass()
	{
		return Complex.class;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		return SetEquivalanceChecker
				.isEquivalent(this.getComponent(), ((Complex) element).getComponent());
	}
}