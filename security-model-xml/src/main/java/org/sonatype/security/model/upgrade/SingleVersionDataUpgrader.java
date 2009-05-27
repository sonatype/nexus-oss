package org.sonatype.security.model.upgrade;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;

public interface SingleVersionDataUpgrader
{
    void upgrade( Object configuration )
        throws ConfigurationIsCorruptedException;
}
