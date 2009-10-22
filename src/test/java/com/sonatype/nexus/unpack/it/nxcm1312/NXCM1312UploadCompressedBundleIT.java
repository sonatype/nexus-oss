package com.sonatype.nexus.unpack.it.nxcm1312;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

import com.sonatype.nexus.unpack.it.AbstractUnpackIT;

public class NXCM1312UploadCompressedBundleIT
    extends AbstractUnpackIT
{

    @Test
    public void upload()
        throws Exception
    {
        DeployUtils.deployWithWagon( container, "http", nexusBaseUrl + "service/local/repositories/"
            + REPO_TEST_HARNESS_REPO + "/content-compressed", getTestFile( "bundle.zip" ), "" );

        Assert.assertEquals( 1, SearchMessageUtil.searchFor( "nxcm1312", "artifact", "2.0" ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( "org.nxcm1312", "maven-deploy-released", "1.0" ).size() );
    }

    @Test
    public void uploadWithPath()
        throws Exception
    {
        DeployUtils.deployWithWagon( container, "http", nexusBaseUrl + "service/local/repositories/"
            + REPO_TEST_HARNESS_REPO + "/content-compressed", getTestFile( "bundle.zip" ), "some/path" );
    }

}
