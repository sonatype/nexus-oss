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
package org.sonatype.security.realms.privileges.application;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.realms.privileges.AbstractPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.realms.validator.SecurityValidationContext;

@Singleton
@Typed( PrivilegeDescriptor.class )
@Named( "ApplicationPrivilegeDescriptor" )
public class ApplicationPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    public static final String TYPE = "method";

    private final PrivilegePropertyDescriptor methodProperty;

    private final PrivilegePropertyDescriptor permissionProperty;

    @Inject
    public ApplicationPrivilegeDescriptor( @Named( "ApplicationPrivilegeMethodPropertyDescriptor" ) PrivilegePropertyDescriptor methodProperty,
                                           @Named( "ApplicationPrivilegePermissionPropertyDescriptor" ) PrivilegePropertyDescriptor permissionProperty )
    {
        this.methodProperty = methodProperty;
        this.permissionProperty = permissionProperty;
    }

    public String getName()
    {
        return "Application";
    }

    public String getType()
    {
        return TYPE;
    }

    public List<PrivilegePropertyDescriptor> getPropertyDescriptors()
    {
        List<PrivilegePropertyDescriptor> propertyDescriptors = new ArrayList<PrivilegePropertyDescriptor>();

        propertyDescriptors.add( methodProperty );
        propertyDescriptors.add( permissionProperty );

        return propertyDescriptors;
    }

    public String buildPermission( CPrivilege privilege )
    {
        if ( !TYPE.equals( privilege.getType() ) )
        {
            return null;
        }

        String permission = getProperty( privilege, ApplicationPrivilegePermissionPropertyDescriptor.ID );
        String method = getProperty( privilege, ApplicationPrivilegeMethodPropertyDescriptor.ID );

        if ( StringUtils.isEmpty( permission ) )
        {
            permission = "*:*";
        }

        if ( StringUtils.isEmpty( method ) )
        {
            method = "*";
        }

        return permission + ":" + method;
    }

    @Override
    public ValidationResponse validatePrivilege( CPrivilege privilege, SecurityValidationContext ctx, boolean update )
    {
        ValidationResponse response = super.validatePrivilege( privilege, ctx, update );

        if ( !TYPE.equals( privilege.getType() ) )
        {
            return response;
        }

        // validate method
        // method is of form ('*' | 'read' | 'create' | 'update' | 'delete' [, method]* )
        // so, 'read' method is correct, but so is also 'create,update,delete'
        // '*' means ALL POSSIBLE value for this "field"
        String method = null;
        String permission = null;

        for ( CProperty property : (List<CProperty>) privilege.getProperties() )
        {
            if ( property.getKey().equals( ApplicationPrivilegeMethodPropertyDescriptor.ID ) )
            {
                method = property.getValue();
            }
            else if ( property.getKey().equals( ApplicationPrivilegePermissionPropertyDescriptor.ID ) )
            {
                permission = property.getValue();
            }
        }

        if ( StringUtils.isEmpty( permission ) )
        {
            response.addValidationError( "Permission cannot be empty on a privilege!" );
        }

        if ( StringUtils.isEmpty( method ) )
        {
            response.addValidationError( "Method cannot be empty on a privilege!" );
        }
        else
        {
            String[] methods = null;

            if ( method.contains( "," ) )
            {
                // it is a list of methods
                methods = method.split( "," );
            }
            else
            {
                // it is a single method
                methods = new String[] { method };
            }

            boolean valid = true;

            for ( String singlemethod : methods )
            {
                if ( !"create".equals( singlemethod ) && !"delete".equals( singlemethod )
                    && !"read".equals( singlemethod ) && !"update".equals( singlemethod ) && !"*".equals( singlemethod ) )
                {
                    valid = false;

                    break;
                }
            }

            if ( !valid )
            {
                ValidationMessage message =
                    new ValidationMessage( "method", "Privilege ID '" + privilege.getId()
                        + "' Method is wrong! (Allowed methods are: create, delete, read and update)",
                                           "Invalid method selected." );
                response.addValidationError( message );
            }
        }

        return response;
    }
}
