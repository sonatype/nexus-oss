/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.jsecurity.realms.simple;

import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class SimpleUserLocatorTest
    extends PlexusTestCase
{

    public void testLocatorLookup()
        throws Exception
    {
        // a bit of plexus back ground, this is how you can look up a component from a test class
        this.lookup( UserManager.class, "Simple" );
    }

    public void testSearch()
        throws Exception
    {
        UserManager userLocator = this.lookup( UserManager.class, "Simple" );

        Set<User> result = userLocator.searchUsers( new UserSearchCriteria( "adm" ) );
        Assert.assertEquals( 1, result.size() );
        // your test could be a bit more robust
        Assert.assertEquals( result.iterator().next().getUserId(), "admin-simple" );
    }

    public void testIdList()
        throws Exception
    {
        UserManager userLocator = this.lookup( UserManager.class, "Simple" );

        Set<String> ids = userLocator.listUserIds();

        Assert.assertTrue( ids.contains( "admin-simple" ) );
        Assert.assertTrue( ids.contains( "deployment-simple" ) );
        Assert.assertTrue( ids.contains( "anonymous-simple" ) );

        Assert.assertEquals( 3, ids.size() );
    }

    public void testUserList()
        throws Exception
    {
        UserManager userLocator = this.lookup( UserManager.class, "Simple" );

        Set<User> users = userLocator.listUsers();
        // your test could be a bit more robust
        Assert.assertEquals( 3, users.size() );
    }

}
