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
package org.sonatype.jsecurity.realms.url;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;


public class URLUserLocatorTest
    extends PlexusTestCase
{

    public void testBasics() throws Exception
    {
        
        PlexusUserManager userManager = this.lookup( PlexusUserManager.class );
        
        PlexusUser user = userManager.getUser( "ANYBODY" );
        Assert.assertNotNull( user );
        Assert.assertEquals( "url", user.getSource() );
        
        Assert.assertNotNull( userManager.getUser( "RANDOM", "url" ) );
        
        Assert.assertEquals( 1, userManager.searchUsers( new PlexusUserSearchCriteria("abcd"), "url" ).size() );
        
        
    }
    
}
