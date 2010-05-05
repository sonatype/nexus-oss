/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package com.sonatype.nexus.unpack.it.nxcm1312;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

import com.sonatype.nexus.unpack.it.AbstractUnpackIT;

public class NXCM1312UploadCompressedBundleIT
    extends AbstractUnpackIT
{

    @Test
    public void upload()
        throws Exception
    {
        getDeployUtils().deployWithWagon( "http", nexusBaseUrl + "service/local/repositories/"
            + REPO_TEST_HARNESS_REPO + "/content-compressed", getTestFile( "bundle.zip" ), "" );

        Assert.assertEquals( 1, SearchMessageUtil.searchFor( "nxcm1312", "artifact", "2.0" ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( "org.nxcm1312", "maven-deploy-released", "1.0" ).size() );
    }

    @Test
    public void uploadWithPath()
        throws Exception
    {
        getDeployUtils().deployWithWagon( "http", nexusBaseUrl + "service/local/repositories/"
            + REPO_TEST_HARNESS_REPO + "/content-compressed", getTestFile( "bundle.zip" ), "some/path" );

        // Check for the parent folder, it should been created
        File root = new File( nexusWorkDir, "storage/nexus-test-harness-repo/some/path" );
        Assert.assertTrue( root.isDirectory() );
    }

    @Test
    public void uploadWithDelete()
        throws Exception
    {
        // what we do here:
        // 1. upload without delete flag, it should succeed
        // 2. then validate that upload happened okay
        // 3. then we upload bundle1.zip (it does not contains nxcm1312 root dir) without delete flag
        // 4. then validate that upload happened okay, but root directory nxcm1312 is still in place
        // 5. then we upload bundle1.zip again, this time with delete flag
        // 6. then we validate that upload happened okay, and root directory nxcm1312 is deleted

        FileRepresentation bundleRepresentation =
            new FileRepresentation( getTestFile( "bundle.zip" ), MediaType.APPLICATION_ZIP );
        FileRepresentation bundle1Representation =
            new FileRepresentation( getTestFile( "bundle1.zip" ), MediaType.APPLICATION_ZIP );

        // 1. upload the bundle
        Assert.assertTrue( uploadBundle( false, bundleRepresentation ).getStatus().isSuccess() );

        // 2. validate all is there
        validateBundleUpload( true, "org", "nxcm1312" );

        // 3. upload bundle1.zip (that lacks nxcm1312 root dir but do not use delete flag)
        Assert.assertTrue( uploadBundle( false, bundle1Representation ).getStatus().isSuccess() );

        // 4. validate, all should be still there (did not delete)
        validateBundleUpload( true, "org", "nxcm1312" );

        // 5. upload bundle1.zip (that lacks nxcm1312 root dir but this time DO USE delete flag)
        Assert.assertTrue( uploadBundle( true, bundle1Representation ).getStatus().isSuccess() );

        // 4. validate, nxcm1312 should not be there anymore
        validateBundleUpload( true, "org" );
        validateBundleUpload( false, "nxcm1312" );
    }

    protected Response uploadBundle( boolean useDeleteFlag, Representation bundleRepresentation )
        throws Exception
    {
        String serviceUrl = "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/content-compressed";

        if ( useDeleteFlag )
        {
            serviceUrl = serviceUrl + "?delete";
        }

        Response response = RequestFacade.sendMessage( serviceUrl, Method.PUT, bundleRepresentation );

        return response;
    }

    protected void validateBundleUpload( boolean checkForPresence, String... presentRootDirectories )
        throws Exception
    {
        File repositoryRootDirectory = new File( nexusWorkDir, "storage/nexus-test-harness-repo" );

        for ( String presentRootDirectory : presentRootDirectories )
        {
            if ( checkForPresence )
            {
                Assert.assertTrue( "Directory should exists with name: " + presentRootDirectory,
                                   new File( repositoryRootDirectory, presentRootDirectory ).isDirectory() );
            }
            else
            {
                Assert.assertFalse( "File should not exists: " + presentRootDirectory,
                                    new File( repositoryRootDirectory, presentRootDirectory ).exists() );
            }
        }
    }

}
