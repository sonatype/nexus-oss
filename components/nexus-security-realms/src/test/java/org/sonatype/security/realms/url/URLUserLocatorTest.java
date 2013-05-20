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

import junit.framework.Assert;

import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class URLUserLocatorTest
    extends AbstractSecurityTestCase
{

    public void testBasics()
        throws Exception
    {

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        User user = securitySystem.getUser( "ANYBODY" );
        Assert.assertNotNull( user );
        Assert.assertEquals( "url", user.getSource() );

        Assert.assertNotNull( securitySystem.getUser( "RANDOM", "url" ) );

        Assert.assertEquals( 1, securitySystem.searchUsers( new UserSearchCriteria( "abcd", null, "url" ) ).size() );

    }

}
