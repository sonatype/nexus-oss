/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.jsecurity.realms.privileges.application;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.privileges.AbstractPrivilegeDescriptor;
import org.sonatype.jsecurity.realms.privileges.PrivilegeDescriptor;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.jsecurity.realms.validator.ValidationMessage;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

@Component( role = PrivilegeDescriptor.class, hint = "ApplicationPrivilegeDescriptor" )
public class ApplicationPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    public static final String TYPE = "method";
    
    @Requirement( role = PrivilegePropertyDescriptor.class, hint = "ApplicationPrivilegeMethodPropertyDescriptor" )
    private PrivilegePropertyDescriptor methodProperty;
    
    @Requirement( role = PrivilegePropertyDescriptor.class, hint = "ApplicationPrivilegePermissionPropertyDescriptor" )
    private PrivilegePropertyDescriptor permissionProperty;
    
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
    public ValidationResponse validatePrivilege( CPrivilege privilege, ValidationContext ctx, boolean update )
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
                    && !"read".equals( singlemethod ) && !"update".equals( singlemethod )
                    && !"*".equals( singlemethod ) )
                {
                    valid = false;

                    break;
                }
            }

            if ( !valid )
            {
                ValidationMessage message = new ValidationMessage(
                    "method",
                    "Privilege ID '" + privilege.getId()
                        + "' Method is wrong! (Allowed methods are: create, delete, read and update)",
                    "Invalid method selected." );
                response.addValidationError( message );
            }
        }

        return response;
    }
}
