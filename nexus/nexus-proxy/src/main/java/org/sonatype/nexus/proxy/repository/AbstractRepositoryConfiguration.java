package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.AbstractXpp3DomExternalConfigurationHolder;

/**
 * A superclass for Repositort External configuratuins.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryConfiguration
    extends AbstractXpp3DomExternalConfigurationHolder
{
    public AbstractRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    @Override
    public void validate( ApplicationConfiguration applicationConfiguration, CoreConfiguration owner )
        throws ConfigurationException
    {
    }
}
