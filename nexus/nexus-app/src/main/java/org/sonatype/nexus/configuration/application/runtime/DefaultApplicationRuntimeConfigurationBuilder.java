/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.application.runtime;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class DefaultRuntimeConfigurationBuilder. Todo: all the bad thing is now concentrated in this class. We are
 * playing container instead of container.
 * 
 * @author cstamas
 */
@Component( role = ApplicationRuntimeConfigurationBuilder.class )
public class DefaultApplicationRuntimeConfigurationBuilder
    extends AbstractLoggingComponent
    implements ApplicationRuntimeConfigurationBuilder
{
    @Requirement
    private PlexusContainer plexusContainer;

    public Repository createRepositoryFromModel( Configuration configuration, CRepository repoConf )
        throws ConfigurationException
    {
        Repository repository = createRepository( repoConf.getProviderRole(), repoConf.getProviderHint() );

        repository.configure( repoConf );

        return repository;
    }

    // ----------------------------------------
    // private stuff

    private Repository createRepository( String role, String hint )
        throws InvalidConfigurationException
    {
        try
        {
            return Repository.class.cast( plexusContainer.lookup( role, hint ) );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Could not lookup a new instance of Repository!", e );
        }
    }
}
