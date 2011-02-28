/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1954;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractDeleteArtifactsIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }

    protected static final String REPO_TEST_HARNESS_PROXY = "nexus-test-harness-proxy";

    private File artifact;

    private Gav artifact1v2;

    private Gav artifact1v1;

    private Gav artifact2v1;

    public AbstractDeleteArtifactsIT()
    {
        super();
    }

    public AbstractDeleteArtifactsIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    //@BeforeClass
    @BeforeMethod
    public void init()
    {
        artifact = getTestFile( "artifact.jar" );
        artifact1v1 = GavUtil.newGav( "nexus1954", "artifact1", "1.0" );
        artifact1v2 = GavUtil.newGav( "nexus1954", "artifact1", "2.0" );
        artifact2v1 = GavUtil.newGav( "nexus1954", "artifact2", "1.0" );
    }

    @Test
    public void indexTest()
        throws Exception
    {
        updateIndexes();
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );

        getDeployUtils().deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, artifact1v2, artifact );
        getDeployUtils().deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, GavUtil.newGav( "nexus1954", "artifact2", "1.0" ),
                                            artifact );

        updateIndexes();
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v2, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact2v1, REPO_TEST_HARNESS_REPO ).size() );

        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v2, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact2v1, REPO_TEST_HARNESS_PROXY ).size() );

        deleteArtifact( artifact1v2 );

        updateIndexes();
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact1v2, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact2v1, REPO_TEST_HARNESS_REPO ).size() );

        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact1v2, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 1, getSearchMessageUtil().searchForGav( artifact2v1, REPO_TEST_HARNESS_PROXY ).size() );

        deleteArtifact( artifact1v1 );
        deleteArtifact( artifact2v1 );

        updateIndexes();
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact1v2, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact2v1, REPO_TEST_HARNESS_REPO ).size() );

        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact1v2, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 0, getSearchMessageUtil().searchForGav( artifact2v1, REPO_TEST_HARNESS_PROXY ).size() );
    }

    private void deleteArtifact( Gav gav )
        throws FileNotFoundException, IOException
    {
        String dirPath = gav.getGroupId().replace( '.', '/' ) + "/" + gav.getArtifactId() + "/" + gav.getVersion();
        Assert.assertTrue( deleteFromRepository( REPO_TEST_HARNESS_REPO, dirPath ) );
    }

    private static final SimpleDateFormat df;

    static
    {
        df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
        df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    protected void updateIndexes()
        throws Exception
    {

        long hostedLastMod = -1;

        File hostedIndexProps =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO
                + "/.index/nexus-maven-repository-index.properties" );
        if ( hostedIndexProps.exists() )
        {
            hostedLastMod = readLastMod( hostedIndexProps );
        }

        long proxyLastMod = -1;
        File proxyIndexProps =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO
                + "/.index/nexus-maven-repository-index.properties" );
        if ( proxyIndexProps.exists() )
        {
            proxyLastMod = readLastMod( proxyIndexProps );
        }

        TaskScheduleUtil.waitForAllTasksToStop();

        runUpdateIndex();

        long hostedLastMod2 = readLastMod( hostedIndexProps );
        Assert.assertTrue( hostedLastMod < hostedLastMod2 );

        long proxyLastMod2 = readLastMod( proxyIndexProps );
        Assert.assertTrue( proxyLastMod < proxyLastMod2 );
    }

    protected abstract void runUpdateIndex()
        throws Exception;

    private long readLastMod( File indexProps )
        throws Exception
    {
        Properties p = new Properties();
        InputStream input = new FileInputStream( indexProps );
        p.load( input );
        input.close();

        return df.parse( p.getProperty( IndexingContext.INDEX_TIMESTAMP ) ).getTime();
    }

}