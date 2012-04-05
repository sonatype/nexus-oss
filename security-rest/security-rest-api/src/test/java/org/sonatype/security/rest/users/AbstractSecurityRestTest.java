/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.rest.users;

import java.io.File;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

public abstract class AbstractSecurityRestTest
    extends PlexusTestCase
{

    protected static final String REALM_KEY = new MockUserManager().getSource();

    protected static final String WORK_DIR = "target/UserToRolePRTest";

    protected static final String TEST_CONFIG = "target/test-classes/"
        + UserToRolePRTest.class.getName().replaceAll( "\\.", "\\/" ) + "-security.xml";

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
        configuration.setAutoWiring( true );

        super.customizeContainerConfiguration( configuration );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.copyFile( new File( TEST_CONFIG ), new File( WORK_DIR, "/conf/security.xml" ) );

        // start security
        this.lookup( SecuritySystem.class ).start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            lookup( SecuritySystem.class ).stop();
            lookup( CacheManagerComponent.class ).shutdown();
        }
        finally
        {
            super.tearDown();
        }
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "nexus-work", WORK_DIR );
        context.put( "security-xml-file", WORK_DIR + "/conf/security.xml" );
        context.put( "application-conf", WORK_DIR + "/conf/" );
    }
}
