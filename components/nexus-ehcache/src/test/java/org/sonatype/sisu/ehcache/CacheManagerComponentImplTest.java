/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.sisu.ehcache;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.lifecycle.Stoppable;
import org.sonatype.appcontext.source.MapEntrySource;
import org.sonatype.guice.bean.containers.InjectedTestCase;

import com.google.inject.Binder;
import com.google.inject.Key;

public class CacheManagerComponentImplTest
    extends InjectedTestCase
{
    private AppContext appContext;

    private CacheManagerComponent cacheManagerComponent;

    @Override
    public void configure( Binder binder )
    {
        final HashMap<String, String> aMap = new HashMap<String, String>();
        aMap.put( "foo", "FOO" );
        aMap.put( "bar", "BAR" );
        aMap.put( "basedir", "." );
        final AppContextRequest req = Factory.getDefaultRequest();
        req.getSources().add( new MapEntrySource( "aMAp", aMap ) );

        appContext = Factory.create( req );

        binder.bind( Key.get( AppContext.class ) ).toInstance( appContext );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        cacheManagerComponent = lookup( CacheManagerComponent.class );
        appContext.getLifecycleManager().registerManaged( new CacheManagerLifecycleHandler( cacheManagerComponent ) );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            appContext.getLifecycleManager().invokeHandler( Stoppable.class );
        }
        finally
        {
            super.tearDown();
        }
    }

    public void testConfigFromClasspath()
        throws Exception
    {
        // look it up
        CacheManagerComponentImpl cacheWrapper = (CacheManagerComponentImpl) cacheManagerComponent;

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

        // no need for this, this UT employs AppContext lifecycle, see #tearDown
        // cacheWrapper.shutdown();
    }

    public void testConfigFromFile()
        throws Exception
    {
        // stop the one created in setUp() method
        cacheManagerComponent.shutdown();
        
        // look it up
        final File file = new File( new File( getBasedir() ), "src/test/resources/ehcache.xml" );
        CacheManagerComponentImpl cacheWrapper = new CacheManagerComponentImpl( appContext, file );

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

        // no need for this, this UT employs AppContext lifecycle, see #tearDown
        // cacheWrapper.shutdown();
    }

}
