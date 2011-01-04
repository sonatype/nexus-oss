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
package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.realms.privileges.AbstractPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.realms.validator.SecurityValidationContext;

@Component( role = PrivilegeDescriptor.class, hint = "RepositoryViewPrivilegeDescriptor" )
public class RepositoryViewPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    public static final String TYPE = "repository";

    @Requirement( role = PrivilegePropertyDescriptor.class, hint = "RepositoryPropertyDescriptor" )
    private PrivilegePropertyDescriptor repoProperty;

    public String getName()
    {
        return "Repository View";
    }

    public List<PrivilegePropertyDescriptor> getPropertyDescriptors()
    {
        List<PrivilegePropertyDescriptor> propertyDescriptors = new ArrayList<PrivilegePropertyDescriptor>();

        propertyDescriptors.add( repoProperty );

        return propertyDescriptors;
    }

    public String getType()
    {
        return TYPE;
    }

    public String buildPermission( CPrivilege privilege )
    {
        if ( !TYPE.equals( privilege.getType() ) )
        {
            return null;
        }

        String repoId = getProperty( privilege, RepositoryPropertyDescriptor.ID );

        if ( StringUtils.isEmpty( repoId ) )
        {
            repoId = "*";
        }

        return buildPermission( NexusItemAuthorizer.VIEW_REPOSITORY_KEY, repoId );
    }

    @Override
    public ValidationResponse validatePrivilege( CPrivilege privilege,
        SecurityValidationContext ctx, boolean update )
    {
        ValidationResponse response = super.validatePrivilege( privilege, ctx, update );

        if ( !TYPE.equals( privilege.getType() ) )
        {
            return response;
        }

        return response;
    }

    public static String buildPermission( String objectType, String objectId )
    {
        return "nexus:view:" + objectType + ":" + objectId;
    }
}
