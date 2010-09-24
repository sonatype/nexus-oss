package org.sonatype.plexus.components.ehcache;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.junit.Assert;

public class DefaultEhCacheWrapperTest
    extends PlexusTestCase
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
            getTestFile( "target/plexus-home/ehcache" ).getAbsoluteFile().getPath(), storePath );
    }

}
