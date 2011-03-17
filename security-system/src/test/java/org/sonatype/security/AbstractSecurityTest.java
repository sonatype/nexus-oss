package org.sonatype.security;

import java.io.File;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.guice.bean.containers.InjectedTestCase;

public abstract class AbstractSecurityTest
    extends InjectedTestCase
{

    protected File PLEXUS_HOME = new File( "./target/plexus-home/" );

    protected File APP_CONF = new File( PLEXUS_HOME, "conf" );

    @Override
    public void configure( Properties properties )
    {
        properties.put( "application-conf", APP_CONF.getAbsolutePath() );
        super.configure( properties );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        // delete the plexus home dir
        FileUtils.deleteDirectory( PLEXUS_HOME );

        this.getSecuritySystem().start();
    }

    protected SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }
}
