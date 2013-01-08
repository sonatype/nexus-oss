/*
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
package org.sonatype.nexus.unpack.it.nxcm1312;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.isSuccess;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;

import org.sonatype.nexus.unpack.it.AbstractUnpackIT;

public class NXCM1312UploadCompressedBundleIT
    extends AbstractUnpackIT
{
    // HACK: Deal with lack of test dependencies, by running them all together under one-test method

    @Test
    public void testCombined()
        throws Exception
    {
        do_upload();
        do_uploadWithPath();
        do_uploadWithDelete();
    }

    //@Test
    public void do_upload()
        throws Exception
    {
        // this one test DOES depend on indexing, so we must enable it
        setIndexingEnabled( true );

        getDeployUtils().deployWithWagon(
            "http",
            nexusBaseUrl + "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/content-compressed",
            getTestFile( "bundle.zip" ),
            "" );

        getEventInspectorsUtil().waitForCalmPeriod();

        // force index before searching, else we are dependent on very specific timing for this to pass... and even asis it might still fail due to timing

        getSearchMessageUtil().reindexGAV(REPO_TEST_HARNESS_REPO, new Gav("nxcm1312", "artifact", "2.0"));
        Assert.assertEquals(getSearchMessageUtil().searchForGav("nxcm1312", "artifact", "2.0").size(), 1);

        getSearchMessageUtil().reindexGAV(REPO_TEST_HARNESS_REPO, new Gav("org.nxcm1312", "maven-deploy-released", "1.0"));
        Assert.assertEquals(getSearchMessageUtil().searchForGav("org.nxcm1312", "maven-deploy-released", "1.0").size(), 1);
    }

    //@Test( dependsOnMethods = "upload" )
    public void do_uploadWithPath()
        throws Exception
    {
        getDeployUtils().deployWithWagon(
            "http",
            nexusBaseUrl + "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/content-compressed",
            getTestFile( "bundle.zip" ),
            "some/path" );

        // Check for the parent folder, it should been created
        File root = new File( nexusWorkDir, "storage/nexus-test-harness-repo/some/path" );
        assertThat( root, FileMatchers.isDirectory() );
    }

    //@Test( dependsOnMethods = "uploadWithPath" )
    public void do_uploadWithDelete()
        throws Exception
    {
        // what we do here:
        // 1. upload without delete flag, it should succeed
        // 2. then validate that upload happened okay
        // 3. then we upload bundle1.zip (it does not contains nxcm1312 root dir) without delete flag
        // 4. then validate that upload happened okay, but root directory nxcm1312 is still in place
        // 5. then we upload bundle1.zip again, this time with delete flag
        // 6. then we validate that upload happened okay, and root directory nxcm1312 is deleted

        FileRepresentation bundleRepresentation = new FileRepresentation(
            getTestFile( "bundle.zip" ),
            MediaType.APPLICATION_ZIP );
        FileRepresentation bundle1Representation = new FileRepresentation(
            getTestFile( "bundle1.zip" ),
            MediaType.APPLICATION_ZIP );

        // 1. upload the bundle
        assertThat( uploadBundle( false, bundleRepresentation ), isSuccess() );

        // 2. validate all is there
        validateBundleUpload( true, "org", "nxcm1312" );

        // 3. upload bundle1.zip (that lacks nxcm1312 root dir but do not use delete flag)
        assertThat( uploadBundle( false, bundle1Representation ), isSuccess() );

        // 4. validate, all should be still there (did not delete)
        validateBundleUpload( true, "org", "nxcm1312" );

        // 5. upload bundle1.zip (that lacks nxcm1312 root dir but this time DO USE delete flag)
        assertThat( uploadBundle( true, bundle1Representation ), isSuccess() );

        // 4. validate, nxcm1312 should not be there anymore
        validateBundleUpload( true, "org" );
        validateBundleUpload( false, "nxcm1312" );
    }

    protected Status uploadBundle( boolean useDeleteFlag, Representation bundleRepresentation )
        throws Exception
    {
        String serviceUrl = "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/content-compressed";

        if ( useDeleteFlag )
        {
            serviceUrl = serviceUrl + "?delete";
        }

        Response response = null;
        try
        {
            response = RequestFacade.sendMessage( serviceUrl, Method.PUT, bundleRepresentation );

            return response.getStatus();
        }
        finally
        {
            RequestFacade.releaseResponse( response );

            getEventInspectorsUtil().waitForCalmPeriod();
            TaskScheduleUtil.waitForAllTasksToStop();
        }
    }

    protected void validateBundleUpload( boolean checkForPresence, String... presentRootDirectories )
        throws Exception
    {
        File repositoryRootDirectory = new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO );

        for ( String presentRootDirectory : presentRootDirectories )
        {
            if ( checkForPresence )
            {
                assertThat( new File( repositoryRootDirectory, presentRootDirectory ), FileMatchers.isDirectory() );
            }
            else
            {
                assertThat( new File( repositoryRootDirectory, presentRootDirectory ), not( FileMatchers.exists() ) );
            }
        }
    }

}
