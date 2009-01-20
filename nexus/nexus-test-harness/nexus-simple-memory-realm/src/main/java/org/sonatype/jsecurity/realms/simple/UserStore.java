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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a very simple in memory
 */
public class UserStore
{

    private Map<String, SimpleUser> userMap = new HashMap<String, SimpleUser>();

    public UserStore()
    {
        // NOTE: Using the same userId from another Nexus realm could result in issues (unless your configuration is
        // 100% correct)
        // If you have 2 'jcoder' users, (if the 'jcoder' defined in the primary realm does not have any privileges the
        // user will not be able to login to Nexus.
        // to work around this, just assign the user the correct privileges. 

        SimpleUser admin = new SimpleUser();
        admin.setEmail( "admin-simple@sample.com" );
        admin.setName( "Simple Administrator" );
        admin.setUserId( "admin-simple" );
        admin.setPassword( "admin123" );

        SimpleUser deployment = new SimpleUser();
        deployment.setEmail( "deployment-simple@sample.com" );
        deployment.setName( "Simple Developer" );
        deployment.setUserId( "deployment-simple" );
        deployment.setPassword( "deployment123" );

        SimpleUser anonymous = new SimpleUser();
        anonymous.setEmail( "anonymous-simple@sample.com" );
        anonymous.setName( "Simple Anonymous" );
        anonymous.setUserId( "anonymous-simple" );
        anonymous.setPassword( "anonymous" );

        // put the users in a map
        this.userMap.put( admin.getUserId(), admin );
        this.userMap.put( deployment.getUserId(), deployment );
        this.userMap.put( anonymous.getUserId(), anonymous );

    }

    public Collection<SimpleUser> getAllUsers()
    {
        return this.userMap.values();
    }

    public SimpleUser getUser( String userId )
    {
        return this.userMap.get( userId );
    }

}
