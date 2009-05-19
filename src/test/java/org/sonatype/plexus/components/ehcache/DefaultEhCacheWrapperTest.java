package org.sonatype.plexus.components.ehcache;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.junit.Assert;

public class DefaultEhCacheWrapperTest
    extends PlexusTestCase
{

    public void testLookup()
        throws Exception
    {
        PlexusEhCacheWrapper cacheWrapper = this.lookup( PlexusEhCacheWrapper.class );
        Assert.assertNotNull( cacheWrapper );

        String storePath = cacheWrapper.getEhCacheManager().getDiskStorePath();
        Assert.assertTrue( "Invalid path " + storePath, new File( storePath ).isAbsolute() );
    }

}
