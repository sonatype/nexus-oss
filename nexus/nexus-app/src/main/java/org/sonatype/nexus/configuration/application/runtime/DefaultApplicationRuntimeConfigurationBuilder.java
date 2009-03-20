/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.application.runtime;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class DefaultRuntimeConfigurationBuilder. Todo: all the bad thing is now concentrated in this class. We are
 * playing container instead of container.
 * 
 * @author cstamas
 */
@Component( role = ApplicationRuntimeConfigurationBuilder.class )
public class DefaultApplicationRuntimeConfigurationBuilder
    extends AbstractLogEnabled
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
