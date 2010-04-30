package org.sonatype.nexus.integrationtests.nexus1961;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.index.treeview.TreeNode;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeNode;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class Nexus1961IndexContentIT
    extends AbstractNexusIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void getIndexContent()
        throws Exception
    {
        String serviceURI = "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/index_content/";

        Response response = RequestFacade.doGetRequest( serviceURI );
        String responseText = response.getEntity().getText();
        Status status = response.getStatus();
        Assert.assertTrue( responseText + status, status.isSuccess() );

        XStream xstream = XStreamFactory.getXmlXStream();
        
        xstream.processAnnotations( IndexBrowserTreeNode.class );
        xstream.processAnnotations( IndexBrowserTreeViewResponseDTO.class );

        XStreamRepresentation re =
            new XStreamRepresentation( xstream, responseText, MediaType.APPLICATION_XML );
        IndexBrowserTreeViewResponseDTO resourceResponse =
            (IndexBrowserTreeViewResponseDTO) re.getPayload( new IndexBrowserTreeViewResponseDTO() );

        IndexBrowserTreeNode content = resourceResponse.getData();
        
        List<TreeNode> children = content.getChildren();
        for ( TreeNode child : children )
        {
            Assert.assertEquals( "nexus1961", child.getNodeName() );
        }

    }
}
