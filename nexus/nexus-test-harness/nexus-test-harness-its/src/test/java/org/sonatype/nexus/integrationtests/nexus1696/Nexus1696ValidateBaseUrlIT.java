/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1696;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RestApiSettings;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.ContentListMessageUtil;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.UserResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus1696ValidateBaseUrlIT
    extends AbstractNexusIntegrationTest
{

    private String baseUrl;

    @BeforeMethod
    public void init()
        throws Exception
    {
        baseUrl = nexusBaseUrl.replace( "nexus", "nexus1696" ).replace( "http", "https" );

        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        RestApiSettings restApiSettings = new RestApiSettings();
        restApiSettings.setForceBaseUrl( true );
        restApiSettings.setBaseUrl( baseUrl );
        settings.setGlobalRestApiSettings( restApiSettings );

        SettingsMessageUtil.save( settings );
    }

    @Test
    public void checkGroups()
        throws Exception
    {
        GroupMessageUtil groupUtil =
            new GroupMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
        ContentListMessageUtil contentUtil =
            new ContentListMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );

        List<RepositoryGroupListResource> groups = groupUtil.getList();
        Assert.assertFalse( groups.isEmpty(), "No itens to be tested" );

        for ( RepositoryGroupListResource group : groups )
        {
            Assert.assertTrue( group.getResourceURI().startsWith( baseUrl ), "Repository '" + group.getId()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + group.getResourceURI() );

            List<ContentListResource> contents = contentUtil.getContentListResource( group.getId(), "/", true );

            for ( ContentListResource content : contents )
            {
                Assert.assertTrue( content.getResourceURI().startsWith( baseUrl ),
                    "Group content '" + content.getText() + "' uri do not start with baseUrl.  Expected: " + baseUrl
                        + ", but got: " + content.getResourceURI() );
            }
        }
    }

    @Test
    public void checkRepositories()
        throws Exception
    {
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
        ContentListMessageUtil contentUtil =
            new ContentListMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );

        List<RepositoryListResource> repositories = repoUtil.getList();
        Assert.assertFalse( repositories.isEmpty(), "No itens to be tested" );

        for ( RepositoryListResource repo : repositories )
        {
            Assert.assertTrue( repo.getResourceURI().startsWith( baseUrl ), "Repository '" + repo.getId()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + repo.getResourceURI() );

            List<ContentListResource> contents = contentUtil.getContentListResource( repo.getId(), "/", false );

            for ( ContentListResource content : contents )
            {
                Assert.assertTrue( content.getResourceURI().startsWith( baseUrl ),
                    "Repository content '" + content.getText() + "' uri do not start with baseUrl.  Expected: "
                        + baseUrl + ", but got: " + content.getResourceURI() );
            }
        }
    }

    @Test
    public void checkPrivs()
        throws Exception
    {
        List<PrivilegeStatusResource> privs =
            new PrivilegesMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML ).getList();
        Assert.assertFalse( privs.isEmpty(), "No itens to be tested" );

        for ( PrivilegeStatusResource priv : privs )
        {
            Assert.assertTrue( priv.getResourceURI().startsWith( baseUrl ), "Privilege '" + priv.getId()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + priv.getResourceURI() );
        }
    }

    @Test
    public void checkRoles()
        throws Exception
    {
        List<RoleResource> roles = new RoleMessageUtil( this, null, null ).getList();
        Assert.assertFalse( roles.isEmpty(), "No itens to be tested" );

        for ( RoleResource role : roles )
        {
            Assert.assertTrue( role.getResourceURI().startsWith( baseUrl ), "Role '" + role.getId()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + role.getResourceURI() );
        }
    }

    @Test
    public void checkUsers()
        throws Exception
    {
        List<UserResource> users = new UserMessageUtil( this, null, null ).getList();
        Assert.assertFalse( users.isEmpty(), "No itens to be tested" );

        for ( UserResource user : users )
        {
            Assert.assertTrue( user.getResourceURI().startsWith( baseUrl ), "User '" + user.getUserId()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + user.getResourceURI() );
        }
    }

    @Test
    public void checkRouting()
        throws Exception
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "public" );
        resource.setPattern( ".*/org/.*" );
        resource.setRuleType( RepositoryRouteResource.INCLUSION_RULE_TYPE );
        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        RoutesMessageUtil routesUtil = new RoutesMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
        Status status = routesUtil.sendMessage( Method.POST, resource ).getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to create a route " + status );

        List<RepositoryRouteListResource> routes = RoutesMessageUtil.getList();
        Assert.assertFalse( routes.isEmpty(), "No itens to be tested" );

        for ( RepositoryRouteListResource route : routes )
        {
            Assert.assertTrue( route.getResourceURI().startsWith( baseUrl ), "Route '" + route.getGroupId()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + route.getResourceURI() );
        }
    }

    @Test
    public void checkTasks()
        throws Exception
    {
        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( "taskManual" );
        scheduledTask.setSchedule( "manual" );
        scheduledTask.setTypeId( UpdateIndexTaskDescriptor.ID );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( "all_repo" );
        scheduledTask.addProperty( prop );

        Status status = TaskScheduleUtil.create( scheduledTask );
        Assert.assertTrue( status.isSuccess(), "Unable to create a task " + status );

        List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();
        Assert.assertFalse( tasks.isEmpty(), "No itens to be tested" );

        for ( ScheduledServiceListResource task : tasks )
        {
            Assert.assertTrue( task.getResourceURI().startsWith( baseUrl ), "Task '" + task.getName()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + task.getResourceURI() );
        }
    }

    @Test
    public void checkRepositoryTargets()
        throws Exception
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "createTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        TargetMessageUtil targetUtil = new TargetMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        targetUtil.createTarget( resource );

        List<RepositoryTargetListResource> targets = TargetMessageUtil.getList();
        Assert.assertFalse( targets.isEmpty(), "No itens to be tested" );

        for ( RepositoryTargetListResource target : targets )
        {
            Assert.assertTrue( target.getResourceURI().startsWith( baseUrl ), "Target '" + target.getName()
                + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + target.getResourceURI() );
        }
    }

    @AfterMethod
    public void resetBaseUrl()
        throws Exception
    {
        baseUrl = nexusBaseUrl;

        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        RestApiSettings restApiSettings = new RestApiSettings();
        restApiSettings.setForceBaseUrl( true );
        restApiSettings.setBaseUrl( baseUrl );
        settings.setGlobalRestApiSettings( restApiSettings );

        SettingsMessageUtil.save( settings );
    }

}
