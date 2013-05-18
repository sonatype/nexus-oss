/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security;

import java.io.File;
import java.util.Properties;

import org.apache.shiro.util.ThreadContext;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.inject.BeanScanning;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

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

        getSecuritySystem().start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            getSecuritySystem().stop();
            lookup( CacheManagerComponent.class ).shutdown();
        }
        finally
        {
            ThreadContext.remove();
            super.tearDown();
        }
    }

    @Override
    public BeanScanning scanning()
    {
        return BeanScanning.INDEX;
    }

    protected SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }
}
