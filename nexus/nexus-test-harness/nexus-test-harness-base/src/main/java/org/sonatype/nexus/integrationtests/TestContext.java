/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests;

import java.util.HashMap;

public class TestContext
{

    private boolean secureTest = false;

    private String username = "admin";

    private String password = "admin123";
    
    private String adminUsername = "admin";

    private String adminPassword = "admin123";
    
    private boolean useAdminForRequests = true;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public Object getObject( String key )
    {
        return map.get( key );
    }

    public boolean getBoolean( String key )
    {
        if ( map.containsKey( key ) )
        {
            return (Boolean) map.get( key );
        }

        return false;
    }

    public void put( String key, Object value )
    {
        this.map.put( key, value );
    }

    public boolean isSecureTest()
    {
        return secureTest;
    }

    public void setSecureTest( boolean secureTest )
    {
        this.secureTest = secureTest;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getAdminUsername()
    {
        return adminUsername;
    }

    public void setAdminUsername( String adminUsername )
    {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword()
    {
        return adminPassword;
    }

    public void setAdminPassword( String adminPassword )
    {
        this.adminPassword = adminPassword;
    }


    public void useAdminForRequests()
    {
        this.username = this.adminUsername;
        this.password = this.adminPassword;
    }
    
    

}
