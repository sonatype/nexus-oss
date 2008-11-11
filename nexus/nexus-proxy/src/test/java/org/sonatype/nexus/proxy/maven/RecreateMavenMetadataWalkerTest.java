/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */

package org.sonatype.nexus.proxy.maven;

import java.io.File;
import java.io.FileInputStream;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.StoreWalker;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalkerTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private Repository inhouse;

    private Repository inhouseSnapshot;

    private File repoBase = new File( "./target/test-classes/mavenMetadataTestRepo" );

    private Walker walker;

    private String[] releaseArtifactFiles = {
        "/junit/junit/3.8.1/junit-3.8.1.jar",
        "/junit/junit/3.8.1/junit-3.8.1.pom",
        "/junit/junit/3.8.2/junit-3.8.2.jar",
        "/junit/junit/3.8.2/junit-3.8.2.pom",
        "/junit/junit/4.0/junit-4.0.jar",
        "/junit/junit/4.0/junit-4.0.pom",
        "/junit/junit/4.4/junit-4.4.jar",
        "/junit/junit/4.4/junit-4.4.pom" };

    private String[] snapshotArtifactFiles = {
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.180215-1.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.180215-1.pom",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.182430-2.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.182430-2.pom",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.184527-3.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.184527-3.pom",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081025.143218-32.jar",
        "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081025.143218-32.pom" };

    private String[] pluginArtifactFiles = {
        "/org/apache/maven/plugins/maven-antrun-plugin/1.1/maven-antrun-plugin-1.1.jar",
        "/org/apache/maven/plugins/maven-antrun-plugin/1.1/maven-antrun-plugin-1.1.pom",
        "/org/apache/maven/plugins/maven-clean-plugin/2.2/maven-clean-plugin-2.2.jar",
        "/org/apache/maven/plugins/maven-clean-plugin/2.2/maven-clean-plugin-2.2.pom",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.1/maven-plugin-plugin-2.4.1.jar",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.1/maven-plugin-plugin-2.4.1.pom",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.3/maven-plugin-plugin-2.4.3.jar",
        "/org/apache/maven/plugins/maven-plugin-plugin/2.4.3/maven-plugin-plugin-2.4.3.pom",
        "/org/apache/maven/plugins/maven-source-plugin/2.0.4/maven-source-plugin-2.0.4.jar",
        "/org/apache/maven/plugins/maven-source-plugin/2.0.4/maven-source-plugin-2.0.4.pom" };

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );

        return jettyTestsuiteEnvironmentBuilder;
    }

    @Override
    public void setUp()
        throws Exception
    {

        super.setUp();

        inhouse = getRepositoryRegistry().getRepository( "inhouse" );

        // copy all release artifact fils hosted inhouse repo
        for ( String releaseArtifactFile : releaseArtifactFiles )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( releaseArtifactFile, true );

            FileInputStream fis = new FileInputStream( new File( repoBase, releaseArtifactFile ) );

            inhouse.storeItem( request, fis, null );

            fis.close();
        }

        for ( String pluginArtifactFile : pluginArtifactFiles )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( pluginArtifactFile, true );

            FileInputStream fis = new FileInputStream( new File( repoBase, pluginArtifactFile ) );

            inhouse.storeItem( request, fis, null );

            fis.close();
        }

        inhouseSnapshot = getRepositoryRegistry().getRepository( "inhouse-snapshot" );

        // copy all snapshot artifact fils hosted snapshot inhouse repo
        for ( String snapshotArtifactFile : snapshotArtifactFiles )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( snapshotArtifactFile, true );

            FileInputStream fis = new FileInputStream( new File( repoBase, snapshotArtifactFile ) );

            inhouseSnapshot.storeItem( request, fis, null );

            fis.close();
        }

        walker = (Walker) lookup( Walker.class );
    }

    public void testRecreateMavenMetadataWalkerWalkerRelease()
        throws Exception
    {
        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor();

        DefaultWalkerContext ctx = new DefaultWalkerContext( inhouse );

        ctx.getProcessors().add( wp );

        walker.walk( ctx );

        assertNotNull( inhouse.retrieveItem( new ResourceStoreRequest( "/junit/junit/maven-metadata.xml", false ) ) );

    }

    public void testRecreateMavenMetadataWalkerWalkerSnapshot()
        throws Exception
    {
        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor();

        DefaultWalkerContext ctx = new DefaultWalkerContext( inhouseSnapshot );

        ctx.getProcessors().add( wp );

        walker.walk( ctx );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-api/maven-metadata.xml",
            false ) ) );

        assertNotNull( inhouseSnapshot.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/maven-metadata.xml",
            false ) ) );
    }

    public void testRecreateMavenMetadataWalkerWalkerPlugin()
        throws Exception
    {
        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor();

        DefaultWalkerContext ctx = new DefaultWalkerContext( inhouse );

        ctx.getProcessors().add( wp );

        walker.walk( ctx );

        assertNotNull( inhouse.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/plugins/maven-metadata.xml",
            false ) ) );
    }
    
    public void testRebuildChecksumFiles()
        throws Exception
    {
        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor();

        DefaultWalkerContext ctx = new DefaultWalkerContext( inhouse );

        ctx.getProcessors().add( wp );

        walker.walk( ctx );

        assertNotNull( inhouse
            .retrieveItem( new ResourceStoreRequest( "/junit/junit/3.8.1/junit-3.8.1.jar.md5", false ) ) );

        assertNotNull( inhouse
            .retrieveItem( new ResourceStoreRequest( "/junit/junit/3.8.1/junit-3.8.1.jar.sha1", false ) ) );

        assertNotNull( inhouse.retrieveItem( new ResourceStoreRequest( "/junit/junit/4.0/junit-4.0.pom.md5", false ) ) );

        assertNotNull( inhouse.retrieveItem( new ResourceStoreRequest( "/junit/junit/maven-metadata.xml.md5", false ) ) );

        assertNotNull( inhouse.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/plugins/maven-metadata.xml.sha1",
            false ) ) );
    }

}
