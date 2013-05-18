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
package org.sonatype.security.web;

import junit.framework.Assert;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.security.SecuritySystem;

public class DifferentThreadAuthenticationTestSkipped
    extends AbstractWebSecurityTest
{

    public void testGetSubjectFromThread()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.start();

        // need to bind to a request
        // this.setupLoginContext( "testGetSubjectFromThread" );

        Assert.assertNotNull( securitySystem.login( new UsernamePasswordToken( "jcoder", "jcoder" ) ) );

        // WebUtils.unbindServletRequest();
        // WebUtils.unbindServletResponse();
        //
        // now with the thread
        SubjectRetrievingThread thread = new SubjectRetrievingThread( this );

        thread.setContextClassLoader( null );
        thread.start();
        thread.join( 500 );
        Assert.assertNotNull( thread.getSubject() );
        Subject subject = thread.getSubject();
        Assert.assertTrue( subject.hasRole( "RoleA" ) );

        // if we login again with the jcoder user we should need to bind the request again
        try
        {
            securitySystem.login( new UsernamePasswordToken( "jcoder", "jcoder" ) );
            Assert.fail( "Expected IllegalStateException" );
        }
        catch ( IllegalStateException e )
        {
            // this is not a great exception to catch...
            // but we check the success on the next call
        }

        this.setupLoginContext( "testGetSubjectFromThread-again" );
        subject = securitySystem.login( new UsernamePasswordToken( "jcoder", "jcoder" ) );
        Assert.assertNotNull( subject );

    }

    class SubjectRetrievingThread
        extends Thread
    {
        private InjectedTestCase testCase;

        // private Subject subject;
        private SecuritySystem securitySystem;

        private SubjectRetrievingThread( InjectedTestCase testCase )
        {
            this.testCase = testCase;
            this.securitySystem = testCase.lookup( SecuritySystem.class );
        }

        @Override
        public void run()
        {
            // FIXME: add this back in
            // this.securitySystem.runAs( new SimplePrincipalCollection("jcoder", "") );
        }

        public Subject getSubject()
        {
            return this.securitySystem.getSubject();
        }
    }
}
