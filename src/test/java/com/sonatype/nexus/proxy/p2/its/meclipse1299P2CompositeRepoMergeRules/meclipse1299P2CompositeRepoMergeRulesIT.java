/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its.meclipse1299P2CompositeRepoMergeRules;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import com.sonatype.nexus.proxy.p2.its.AbstractNexusProxyP2IntegrationIT;

public class meclipse1299P2CompositeRepoMergeRulesIT
    extends AbstractNexusProxyP2IntegrationIT
{
    private static final String TEST_REPO_ID = "p2proxymeclipse1299P2CompositeRepoMergeRules";

    public meclipse1299P2CompositeRepoMergeRulesIT()
    {
        super( TEST_REPO_ID );
    }

    @Test
    public void p2ProxyCompositeContentAndArtifacts()
        throws Exception
    {
        File artifactsXmlFile = new File("target/downloads/meclipse1299P2CompositeRepoMergeRules/artifacts.xml");
        Assert.assertFalse( artifactsXmlFile.exists() );

        downloadFile( new URL( getRepositoryUrl( TEST_REPO_ID ) + "/artifacts.xml" ), artifactsXmlFile.getAbsolutePath() );
        Assert.assertTrue( artifactsXmlFile.exists() );

        String artifactsXmlContent = FileUtils.fileRead( artifactsXmlFile );
        Assert.assertTrue( artifactsXmlContent.contains( "<mappings size=\"5\">" ) );
        Assert.assertTrue( artifactsXmlContent.contains( "<rule output=\"${repoUrl}/plugins/${id}_${version}.jar\" filter=\"(&amp; (classifier=osgi.bundle))\" />" ) );
        Assert.assertTrue( artifactsXmlContent.contains( "<rule output=\"${repoUrl}/binary/${id}_${version}\" filter=\"(&amp; (classifier=binary))\" />" ) );
        Assert.assertTrue( artifactsXmlContent.contains( "<rule output=\"${repoUrl}/features/${id}_${version}.jar\" filter=\"(&amp; (classifier=org.eclipse.update.feature))\" />" ) );
        Assert.assertTrue( artifactsXmlContent.contains( "<rule output=\"foo.bar\" filter=\"(&amp; (classifier=foo))\" />" ) );
        Assert.assertTrue( artifactsXmlContent.contains( "<rule output=\"bar.foo\" filter=\"(&amp; (classifier=bar))\" />" ) );
    }
}
