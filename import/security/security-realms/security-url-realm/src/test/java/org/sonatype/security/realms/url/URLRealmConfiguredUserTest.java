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
package org.sonatype.security.realms.url;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class URLRealmConfiguredUserTest
    extends AbstractSecurityTestCase
{

    private String securityXmlPath = "./target/plexus-home/" + this.getClass().getSimpleName() + "/security.xml";

    public void testURLRealmConfiguredUser()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        UserManager urlLocator = this.lookup( UserManager.class, "url" );

        // try to get a normal user to make sure the search is working
        Assert.assertEquals( 1,
                             securitySystem.searchUsers( new UserSearchCriteria( "user1", null, "allConfigured" ) ).size() );

        // make sure we get the URL realm user from this search
        Assert.assertEquals( 1,
                             securitySystem.searchUsers( new UserSearchCriteria( "url-user", null, "allConfigured" ) ).size() );

        // do the search from the URL realm
        Assert.assertEquals( 1, urlLocator.searchUsers( new UserSearchCriteria( "url-user" ) ).size() );

        // do the search using the user manager.
        Assert.assertEquals( 1, securitySystem.searchUsers( new UserSearchCriteria( "url-user" ) ).size() );

        // the list should contain a single user
        Assert.assertTrue( urlLocator.listUserIds().contains( "url-user" ) );
        Assert.assertEquals( 1, urlLocator.listUserIds().size() );
    }

    @Override
    public void configure( Properties properties )
    {
        super.configure( properties );
        properties.put( "security-xml-file", this.securityXmlPath );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy security.xml file into place
        FileUtils.copyFile( new File( "./target/test-classes/configuredUser-security.xml" ),
                            new File( this.securityXmlPath ) );
    }

}
