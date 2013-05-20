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
package org.sonatype.security.configuration;

import junit.framework.Assert;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.inject.BeanScanning;
import org.sonatype.sisu.litmus.testsupport.TestUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class SecurityConfigurationManagerTest
    extends InjectedTestCase
{
    private final TestUtil util = new TestUtil(this);

    // FIXME: Upgrade to junit4

    private File PLEXUS_HOME = util.createTempDir("plexus-home");

    private File APP_CONF = new File( PLEXUS_HOME, "conf" );

    @Override
    public void configure( Properties properties )
    {
        properties.put( "application-conf", APP_CONF.getAbsolutePath() );
        super.configure( properties );
    }

    @Override
    public BeanScanning scanning()
    {
        return BeanScanning.INDEX;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        // delete the plexus home dir
        FileUtils.deleteDirectory( PLEXUS_HOME );

        super.setUp();
    }

    //@Test
    public void testLoadEmptyDefaults()
        throws Exception
    {
        SecurityConfigurationManager config = this.lookup( SecurityConfigurationManager.class );

        Assert.assertNotNull( config );

        Assert.assertEquals( "anonymous-pass", config.getAnonymousPassword() );
        Assert.assertEquals( "anonymous-user", config.getAnonymousUsername() );

        Assert.assertEquals( false, config.isAnonymousAccessEnabled() );
        Assert.assertEquals( true, config.isEnabled() );

        List<String> realms = config.getRealms();
        assertThat(realms, containsInAnyOrder(
            "MockRealmA", "MockRealmB", "ExceptionThrowingMockRealm", "FakeRealm1", "FakeRealm2"));
    }

    //@Test
    public void testWrite()
        throws Exception
    {
        SecurityConfigurationManager config = this.lookup( SecurityConfigurationManager.class );

        config.setAnonymousAccessEnabled( true );
        config.setEnabled( false );
        config.setAnonymousPassword( "new-pass" );
        config.setAnonymousUsername( "new-user" );

        List<String> realms = Collections.singletonList("FakeRealm1");
        config.setRealms( realms );

        config.save();

        config.clearCache();

        Assert.assertEquals( "new-pass", config.getAnonymousPassword() );
        Assert.assertEquals( "new-user", config.getAnonymousUsername() );

        Assert.assertEquals( true, config.isAnonymousAccessEnabled() );
        Assert.assertEquals( false, config.isEnabled() );

        realms = config.getRealms();
        Assert.assertEquals( 1, realms.size() );
        Assert.assertEquals( "FakeRealm1", realms.get( 0 ) );
    }
}
