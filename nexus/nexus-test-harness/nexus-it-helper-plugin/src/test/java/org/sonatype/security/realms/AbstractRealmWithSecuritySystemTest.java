package org.sonatype.security.realms;

import java.io.File;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.context.Context;
import org.sonatype.nexus.configuration.application.DefaultNexusConfiguration;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

/**
 * Abstract class for "realm" related tests that uses EHCache, and that does not bring up Nexus component only Security
 * subsystem. Without Nexus (or better {@link DefaultNexusConfiguration} brought up, we have to manually manage EHCache
 * manager component, and cleanly shut it down between tests as EHCache 2.5+ yells without it (violates the
 * "one named manager per JVM").
 * 
 * @author cstamas
 */
public abstract class AbstractRealmWithSecuritySystemTest
    extends AbstractRealmTest
{
    private SecuritySystem securitySystem;

    private CacheManagerComponent cacheManagerComponent;

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setAutoWiring( true );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    @Override
    protected void customizeContext( final Context ctx )
    {
        super.customizeContext( ctx );
        ctx.put( "application-conf", getConfDir().getAbsolutePath() );
        ctx.put( "security-xml-file", getConfDir().getAbsolutePath() + "/security.xml" );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        securitySystem = lookup( SecuritySystem.class );
        cacheManagerComponent = lookup( CacheManagerComponent.class );
    }

    protected void tearDown()
        throws Exception
    {
        if ( securitySystem != null )
        {
            securitySystem.stop();
        }
        if ( cacheManagerComponent != null )
        {
            cacheManagerComponent.shutdown();
        }
        super.tearDown();
    }

    protected abstract File getConfDir();

}
