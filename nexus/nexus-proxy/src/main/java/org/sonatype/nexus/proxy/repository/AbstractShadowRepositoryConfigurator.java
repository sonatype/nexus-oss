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
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public abstract class AbstractShadowRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{
    @Override
    public void doConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws InvalidConfigurationException
    {
        ShadowRepository shadowRepository = repository.adaptToFacet( ShadowRepository.class );

        try
        {
            Repository masterRepository = getRepositoryRegistry().getRepository(
                externalConfiguration.getChild( "masterRepository" ).getValue() );

            shadowRepository.setMasterRepository( masterRepository );
        }
        catch ( IncompatibleMasterRepositoryException e )
        {
            ValidationMessage message = new ValidationMessage(
                "shadowOf",
                e.getMessage(),
                "The source nexus repository is of an invalid Format." );

            ValidationResponse response = new ApplicationValidationResponse();

            response.addValidationError( message );

            throw new InvalidConfigurationException( response );
        }
        catch ( NoSuchRepositoryException e )
        {
            ValidationMessage message = new ValidationMessage(
                "shadowOf",
                e.getMessage(),
                "The source nexus repository is not existing." );

            ValidationResponse response = new ApplicationValidationResponse();

            response.addValidationError( message );

            throw new InvalidConfigurationException( response );
        }
        catch ( PlexusConfigurationException e )
        {
            throw new InvalidConfigurationException( "Could not read the configuration!", e );
        }
    }

}
