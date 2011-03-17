package org.sonatype.plexus.components.ehcache;

import java.io.File;

import org.junit.Assert;
import org.sonatype.guice.bean.containers.InjectedTestCase;

public class DefaultEhCacheWrapperTest
    extends InjectedTestCase
{

    public void testConfigFromClasspath()
        throws Exception
    {
        // look it up
        PlexusEhCacheWrapper cacheWrapper = lookup( PlexusEhCacheWrapper.class );

        // check the store path, since the config from src/test/resources should be picked up
        String storePath = cacheWrapper.getEhCacheManager().getDiskStorePath();

        // it has to be absolute
        Assert.assertTrue( "Invalid path " + storePath, new File( storePath ).isAbsolute() );

        // it has to be Interpolated
        Assert.assertFalse( "The path is not interpolated? " + storePath,
                            storePath.contains( "${" ) || storePath.contains( "}" ) );

        // it has to point where we did set it (${basedir}/target/plexus-home/ehcache)
        Assert.assertEquals( "The store path does not point where we set it!",
                             new File( getBasedir(), "target/plexus-home/ehcache" ).getAbsoluteFile().getPath().toLowerCase(),
                             storePath.toLowerCase() );
        
        cacheWrapper.stop();
    }

}
