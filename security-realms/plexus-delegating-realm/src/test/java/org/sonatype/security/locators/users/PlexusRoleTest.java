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
package org.sonatype.security.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.locators.users.PlexusRole;

public class PlexusRoleTest
    extends PlexusTestCase
{

    public void testCompareDifferentId()
    {
        PlexusRole roleA = new PlexusRole();
        roleA.setName( "ID1" );
        roleA.setRoleId( "ID1" );
        roleA.setSource( "source" );

        PlexusRole roleB = new PlexusRole();
        roleB.setName( "ID2" );
        roleB.setRoleId( "ID2" );
        roleB.setSource( "source" );
        
        Assert.assertEquals( -1, roleA.compareTo( roleB ) );
        Assert.assertEquals( 1, roleB.compareTo( roleA ) );

    }
    
    public void testCompareDifferentSource()
    {
        PlexusRole roleA = new PlexusRole();
        roleA.setName( "ID1" );
        roleA.setRoleId( "ID1" );
        roleA.setSource( "source1" );

        PlexusRole roleB = new PlexusRole();
        roleB.setName( "ID1" );
        roleB.setRoleId( "ID1" );
        roleB.setSource( "source2" );
        
        Assert.assertEquals( -1, roleA.compareTo( roleB ) );
        Assert.assertEquals( 1, roleB.compareTo( roleA ) );

    }

}
