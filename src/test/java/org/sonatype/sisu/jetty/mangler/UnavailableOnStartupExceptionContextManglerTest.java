package org.sonatype.sisu.jetty.mangler;

import java.io.File;

import junit.framework.Assert;

import org.sonatype.sisu.jetty.AbstractJettyConfigurationTest;
import org.sonatype.sisu.jetty.Jetty8;

public class UnavailableOnStartupExceptionContextManglerTest
    extends AbstractJettyConfigurationTest
{
    public void testNoPreconfiguredWars()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-with-rewrite-handler.xml" ) ) );
        int affected = subject.mangleServer( new UnavailableOnStartupExceptionContextMangler() );
        Assert.assertEquals( "Config contains no WebApps!", 0, affected );
    }

    public void testOneWar()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-one-war-context.xml" ) ) );
        int affected = subject.mangleServer( new UnavailableOnStartupExceptionContextMangler() );
        Assert.assertEquals( "One WAR needs to be set!", 1, affected );
    }

    public void testTwoWars()
        throws Exception
    {
        Jetty8 subject = new Jetty8( new File( getJettyXmlPath( "jetty-two-war-context.xml" ) ) );
        int affected = subject.mangleServer( new UnavailableOnStartupExceptionContextMangler() );
        Assert.assertEquals( "Two WARs needs to be set!", 2, affected );
    }
}
