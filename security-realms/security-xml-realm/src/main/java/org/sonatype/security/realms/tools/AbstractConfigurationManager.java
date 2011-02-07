package org.sonatype.security.realms.tools;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.security.model.Configuration;

import com.google.inject.Inject;

public abstract class AbstractConfigurationManager
    implements ConfigurationManager
{
    @Inject
    private Logger logger;

    protected Logger getLogger()
    {
        return logger;
    }

    //

    private volatile EnhancedConfiguration configuration = null;

    public synchronized void clearCache()
    {
        configuration = null;
    }

    protected synchronized EnhancedConfiguration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }

        final Configuration newConfiguration = doGetConfiguration();

        // enhancing it
        this.configuration = new EnhancedConfiguration( newConfiguration );

        return this.configuration;
    }

    protected abstract Configuration doGetConfiguration();
}
