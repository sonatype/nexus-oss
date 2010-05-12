package org.sonatype.security;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractSecurityTest
    extends PlexusTestCase
{

    protected File PLEXUS_HOME = new File( "./target/plexus-home/" );

    protected File APP_CONF = new File( PLEXUS_HOME, "conf" );

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        context.put( "application-conf", APP_CONF.getAbsolutePath() );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        // delete the plexus home dir
        FileUtils.deleteDirectory( PLEXUS_HOME );

        this.getSecuritySystem().start();
        
        super.setUp();
    }

    protected SecuritySystem getSecuritySystem()
        throws Exception
    {   
        return this.lookup( SecuritySystem.class );
    }
}
