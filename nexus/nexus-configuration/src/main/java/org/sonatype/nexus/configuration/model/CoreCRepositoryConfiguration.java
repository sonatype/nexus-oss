package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;

import com.thoughtworks.xstream.XStream;

public class CoreCRepositoryConfiguration
    implements CoreConfiguration
{
    private CRepository configuration;

    private CRepository changedConfiguration;

    private XStream xstream = new XStream();

    public CoreCRepositoryConfiguration( CRepository configuration )
    {
        this.configuration = configuration;
    }

    public CRepository getConfiguration( boolean forWrite )
    {
        if ( forWrite )
        {
            if ( configuration != null && changedConfiguration == null )
            {
                // copy it, in a VERY CRUDE WAY :)
                changedConfiguration = (CRepository) xstream.fromXML( xstream.toXML( configuration ) );

                changedConfiguration.externalConfigurationImple = configuration.externalConfigurationImple;
            }

            return changedConfiguration;
        }
        else
        {
            return configuration;
        }
    }

    public ExternalConfiguration getExternalConfiguration()
    {
        return configuration.externalConfigurationImple;
    }

    public boolean isDirty()
    {
        return changedConfiguration != null;
    }

    public void applyChanges()
    {
        if ( isDirty() )
        {
            configuration = (CRepository) xstream.fromXML( xstream.toXML( changedConfiguration ), configuration );
        }

        changedConfiguration = null;

        if ( getExternalConfiguration() != null && getExternalConfiguration().isDirty() )
        {
            getExternalConfiguration().applyChanges();
        }
    }

    public void rollbackChanges()
    {
        this.changedConfiguration = null;
    }
}
