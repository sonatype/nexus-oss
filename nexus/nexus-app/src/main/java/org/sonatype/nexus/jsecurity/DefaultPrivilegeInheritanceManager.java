/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.jsecurity;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = PrivilegeInheritanceManager.class )
public class DefaultPrivilegeInheritanceManager
    implements PrivilegeInheritanceManager
{
    public Set<String> getInheritedMethods( String method )
    {
        HashSet<String> methods = new HashSet<String>();

        methods.add( method );

        if ( "create".equals( method ) )
        {
            methods.add( "read" );
        }
        else if ( "delete".equals( method ) )
        {
            methods.add( "read" );
        }
        else if ( "update".equals( method ) )
        {
            methods.add( "read" );
        }

        return methods;
    }
}
