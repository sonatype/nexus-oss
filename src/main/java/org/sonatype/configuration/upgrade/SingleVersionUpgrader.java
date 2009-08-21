package org.sonatype.configuration.upgrade;

import java.io.File;
import java.io.IOException;

/**
 * A component involved only if old security configuration is found. It will fetch the old configuration, transform it
 * to current Configuration model and return it. Nothing else.
 * 
 * @author cstamas
 */
public interface SingleVersionUpgrader
{
    Object loadConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException;

    void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException;
}
