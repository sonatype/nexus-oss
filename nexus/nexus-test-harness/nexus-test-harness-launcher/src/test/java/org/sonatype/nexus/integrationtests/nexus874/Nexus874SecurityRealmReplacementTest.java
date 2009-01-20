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

import java.net.ConnectException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.NexusStateUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;

/**
 * Validate the MemoryRealm that replaces default nexus security
 */
public class Nexus874SecurityRealmReplacementTest
    extends AbstractNexusIntegrationTest
{    
    private GroupMessageUtil groupUtil;
    private RepositoryMessageUtil repoUtil;
    private RoleMessageUtil roleUtil;
    private UserMessageUtil userUtil;
    
    public Nexus874SecurityRealmReplacementTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        groupUtil = new GroupMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        repoUtil = new RepositoryMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
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
        
        NexusStateUtil.getNexusStatus();
        
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );
        
        NexusStateUtil.getNexusStatus();
        
        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );
        
        NexusStateUtil.getNexusStatus();
    }
    
    @Test
    public void negativeAuthentication()
        throws Exception
    {           
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );
        
        try
        {
            NexusStateUtil.getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            //good
        }
        
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );
        
        try
        {
            NexusStateUtil.getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            //good
        }
        
        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );
        
        try
        {
            NexusStateUtil.getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            //good
        }
    }
    
    @Test
    public void authorization()
        throws Exception
    {        
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        NexusStateUtil.getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();
        TargetMessageUtil.getList();
        TaskScheduleUtil.getTasks();
        
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );
        
        NexusStateUtil.getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();
        
        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );
        
        NexusStateUtil.getNexusStatus();
        groupUtil.getList();
        repoUtil.getList();
    }
    
    @Test
    public void negativeAuthorization()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );
        
        try
        {
            TargetMessageUtil.getList();
            Assert.fail();
        }
        catch ( Exception e )
        {
            // OK
        }
        
        try
        {
            TaskScheduleUtil.getTasks();
            Assert.fail();
        }
        catch ( Exception e )
        {
            // OK
        }
        
        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );
        
        try
        {
            TargetMessageUtil.getList();
            Assert.fail();
        }
        catch ( Exception e )
        {
            // OK
        }
        
        try
        {
            TaskScheduleUtil.getTasks();
            Assert.fail();
        }
        catch ( Exception e )
        {
            // OK
        }
        
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        try
        {
            userUtil.getList();
            Assert.fail();
        }
        catch ( Exception e )
        {
            // OK
        }
        
        try
        {
            roleUtil.getList();
            Assert.fail();
        }
        catch ( Exception e )
        {
            // OK
        }
    }
}
