/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.rest.roles;

import java.util.Comparator;

import org.sonatype.security.rest.model.RoleAndPrivilegeListResource;

public final class RoleAndPrivilegeListResourceComparator
    implements Comparator<RoleAndPrivilegeListResource>
{
    private final String sort;

    private final String dir;

    public static final String SORT_NAME = "name";

    public static final String SORT_DESCRIPTION = "description";

    public static final String DIR_ASC = "ASC";

    public static final String DIR_DESC = "DESC";

    public RoleAndPrivilegeListResourceComparator( String sort, String dir )
    {
        this.sort = sort;
        this.dir = dir;
    }

    public int compare( RoleAndPrivilegeListResource o1, RoleAndPrivilegeListResource o2 )
    {
        // always sort by roles first, then privileges
        if ( o1.getType().equals( "role" ) && o2.getType().equals( "privilege" ) )
        {
            return -1;
        }
        else if ( o1.getType().equals( "privilege" ) && o2.getType().equals( "role" ) )
        {
            return 1;
        }

        if ( SORT_NAME.equals( sort ) )
        {
            return doCompare( o1.getName(), o2.getName(), dir );
        }
        else if ( SORT_DESCRIPTION.equals( sort ) )
        {
            return doCompare( o1.getDescription(), o2.getDescription(), dir );
        }

        return 0;
    }

    private int doCompare( String value1, String value2, String dir )
    {
        if ( DIR_DESC.equals( dir ) )
        {
            return value2.compareToIgnoreCase( value1 );
        }
        else
        {
            return value1.compareToIgnoreCase( value2 );
        }
    }
}