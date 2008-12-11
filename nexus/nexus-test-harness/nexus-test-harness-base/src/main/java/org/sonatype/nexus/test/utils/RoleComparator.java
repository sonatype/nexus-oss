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
package org.sonatype.nexus.test.utils;

import java.util.Comparator;

import org.sonatype.jsecurity.model.CRole;

/**
 * This only works for equals...
 */
public class RoleComparator
    implements Comparator<CRole>
{

    public int compare( CRole role1, CRole role2 )
    {
        
        // quick outs
        if( role1 == null || role2 == null)
        {
            return -1;
        }
        
        if( role1 == role2 || role1.equals( role2 ))
        {
            return 0;
        }

        if ( role1.getDescription() == null )
        {
            if ( role2.getDescription() != null )
                return -1;
        }
        else if ( !role1.getDescription().equals( role2.getDescription() ) )
            return -1;
        if ( role1.getId() == null )
        {
            if ( role2.getId() != null )
                return -1;
        }
        else if ( !role1.getId().equals( role2.getId() ) )
            return -1;
        if ( role1.getModelEncoding() == null )
        {
            if ( role2.getModelEncoding() != null )
                return -1;
        }
        else if ( !role1.getModelEncoding().equals( role2.getModelEncoding() ) )
            return -1;
        if ( role1.getName() == null )
        {
            if ( role2.getName() != null )
                return -1;
        }
        else if ( !role1.getName().equals( role2.getName() ) )
            return -1;
        if ( role1.getPrivileges() == null )
        {
            if ( role2.getPrivileges() != null )
                return -1;
        }
        else if ( !role1.getPrivileges().equals( role2.getPrivileges() ) )
            return -1;
        if ( role1.getRoles() == null )
        {
            if ( role2.getRoles() != null )
                return -1;
        }
        else if ( !role1.getRoles().equals( role2.getRoles() ) )
            return -1;
        if ( role1.getSessionTimeout() != role2.getSessionTimeout() )
            return -1;
        return 0;
        
    }
}
