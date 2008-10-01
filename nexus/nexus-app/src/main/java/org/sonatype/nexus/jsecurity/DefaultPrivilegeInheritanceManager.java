/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.jsecurity;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.security.model.CPrivilege;

@Component( role = PrivilegeInheritanceManager.class )
public class DefaultPrivilegeInheritanceManager
    implements PrivilegeInheritanceManager
{
    public Set<String> getInheritedMethods( String method )
    {
        HashSet<String> methods = new HashSet<String>();

        methods.add( method );

        if ( CPrivilege.METHOD_CREATE.equals( method ) )
        {
            methods.add( CPrivilege.METHOD_READ );
        }
        else if ( CPrivilege.METHOD_DELETE.equals( method ) )
        {
            methods.add( CPrivilege.METHOD_READ );
        }
        else if ( CPrivilege.METHOD_UPDATE.equals( method ) )
        {
            methods.add( CPrivilege.METHOD_READ );
        }

        return methods;
    }
}
