package org.sonatype.nexus.integrationtests.nexus133;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus133TargetCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    protected TargetMessageUtil messageUtil;

    public Nexus133TargetCrudJsonTests()
    {
        this.messageUtil =
            new TargetMessageUtil(
                                   XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                   MediaType.APPLICATION_JSON, this.getBaseNexusUrl() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "createTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.messageUtil.verifyTargetsConfig( responseResource );
    }

    public void readTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "readTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // make sure it was added
        this.messageUtil.verifyTargetsConfig( responseResource );

        // update the Id
        resource.setId( responseResource.getId() );

        response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

    }

    @Test
    public void udpateTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "udpateTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // make sure it was added
        this.messageUtil.verifyTargetsConfig( responseResource );

        // update the Id
        resource.setId( responseResource.getId() );

        resource.setName( "udpateTestRenamed" );
        resource.setContentClass( "maven2" );
        patterns.clear();
        patterns.add( ".*new.*" );
        patterns.add( ".*patterns.*" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure it was updated
        this.messageUtil.verifyTargetsConfig( responseResource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "deleteTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // make sure it was added so we know if it was removed
        this.messageUtil.verifyTargetsConfig( responseResource );

        // use the new IDs
        // response = this.deleteTarget( responseResource.getId() );

        response = this.messageUtil.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Repository Target: " + response.getStatus() );
        }

        this.messageUtil.verifyTargetsConfig( new ArrayList<RepositoryTargetResource>() );
    }

}
