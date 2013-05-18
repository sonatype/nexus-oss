/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty;

import java.io.File;
import java.net.BindException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.jetty.server.Server;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.appcontext.internal.ContextStringDumper;
import org.sonatype.sisu.jetty.mangler.ContextAttributeGetterMangler;
import org.sonatype.sisu.jetty.mangler.ContextGetterMangler;
import org.sonatype.sisu.jetty.mangler.JettyGetterMangler;
import org.sonatype.sisu.jetty.thread.InstrumentedQueuedThreadPool;
import org.sonatype.sisu.jetty.util.JettyUtils;

public class JettyConfigurationTest
    extends AbstractJettyConfigurationTest
{
    @Test
    public void testKeyInclusion()
        throws Exception
    {
        System.setProperty( "foo", "fooVal" );
        System.setProperty( "bar", "barVal" );
        System.setProperty( "none", "noneVal" );
        System.setProperty( JettyUtils.JETTY_CONTEXT_INCLUDE_KEYS_KEY, "foo,bar" );

        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );

        Assert.assertTrue( subject.getAppContext().containsKey( "foo" ) );
        Assert.assertEquals( "fooVal", subject.getAppContext().get( "foo" ) );
        Assert.assertTrue( subject.getAppContext().containsKey( "bar" ) );
        Assert.assertEquals( "barVal", subject.getAppContext().get( "bar" ) );
        Assert.assertFalse( subject.getAppContext().containsKey( "none" ) );
    }

    @Test
    public void testKeyInclusionIsOverriddenByDirectMap()
        throws Exception
    {
        System.setProperty( "foo", "fooVal" );
        System.setProperty( "bar", "barVal" );
        System.setProperty( "none", "noneVal" );
        System.setProperty( JettyUtils.JETTY_CONTEXT_INCLUDE_KEYS_KEY, "foo,bar" );

        final HashMap<String, Object> ctx = new HashMap<String, Object>();
        ctx.put( "foo", "override" );
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ), null, ctx );

        Assert.assertTrue( subject.getAppContext().containsKey( "foo" ) );
        Assert.assertEquals( "override", subject.getAppContext().get( "foo" ) );
        Assert.assertTrue( subject.getAppContext().containsKey( "bar" ) );
        Assert.assertEquals( "barVal", subject.getAppContext().get( "bar" ) );
        Assert.assertFalse( subject.getAppContext().containsKey( "none" ) );
    }

    @Test
    public void testSimpleStart1()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );

        subject.startJetty();

        Assert.assertNotNull( "must be returned", subject.mangleServer( new JettyGetterMangler() ) );

        Assert.assertNotNull( "/context1 exists!", subject.mangleServer( new ContextGetterMangler( "/context1" ) ) );
        Assert.assertNotNull( "/context2 exists!", subject.mangleServer( new ContextGetterMangler( "/context2" ) ) );
        Assert.assertNull( "/context3 does NOT exists!", subject.mangleServer( new ContextGetterMangler( "/context3" ) ) );

        Assert.assertNotNull( "/context1 does have attribute!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context1", "foo" ) ) );
        Assert.assertNull( "/context2 does NOT have attributes!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context2", "foo" ) ) );
        Assert.assertNull( "/context3 does NOT exists!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context3", "foo" ) ) );

        subject.stopJetty();
    }

    // skip this for now
    @Test
    @Ignore
    public void NOtestSimpleStart2()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-with-ajp.xml" ) ) );

        subject.startJetty();

        Assert.assertNotNull( "must be returned", subject.mangleServer( new JettyGetterMangler() ) );

        Assert.assertNotNull( "/context1 exists!", subject.mangleServer( new ContextGetterMangler( "/context1" ) ) );
        Assert.assertNotNull( "/context2 exists!", subject.mangleServer( new ContextGetterMangler( "/context2" ) ) );
        Assert.assertNull( "/context3 does NOT exists!", subject.mangleServer( new ContextGetterMangler( "/context3" ) ) );

        Assert.assertNotNull( "/context1 does have attribute!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context1", "foo" ) ) );
        Assert.assertNull( "/context2 does NOT have attributes!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context2", "foo" ) ) );
        Assert.assertNull( "/context3 does NOT exists!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context3", "foo" ) ) );

        subject.stopJetty();
    }

    // skip this for now
    @Test
    @Ignore
    public void NOtestSimpleStart3()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-with-rewrite-handler.xml" ) ) );

        subject.startJetty();

        Assert.assertNotNull( "must be returned", subject.mangleServer( new JettyGetterMangler() ) );

        Assert.assertNotNull( "/context1 exists!", subject.mangleServer( new ContextGetterMangler( "/context1" ) ) );
        Assert.assertNotNull( "/context2 exists!", subject.mangleServer( new ContextGetterMangler( "/context2" ) ) );
        Assert.assertNull( "/context3 does NOT exists!", subject.mangleServer( new ContextGetterMangler( "/context3" ) ) );

        Assert.assertNotNull( "/context1 does have attribute!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context1", "foo" ) ) );
        Assert.assertNull( "/context2 does NOT have attributes!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context2", "foo" ) ) );
        Assert.assertNull( "/context3 does NOT exists!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context3", "foo" ) ) );

        subject.stopJetty();
    }

    @Test
    public void testSimpleStartWithExtraContext()
        throws Exception
    {
        final Map<String, String> ctx1 = new HashMap<String, String>();
        ctx1.put( "one", "1" );
        final Map<String, String> ctx2 = new HashMap<String, String>();
        ctx2.put( "two", "2" );
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ), null, ctx1, ctx2 );

        subject.startJetty();

        Assert.assertNotNull( "must be returned", subject.mangleServer( new JettyGetterMangler() ) );

        Assert.assertNotNull( "/context1 exists!", subject.mangleServer( new ContextGetterMangler( "/context1" ) ) );
        Assert.assertNotNull( "/context2 exists!", subject.mangleServer( new ContextGetterMangler( "/context2" ) ) );
        Assert.assertNull( "/context3 does NOT exists!", subject.mangleServer( new ContextGetterMangler( "/context3" ) ) );

        Assert.assertNotNull( "/context1 does have attribute!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context1", "foo" ) ) );
        Assert.assertNull( "/context2 does NOT have attributes!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context2", "foo" ) ) );
        Assert.assertNull( "/context3 does NOT exists!",
            subject.mangleServer( new ContextAttributeGetterMangler( "/context3", "foo" ) ) );

        subject.stopJetty();

        System.out.println( ContextStringDumper.dumpToString( subject.getAppContext() ) );
        // we have 4 in props file, and 2 in extra contexts
        Assert.assertEquals( "Context is not correctly set!", 6, subject.getAppContext().size() );
    }

    @Test
    public void testSimpleStartWithInstrumentedThreadPool()
        throws Exception
    {
        final Map<String, String> ctx1 = new HashMap<String, String>();
        ctx1.put( "one", "1" );
        final Map<String, String> ctx2 = new HashMap<String, String>();
        ctx2.put( "two", "2" );
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-instrumented-pool.xml" ) ), null, ctx1, ctx2 );

        subject.startJetty();

        final Server server = subject.mangleServer( new JettyGetterMangler() );

        Assert.assertTrue( "Unexpected pool class!", server.getThreadPool() instanceof InstrumentedQueuedThreadPool );

        subject.stopJetty();
    }

    @Test
    public void testStartStopConsecutively()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );
        subject.startJetty();
        subject.stopJetty();
        subject.startJetty();
        subject.stopJetty();

        // we should get here without exception
    }

    @Test
    public void testStartStopConsecutivelyWrongOrder1()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );

        try
        {
            subject.startJetty();

            subject.startJetty();

            Assert.fail( "We should not get here!" );
        }
        catch ( IllegalStateException e )
        {
            // good
        }
        finally
        {
            subject.stopJetty();
        }
    }

    @Test
    public void testStartStopConsecutivelyWrongOrder2()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );

        try
        {
            subject.stopJetty();

            Assert.fail( "We should not get here!" );
        }
        catch ( IllegalStateException e )
        {
            // good
        }
    }

    @Test
    public void testStartStopConsecutivelyWrongOrder3()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );

        try
        {
            subject.startJetty();

            subject.stopJetty();

            subject.stopJetty();

            Assert.fail( "We should not get here!" );
        }
        catch ( IllegalStateException e )
        {
            // good
        }
    }

    @Test
    public void testCanStopAfterStartupFailure()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-misconfiguration.xml" ) ) );

        try
        {
            subject.startJetty();

            Assert.fail( "We should not get here!" );
        }
        catch ( BindException e )
        {
            // expected
        }
        finally
        {
            try
            {
                subject.stopJetty();

                Assert.fail( "We should not get here!" );
            }
            catch ( BindException e )
            {
                // expected
            }
        }
    }
}
