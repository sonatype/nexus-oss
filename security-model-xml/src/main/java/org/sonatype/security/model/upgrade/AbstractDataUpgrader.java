package org.sonatype.security.model.upgrade;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;

/**
 * An abstract SingleVersionDataUpgrader that is used to validate the <code>configuration</code> object's class and correctly cast it.
 */
public abstract class AbstractDataUpgrader<C>
    implements SingleVersionDataUpgrader
{

    public abstract void doUpgrade( C configuration )
        throws ConfigurationIsCorruptedException;

    @SuppressWarnings("unchecked")
    public void upgrade( Object configuration )
        throws ConfigurationIsCorruptedException
    {
        //TODO type check
        
        // template
        this.doUpgrade( (C) configuration );
    }
}
