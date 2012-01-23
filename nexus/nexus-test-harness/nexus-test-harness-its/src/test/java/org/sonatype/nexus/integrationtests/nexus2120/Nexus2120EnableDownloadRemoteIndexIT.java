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
package org.sonatype.nexus.integrationtests.nexus2120;

import java.io.File;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeNode;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.tasks.UpdateIndexTask;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

public class Nexus2120EnableDownloadRemoteIndexIT
    extends AbstractNexusIntegrationTest
{

    final String URI = "service/local/repositories/basic/index_content/";

    protected static final int webProxyPort;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy-server-port" );
    }

    protected ControlledServer server;

    private RepositoryMessageUtil repoUtil;

    @BeforeMethod
    public void start()
        throws Exception
    {
        server = lookup( ControlledServer.class );
        repoUtil = new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
    }

    @AfterMethod
    public void stop()
        throws Exception
    {
        server.stop();
        server = null;
        repoUtil = null;
    }

    @Test
    public void downloadChecksumTest()
        throws Exception
    {
        RepositoryResource basic = (RepositoryResource) repoUtil.getRepository( "basic" );
        // ensure URL
        Assert.assertEquals( "http://localhost:" + webProxyPort + "/repository/",
            basic.getRemoteStorage().getRemoteStorageUrl() );
        // ensure is not downloading index
        Assert.assertFalse( basic.isDownloadRemoteIndexes() );

        File basicRemoteRepo = getTestFile( "basic" );
        List<String> repoUrls = server.addServer( "repository", basicRemoteRepo );

        server.start();

        // reindex once
        RepositoryMessageUtil.updateIndexes( "basic" );
        TaskScheduleUtil.waitForAllTasksToStop( UpdateIndexTask.class );

        // first try, download remote index set to false
        Assert.assertTrue( repoUrls.isEmpty(), "nexus should not download remote indexes!!! " + repoUrls );

        // server changed here, a 404 is no longer returned if index_context is empty, 404 will only be returned
        // if index_context does not exist (or repo does not exist)
        String content = RequestFacade.doGetForText( URI );

        XStream xstream = XStreamFactory.getXmlXStream();

        xstream.processAnnotations( IndexBrowserTreeNode.class );
        xstream.processAnnotations( IndexBrowserTreeViewResponseDTO.class );

        XStreamRepresentation re = new XStreamRepresentation( xstream, content, MediaType.APPLICATION_XML );
        IndexBrowserTreeViewResponseDTO resourceResponse =
            (IndexBrowserTreeViewResponseDTO) re.getPayload( new IndexBrowserTreeViewResponseDTO() );

        Assert.assertTrue( resourceResponse.getData().getChildren().isEmpty(), "index response should have 0 entries" );

        // I changed my mind, I do wanna remote index
        basic.setDownloadRemoteIndexes( true );
        repoUtil.updateRepo( basic );

        // reindex again
        RepositoryMessageUtil.updateIndexes( "basic" );
        TaskScheduleUtil.waitForAllTasksToStop( UpdateIndexTask.class );

        // did nexus downloaded indexes?
        Assert.assertTrue( repoUrls.contains( "/repository/.index/nexus-maven-repository-index.gz" ),
            "nexus should download remote indexes!!! " + repoUrls );

        RequestFacade.doGet( URI );
    }
}
