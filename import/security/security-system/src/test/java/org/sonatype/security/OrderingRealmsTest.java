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
package org.sonatype.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.sonatype.security.usermanagement.User;

public class OrderingRealmsTest
    extends AbstractSecurityTest
{

    public void testOrderedGetUser()
        throws Exception
    {

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        List<String> realmHints = new ArrayList<String>();
        realmHints.add( "MockRealmA" );
        realmHints.add( "MockRealmB" );
        securitySystem.setRealms( realmHints );

        User jcoder = securitySystem.getUser( "jcoder" );
        Assert.assertNotNull( jcoder );

        // make sure jcoder is from MockUserManagerA
        Assert.assertEquals( "MockUserManagerA", jcoder.getSource() );

        // now change the order
        realmHints.clear();
        realmHints.add( "MockRealmB" );
        realmHints.add( "MockRealmA" );
        securitySystem.setRealms( realmHints );

        jcoder = securitySystem.getUser( "jcoder" );
        Assert.assertNotNull( jcoder );

        // make sure jcoder is from MockUserManagerA
        Assert.assertEquals( "MockUserManagerB", jcoder.getSource() );

    }

}
