package org.biopax.paxtools.model;

import org.biopax.paxtools.impl.level2.Level2FactoryImpl;
import org.biopax.paxtools.impl.level3.Level3FactoryImpl;

import java.io.InputStream;

/**
 * Enumeration type for BioPAX levels.
 */

public enum BioPAXLevel
{
    L1(1,
       "biopax-level1.owl",
       Level2FactoryImpl.class,
       "org.biopax.paxtools.model.level2"),
    L2(2,
       "biopax-level2.owl",
       Level2FactoryImpl.class,
       "org.biopax.paxtools.model.level2"),
    L3(3,
       "biopax-level3.owl",
       Level3FactoryImpl.class,
       "org.biopax.paxtools.model.level3");

// ------------------------------ FIELDS ------------------------------

    private final String filename;

    private BioPAXFactory factory;
    private final int value;
    private final String packageName;
    /**
     * This is the prefix used for all biopax releases.
     */
    public static final String BP_PREFIX = "http://www.biopax.org/release/";

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor
     *
     * @param level        level number
     * @param filename     File name of the owl file.
     * @param factoryClass default factory class
     * @param pm           package name of the model implementation
     */
    BioPAXLevel(int level, String filename,
                Class<? extends BioPAXFactory> factoryClass,
                String pm)
    {
        this.value = level;
        this.filename = filename;
        this.packageName = pm;
        try
        {
            this.factory = factoryClass.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * This method returns the filename of the owl file
     * @return the filename of the owl file
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * This method returns the default factory for this level
     * @return he default factory for this level
     */
    public BioPAXFactory getDefaultFactory()
    {
        return factory;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * This method loads the level file as resource and returns it as
     * an input stream
     * @return an input stream from the owl file.
     */
    public InputStream getLevelFileAsStream()
    {
        return this.getClass().getResourceAsStream(filename);
    }

// --------------------- ACCESORS and MUTATORS---------------------

    /**
     * This method returns the namespace defined for this level.
     * @return namespace defined for this level.
     */
    public String getNameSpace()
    {
        return BP_PREFIX + filename + "#";
    }

    /**
     * This method returns true if the given string starts with the
     * BP_PREFIX
     * @param nameSpace to be checked
     * @return rue if the given string starts with the BP_PREFIX
     */
    public static boolean isInBioPAXNameSpace(String nameSpace)
    {
        return nameSpace != null && nameSpace.startsWith(BP_PREFIX);
    }

	public static BioPAXLevel getLevelFromNameSpace(String namespace)
	{
		if (isInBioPAXNameSpace(namespace))
		{
			{
				for (BioPAXLevel level : BioPAXLevel.values())
				{
					if (namespace.equalsIgnoreCase(level.getNameSpace()))
					{
						return level;
					}
				}
			}
		}
		return null;
	}
    
    public int getValue()
    {
        return value;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public boolean hasElement(BioPAXElement element)
    {
        return element.getModelInterface().getPackage().getName().equals(
                this.packageName);
    }
}
