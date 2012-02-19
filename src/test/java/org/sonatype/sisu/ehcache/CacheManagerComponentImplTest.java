package org.sonatype.sisu.ehcache;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.source.MapEntrySource;
import org.sonatype.guice.bean.containers.InjectedTestCase;

import com.google.inject.Binder;
import com.google.inject.Key;

public class CacheManagerComponentImplTest
    extends InjectedTestCase
{
    @Override
    public void configure( Binder binder )
    {
        final HashMap<String, String> aMap = new HashMap<String, String>();
        aMap.put( "foo", "FOO" );
        aMap.put( "bar", "BAR" );
        aMap.put( "basedir", "." );
        final AppContextRequest req = Factory.getDefaultRequest();
        req.getSources().add( new MapEntrySource( "aMAp", aMap ) );

        final AppContext appContext = Factory.create( req );

        binder.bind( Key.get( AppContext.class ) ).toInstance( appContext );
    }

    public void testConfigFromClasspath()
        throws Exception
    {
        // look it up
        CacheManagerComponentImpl cacheWrapper = (CacheManagerComponentImpl) lookup( CacheManagerComponent.class );

        // check the store path, since the config from src/test/resources should be picked up
        String storePath = cacheWrapper.getCacheManager().getDiskStorePath();

        // it has to be absolute
        Assert.assertTrue( "Invalid path " + storePath, new File( storePath ).isAbsolute() );

        // it has to be Interpolated
        Assert.assertFalse( "The path is not interpolated? " + storePath,
            storePath.contains( "${" ) || storePath.contains( "}" ) );

        // it has to point where we did set it (${basedir}/target/plexus-home/ehcache)
        Assert.assertEquals( "The store path does not point where we set it!", new File( getBasedir(),
            "target/plexus-home/ehcache" ).getAbsoluteFile().getPath().toLowerCase(), storePath.toLowerCase() );

        cacheWrapper.shutdown();
    }

}
