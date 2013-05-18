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
package org.sonatype.nexus.integrationtests.nexus874;

import static org.sonatype.nexus.test.utils.ResponseMatchers.respondsWithStatusCode;

import java.io.IOException;
import java.net.ConnectException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;

/**
 * Validate the MemoryRealm that replaces default nexus security
 */
public class Nexus874SecurityRealmReplacementIT
    extends AbstractNexusIntegrationTest
{
    private GroupMessageUtil groupUtil;

    private RepositoryMessageUtil repoUtil;

    private RoleMessageUtil roleUtil;

    private UserMessageUtil userUtil;
    
    @BeforeClass
    public static void setSecureTest()
        throws ComponentLookupException
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Before
    public void setUp()
    {
        groupUtil = new GroupMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        repoUtil = new RepositoryMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        // targetUtil = new TargetMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        roleUtil = new RoleMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        userUtil = new UserMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void authentication()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        getNexusStatusUtil().getNexusStatus();

        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );

        getNexusStatusUtil().getNexusStatus();

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );

        getNexusStatusUtil().getNexusStatus();
    }

    @Test
    public void negativeAuthentication()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );

        try
        {
            getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            // good
        }

        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );

        try
        {
            getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            // good
        }

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );

        try
        {
            getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            // good
        }
    }

    public void getNexusStatus()
        throws IOException
    {
        try
        {
            RequestFacade.doGet( "service/local/status" );
        }
        catch ( AssertionError e )
        {
            // unsuccessful response
            throw new ConnectException( e.getMessage() );
        }
    }

    @Test
    public void authorization()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        getNexusStatusUtil().getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();
        TargetMessageUtil.getList();
        TaskScheduleUtil.getTasks();

        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );

        getNexusStatusUtil().getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );

        getNexusStatusUtil().getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();
    }

    @Test
    public void negativeAuthorization()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );

        String serviceURI = "service/local/schedules";

        RequestFacade.doGet( "service/local/repo_targets", respondsWithStatusCode( 403 ) );

        RequestFacade.doGet( serviceURI, respondsWithStatusCode( 403 ) );

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );

        RequestFacade.doGet( "service/local/repo_targets", respondsWithStatusCode( 403 ) );

        RequestFacade.doGet( serviceURI, respondsWithStatusCode( 403 ) );

    }
}
