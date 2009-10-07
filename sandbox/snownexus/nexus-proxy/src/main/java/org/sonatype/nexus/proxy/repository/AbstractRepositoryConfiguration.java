package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.validation.ValidationResponse;
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
    public ValidationResponse doValidateChanges( ApplicationConfiguration applicationConfiguration,
                                                 CoreConfiguration owner, Xpp3Dom configuration )
    {
        return new ValidationResponse();
    }
}
