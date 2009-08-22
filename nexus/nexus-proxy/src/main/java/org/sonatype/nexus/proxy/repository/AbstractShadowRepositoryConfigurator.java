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

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public abstract class AbstractShadowRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{
    @Override
    public void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
                                      CRepositoryCoreConfiguration coreConfig )
        throws ConfigurationException
    {
        // Shadows are read only
        repository.setWritePolicy( RepositoryWritePolicy.READ_ONLY );
        
        super.doApplyConfiguration( repository, configuration, coreConfig );

        ShadowRepository shadowRepository = repository.adaptToFacet( ShadowRepository.class );

        AbstractShadowRepositoryConfiguration extConf =
            (AbstractShadowRepositoryConfiguration) coreConfig.getExternalConfiguration().getConfiguration( false );

        try
        {
            shadowRepository.setMasterRepositoryId( extConf.getMasterRepositoryId() );
        }
        catch ( IncompatibleMasterRepositoryException e )
        {
            ValidationMessage message =
                new ValidationMessage( "shadowOf", e.getMessage(),
                                       "The source nexus repository is of an invalid Format." );

            ValidationResponse response = new ApplicationValidationResponse();

            response.addValidationError( message );

            throw new InvalidConfigurationException( response );
        }
        catch ( NoSuchRepositoryException e )
        {
            ValidationMessage message =
                new ValidationMessage( "shadowOf", e.getMessage(), "The source nexus repository is not existing." );

            ValidationResponse response = new ApplicationValidationResponse();

            response.addValidationError( message );

            throw new InvalidConfigurationException( response );
        }
    }

}
