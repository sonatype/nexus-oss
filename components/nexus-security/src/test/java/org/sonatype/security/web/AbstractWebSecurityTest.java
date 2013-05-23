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
package org.sonatype.security.web;

import static org.easymock.EasyMock.replay;

import java.io.File;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.plexus.util.FileUtils;
import org.easymock.EasyMock;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.inject.BeanScanning;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

public abstract class AbstractWebSecurityTest
    extends InjectedTestCase
{

    protected File PLEXUS_HOME = new File( "./target/plexus-home/" );

    protected File APP_CONF = new File( PLEXUS_HOME, "conf" );

    @Override
    public void configure( Properties properties )
    {
        super.configure( properties );
        properties.put( "application-conf", APP_CONF.getAbsolutePath() );
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
        super.setUp();

        // delete the plexus home dir
        FileUtils.deleteDirectory( PLEXUS_HOME );

        this.getSecuritySystem().start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            this.lookup( CacheManagerComponent.class ).shutdown();
        }
        finally
        {
            super.tearDown();
        }
    }

    protected SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }

    protected void setupLoginContext( String sessionId )
    {
        HttpServletRequest mockRequest = EasyMock.createNiceMock( HttpServletRequest.class );
        HttpServletResponse mockResponse = EasyMock.createNiceMock( HttpServletResponse.class );
        HttpSession mockSession = EasyMock.createNiceMock( HttpSession.class );

        EasyMock.expect( mockSession.getId() ).andReturn( sessionId ).anyTimes();
        EasyMock.expect( mockRequest.getCookies() ).andReturn( null ).anyTimes();
        EasyMock.expect( mockRequest.getSession() ).andReturn( mockSession ).anyTimes();
        EasyMock.expect( mockRequest.getSession( false ) ).andReturn( mockSession ).anyTimes();
        replay( mockSession );
        replay( mockRequest );

        // // we need to bind for the "web" impl of the PlexusSecurityManager to work
        // WebUtils.bind( mockRequest );
        // WebUtils.bind( mockResponse );
    }

}
