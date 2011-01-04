/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.config;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;

@Singleton
@Named
public class DefaultCapabilityConfigurationValidator
    implements CapabilityConfigurationValidator
{

    public ValidationResponse validate( final CCapability capability, final boolean isCreateMode )
    {
        final ValidationResponse response = new ValidationResponse();

        if ( !isCreateMode && StringUtils.isEmpty( capability.getId() ) )
        {
            final ValidationMessage msg = new ValidationMessage( "id", "Capability ID cannot be empty." );

            response.addValidationError( msg );
        }
        if ( StringUtils.isEmpty( capability.getName() ) )
        {
            final ValidationMessage msg = new ValidationMessage( "name", "Capability name cannot be empty." );

            response.addValidationError( msg );
        }
        if ( StringUtils.isEmpty( capability.getTypeId() ) )
        {
            final ValidationMessage msg = new ValidationMessage( "typeId", "Capability type cannot be empty." );

            response.addValidationError( msg );
        }

        for ( final CCapabilityProperty property : capability.getProperties() )
        {
            if ( StringUtils.isEmpty( property.getKey() ) )
            {
                final ValidationMessage msg =
                    new ValidationMessage( "type", "Capability properties cannot cannot have an empty key." );

                response.addValidationError( msg );
                break;
            }
        }

        return response;
    }

    @SuppressWarnings( "unchecked" )
    public ValidationResponse validateModel( final ValidationRequest request )
    {
        final ValidationResponse response = new ValidationResponse();

        final Configuration configuration = (Configuration) request.getConfiguration();

        if ( configuration == null )
        {
            final ValidationMessage msg = new ValidationMessage( "*", "No configuration available to validate" );

            response.addValidationError( msg );
        }
        else
        {
            for ( final CCapability capability : configuration.getCapabilities() )
            {
                response.append( validate( capability, false ) );
            }
        }

        return response;
    }
}
