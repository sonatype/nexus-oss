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
        configuration.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
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
