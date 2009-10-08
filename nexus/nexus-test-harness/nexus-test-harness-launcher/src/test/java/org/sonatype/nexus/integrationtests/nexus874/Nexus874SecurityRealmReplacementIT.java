/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus874;

import java.io.IOException;
import java.net.ConnectException;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
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

    public Nexus874SecurityRealmReplacementIT()
        throws ComponentLookupException
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        groupUtil = new GroupMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        repoUtil =
            new RepositoryMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON, getRepositoryTypeRegistry() );
        // targetUtil = new TargetMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        roleUtil = new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void authentication()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        NexusStatusUtil.getNexusStatus();

        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );

        NexusStatusUtil.getNexusStatus();

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );

        NexusStatusUtil.getNexusStatus();
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
        Response response = RequestFacade.doGetRequest( "service/local/status" );

        if ( !response.getStatus().isSuccess() )
        {
            throw new ConnectException( response.getStatus().toString() );
        }
    }

    @Test
    public void authorization()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        NexusStatusUtil.getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();
        TargetMessageUtil.getList();
        TaskScheduleUtil.getTasks();

        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );

        NexusStatusUtil.getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );

        NexusStatusUtil.getNexusStatus();
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
        
        Response response = RequestFacade.doGetRequest( "service/local/repo_targets" );
        Assert.assertEquals( 403, response.getStatus().getCode() );
        
        response = RequestFacade.doGetRequest( serviceURI );
        Assert.assertEquals( 403, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );

        response = RequestFacade.doGetRequest( "service/local/repo_targets" );
        Assert.assertEquals( 403, response.getStatus().getCode() );

        response = RequestFacade.doGetRequest( serviceURI );
        Assert.assertEquals( 403, response.getStatus().getCode() );

    }
}
