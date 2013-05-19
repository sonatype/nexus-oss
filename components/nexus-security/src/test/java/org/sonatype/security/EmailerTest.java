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

import junit.framework.Assert;

import org.sonatype.security.email.SecurityEmailer;
import org.sonatype.security.mock.MockEmailer;
import org.sonatype.security.usermanagement.UserNotFoundException;

import com.google.inject.Binder;

public class EmailerTest
    extends AbstractSecurityTest
{
    private MockEmailer emailer = new MockEmailer();

    @Override
    public void configure( Binder binder )
    {
        binder.bind( SecurityEmailer.class ).toInstance( emailer );
    }

    public void testForgotUsername()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        securitySystem.forgotUsername( "cdugas@sonatype.org" );

        Assert.assertTrue( ( (MockEmailer) emailer ).getForgotUserIds().contains( "cdugas" ) );
        Assert.assertEquals( 1, ( (MockEmailer) emailer ).getForgotUserIds().size() );
    }

    public void testDoNotRecoverAnonUserName()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        try
        {
            securitySystem.forgotUsername( "anonymous@sonatype.org" );
            Assert.fail( "UserNotFoundException expected" );
        }
        catch ( UserNotFoundException e )
        {
            // expected
        }

    }

}
