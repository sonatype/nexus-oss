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
package org.sonatype.sisu.jetty.mangler;

import junit.framework.Assert;
import org.junit.Test;
import org.sonatype.sisu.jetty.AbstractJettyConfigurationTest;
import org.sonatype.sisu.jetty.Jetty8;

import java.io.File;

public class UnavailableOnStartupExceptionContextManglerTest
    extends AbstractJettyConfigurationTest
{
    @Test
    public void testNoPreconfiguredWars()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-with-rewrite-handler.xml" ) ) );
        int affected = subject.mangleServer( new UnavailableOnStartupExceptionContextMangler() );
        Assert.assertEquals( "Config contains no WebApps!", 0, affected );
    }

    @Test
    public void testOneWar()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-one-war-context.xml" ) ) );
        int affected = subject.mangleServer( new UnavailableOnStartupExceptionContextMangler() );
        Assert.assertEquals( "One WAR needs to be set!", 1, affected );
    }

    @Test
    public void testTwoWars()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );
        int affected = subject.mangleServer( new UnavailableOnStartupExceptionContextMangler() );
        Assert.assertEquals( "Two WARs needs to be set!", 2, affected );
    }
}
