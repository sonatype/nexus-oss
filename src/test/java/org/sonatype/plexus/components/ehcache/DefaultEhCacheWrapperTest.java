package org.sonatype.plexus.components.ehcache;

import org.codehaus.plexus.PlexusTestCase;
import org.junit.Assert;

public class DefaultEhCacheWrapperTest
    extends PlexusTestCase
{

    public void testLookup()
        throws Exception
    {
        PlexusEhCacheWrapper cacheWrapper = (PlexusEhCacheWrapper) this.lookup( PlexusEhCacheWrapper.class );
        Assert.assertNotNull( cacheWrapper );
    }

}
