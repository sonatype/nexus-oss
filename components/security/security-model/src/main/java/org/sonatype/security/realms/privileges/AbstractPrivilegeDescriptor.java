/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.privileges;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.realms.validator.ConfigurationIdGenerator;
import org.sonatype.security.realms.validator.SecurityValidationContext;

public abstract class AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    @Inject
    private ConfigurationIdGenerator idGenerator;

    protected String getProperty( CPrivilege privilege, String key )
    {
        for ( CProperty property : (List<CProperty>) privilege.getProperties() )
        {
            if ( property.getKey().equals( key ) )
            {
                return property.getValue();
            }
        }

        return null;
    }

    public ValidationResponse validatePrivilege( CPrivilege privilege, SecurityValidationContext ctx, boolean update )
    {
        ValidationResponse response = new ValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        SecurityValidationContext context = (SecurityValidationContext) response.getContext();

        List<String> existingIds = context.getExistingPrivilegeIds();

        if ( existingIds == null )
        {
            context.addExistingPrivilegeIds();

            existingIds = context.getExistingPrivilegeIds();
        }

        if ( !update
            && ( StringUtils.isEmpty( privilege.getId() ) || "0".equals( privilege.getId() ) || ( existingIds.contains( privilege.getId() ) ) ) )
        {
            String newId = idGenerator.generateId();

            ValidationMessage message =
                new ValidationMessage( "id", "Fixed wrong privilege ID from '" + privilege.getId() + "' to '" + newId
                    + "'" );
            response.addValidationWarning( message );

            privilege.setId( newId );

            response.setModified( true );
        }

        if ( StringUtils.isEmpty( privilege.getType() ) )
        {
            ValidationMessage message =
                new ValidationMessage( "type", "Cannot have an empty type", "Privilege cannot have an invalid type" );

            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( privilege.getName() ) )
        {
            ValidationMessage message =
                new ValidationMessage( "name", "Privilege ID '" + privilege.getId() + "' requires a name.",
                                       "Name is required." );
            response.addValidationError( message );
        }

        existingIds.add( privilege.getId() );

        return response;
    }
}
