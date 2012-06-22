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
package org.sonatype.nexus.plugin.deploy;

import java.util.ArrayList;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeployLifecycleParticipantTest
{
    private StringBufferLogger fakeLogger;

    private DeployLifecycleParticipant defaultLifecycleParticipant;

    @Before
    public void before()
    {
        fakeLogger = new StringBufferLogger( "fake" );
        this.defaultLifecycleParticipant = new DeployLifecycleParticipant();
        this.defaultLifecycleParticipant.enableLogging( fakeLogger );
    }

    // ===

    protected Plugin createPlugin( String groupId, String artifactId )
    {
        final Plugin plugin = new Plugin();
        plugin.setGroupId( groupId );
        plugin.setArtifactId( artifactId );
        Xpp3Dom configuration = new Xpp3Dom( "configuration" );
        plugin.setConfiguration( configuration );
        return plugin;
    }

    protected Plugin createPlugin( String groupId, String artifactId, String goal )
    {
        final Plugin plugin = createPlugin( groupId, artifactId );
        if ( goal != null && goal.trim().length() > 0 )
        {
            final PluginExecution execution = new PluginExecution();
            execution.setId( "default-" + goal );
            execution.setPhase( "deploy" );
            execution.getGoals().add( goal );
            plugin.getExecutions().add( execution );
        }
        return plugin;
    }

    protected Plugin createPluginAndSetConfig( String groupId, String artifactId, String goal, String configValue )
    {
        final Plugin plugin = createPlugin( groupId, artifactId, goal );
        plugin.setGroupId( groupId );
        plugin.setArtifactId( artifactId );
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        Xpp3Dom foo = new Xpp3Dom( "aSwitch" );
        foo.setValue( configValue );
        configuration.addChild( foo );
        return plugin;
    }

    protected MavenSession createSessionWithProjectsForModels( Model... models )
    {
        final MavenSession mockSession = Mockito.mock( MavenSession.class );
        final ArrayList<MavenProject> projects = new ArrayList<MavenProject>();
        for ( Model model : models )
        {
            final MavenProject mockProject = Mockito.mock( MavenProject.class );
            Mockito.when( mockProject.getModel() ).thenReturn( model );
            projects.add( mockProject );
        }
        Mockito.when( mockSession.getProjects() ).thenReturn( projects );
        return mockSession;
    }

    protected Model createModel( String groupId, String artifactId )
    {
        final Model model = new Model();
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( "1.0" );
        model.setBuild( new Build() );
        return model;
    }

    // ==

    @Test
    public void testSimpleOneModule()
        throws MavenExecutionException
    {
        final Model model = createModel( "org.foo", "simple" );
        // deploy defined as "usual"
        model.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // out plugin just declared as extension, nothing more. no goals
        model.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.THIS_GROUP_ID, DeployLifecycleParticipant.THIS_ARTIFACT_ID ) );

        final MavenSession mockSession = createSessionWithProjectsForModels( model );

        defaultLifecycleParticipant.afterProjectsRead( mockSession );

        // aftermath: deploy plugin should executions removed, nexus-maven-plugin should have executions added
        for ( Plugin plugin : mockSession.getProjects().get( 0 ).getModel().getBuild().getPlugins() )
        {
            if ( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID.equals( plugin.getArtifactId() ) )
            {
                Assert.assertTrue( "No executions for plugin "
                    + DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, plugin.getExecutions().isEmpty() );
            }
            else if ( DeployLifecycleParticipant.THIS_ARTIFACT_ID.equals( plugin.getArtifactId() ) )
            {
                Assert.assertEquals( "One execution for plugin " + DeployLifecycleParticipant.THIS_ARTIFACT_ID, 1,
                    plugin.getExecutions().size() );
            }
            else
            {
                // wtf? we did not add any other plugin
                Assert.fail( "Unknown plugin (): " + plugin.getGroupId() + ":" + plugin.getArtifactId() );
            }
        }
        // logging should happen
        Assert.assertTrue( "Missing log?", fakeLogger.getLoggedStuff().contains( "Installing Nexus Staging" ) );
        Assert.assertTrue( "Wrong count reported?", fakeLogger.getLoggedStuff().contains( "... total of 1" ) );
    }

    @Test
    public void testSimpleOneModuleWithManuallySetPlugin()
        throws MavenExecutionException
    {
        final Model model = createModel( "org.foo", "simple-but-manually-set" );
        // deploy defined as "usual"
        model.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // out plugin declared as extension with manually set execution
        model.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.THIS_GROUP_ID, DeployLifecycleParticipant.THIS_ARTIFACT_ID,
                "deploy" ) );

        final MavenSession mockSession = createSessionWithProjectsForModels( model );

        defaultLifecycleParticipant.afterProjectsRead( mockSession );

        // aftermath: lifecycle participant should stay put, do not intervene at all!
        for ( Plugin plugin : mockSession.getProjects().get( 0 ).getModel().getBuild().getPlugins() )
        {
            if ( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID.equals( plugin.getArtifactId() ) )
            {
                Assert.assertEquals( "One execution for plugin "
                    + DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, 1, plugin.getExecutions().size() );
            }
            else if ( DeployLifecycleParticipant.THIS_ARTIFACT_ID.equals( plugin.getArtifactId() ) )
            {
                Assert.assertEquals( "One execution for plugin " + DeployLifecycleParticipant.THIS_ARTIFACT_ID, 1,
                    plugin.getExecutions().size() );
            }
            else
            {
                // wtf? we did not add any other plugin
                Assert.fail( "Unknown plugin (): " + plugin.getGroupId() + ":" + plugin.getArtifactId() );
            }
        }
        // logging should happen
        Assert.assertTrue( "Missing log?", fakeLogger.getLoggedStuff().contains( "Not installing Nexus Staging" ) );
    }

    @Test
    public void testComplexReactorWithAggregators()
        throws MavenExecutionException
    {
        // layout
        // aggregator-pom:
        // parent-A : has one config for nexus-maven-plugin (declared as extension + config)
        // module-A1
        // parent-B : had other config for nexus-maven-plugin (declared as extension + config)
        // module-B1

        // remember: we "simulate" running in maven, where models are already interpolated and executions added!
        // hence, we do not fiddle with "modules" but just creating a list, as "maven would" load and sort em

        // aggregator-pom, essentially empty
        final Model aggregatorPom = createModel( "org.foo", "aggregator" );

        // parent-A:
        final Model parentA = createModel( "org.foo", "parent-a" );
        // deploy defined as "usual"
        parentA.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // our plugin declared as extension, no execution and A specific param
        parentA.getBuild().getPlugins().add(
            createPluginAndSetConfig( DeployLifecycleParticipant.THIS_GROUP_ID,
                DeployLifecycleParticipant.THIS_ARTIFACT_ID, null, "profile-a" ) );

        // module-A:
        final Model moduleA = createModel( "org.foo", "module-a" );
        // deploy defined as "usual"
        moduleA.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // our plugin declared as extension, no execution and A specific param
        moduleA.getBuild().getPlugins().add(
            createPluginAndSetConfig( DeployLifecycleParticipant.THIS_GROUP_ID,
                DeployLifecycleParticipant.THIS_ARTIFACT_ID, null, "profile-a" ) );

        // parent-B:
        final Model parentB = createModel( "org.foo", "parent-b" );
        // deploy defined as "usual"
        parentB.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // our plugin declared as extension, no execution and A specific param
        parentB.getBuild().getPlugins().add(
            createPluginAndSetConfig( DeployLifecycleParticipant.THIS_GROUP_ID,
                DeployLifecycleParticipant.THIS_ARTIFACT_ID, null, "profile-b" ) );

        // module-B:
        final Model moduleB = createModel( "org.foo", "module-b" );
        // deploy defined as "usual"
        moduleB.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // our plugin declared as extension, no execution and A specific param
        moduleB.getBuild().getPlugins().add(
            createPluginAndSetConfig( DeployLifecycleParticipant.THIS_GROUP_ID,
                DeployLifecycleParticipant.THIS_ARTIFACT_ID, null, "profile-b" ) );

        final MavenSession mockSession =
            createSessionWithProjectsForModels( aggregatorPom, parentA, moduleA, parentB, moduleB );

        defaultLifecycleParticipant.afterProjectsRead( mockSession );

        // aftermath: everyting should happen as "usual", but check the proper configurations!
        for ( Plugin plugin : mockSession.getProjects().get( 0 ).getModel().getBuild().getPlugins() )
        {
            if ( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID.equals( plugin.getArtifactId() ) )
            {
                Assert.assertTrue( "No executions for plugin "
                    + DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, plugin.getExecutions().isEmpty() );
            }
            else if ( DeployLifecycleParticipant.THIS_ARTIFACT_ID.equals( plugin.getArtifactId() ) )
            {
                Assert.assertEquals( "One execution for plugin " + DeployLifecycleParticipant.THIS_ARTIFACT_ID, 1,
                    plugin.getExecutions().size() );
                if ( plugin.getArtifactId().endsWith( "-a" ) )
                {
                    Assert.assertTrue( "Config mismatch!",
                        ( (Xpp3Dom) plugin.getConfiguration() ).getChild( "aSwitch" ).getValue().endsWith( "-a" ) );
                }
                else if ( plugin.getArtifactId().endsWith( "-b" ) )
                {
                    Assert.assertTrue( "Config mismatch!",
                        ( (Xpp3Dom) plugin.getConfiguration() ).getChild( "aSwitch" ).getValue().endsWith( "-b" ) );
                }
                else
                {
                    // wtf? we did not add any other plugin
                    Assert.fail( "Unknown plugin config: " + plugin.getGroupId() + ":" + plugin.getArtifactId() );
                }
            }
            else
            {
                // wtf? we did not add any other plugin
                Assert.fail( "Unknown plugin: " + plugin.getGroupId() + ":" + plugin.getArtifactId() );
            }
        }
        // logging should happen
        Assert.assertTrue( "Missing log?", fakeLogger.getLoggedStuff().contains( "Installing Nexus Staging" ) );
        Assert.assertTrue( "Wrong count reported?", fakeLogger.getLoggedStuff().contains( "... total of 4" ) );
    }

    @Test
    public void testABadProject()
        throws MavenExecutionException
    {
        final Model model = createModel( "org.foo", "bad" );
        model.getBuild().getPlugins().add(
            createPlugin( DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_GROUP_ID,
                DeployLifecycleParticipant.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy" ) );
        // our plugin declared MULTIPLE TIMES
        model.getBuild().getPlugins().add(
            createPluginAndSetConfig( DeployLifecycleParticipant.THIS_GROUP_ID,
                DeployLifecycleParticipant.THIS_ARTIFACT_ID, null, "profile-a" ) );
        model.getBuild().getPlugins().add(
            createPluginAndSetConfig( DeployLifecycleParticipant.THIS_GROUP_ID,
                DeployLifecycleParticipant.THIS_ARTIFACT_ID, null, "profile-a" ) );

        final MavenSession mockSession = createSessionWithProjectsForModels( model );

        try
        {
            defaultLifecycleParticipant.afterProjectsRead( mockSession );
            Assert.fail( "Should choke on this!" );
        }
        catch ( MavenExecutionException e )
        {
            // good
            Assert.assertEquals( "The build contains multiple versions of plugin "
                + DeployLifecycleParticipant.THIS_GROUP_ID + ":" + DeployLifecycleParticipant.THIS_ARTIFACT_ID,
                e.getMessage() );
        }
    }
}
