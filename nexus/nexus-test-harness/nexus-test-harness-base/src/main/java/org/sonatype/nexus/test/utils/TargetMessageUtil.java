/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class TargetMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( TargetMessageUtil.class );

    public TargetMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RepositoryTargetResource createTarget( RepositoryTargetResource target )
        throws IOException
    {
        Response response = this.sendMessage( Method.POST, target );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Could not create Repository Target: " + response.getStatus() + "\nResponse Text:\n"
            + responseText + "\n" + xstream.toXML( target ), response.getStatus().isSuccess() );

        // get the Resource object
        RepositoryTargetResource responseResource = this.getResourceFromResponse( responseText );

        // validate
        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( target.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( target.getName(), responseResource.getName() );
        Assert.assertEquals( target.getPatterns(), responseResource.getPatterns() );

        this.verifyTargetsConfig( responseResource );

        return responseResource;
    }

    public Response sendMessage( Method method, RepositoryTargetResource resource )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String repoTargetId = ( resource.getId() == null ) ? "?undefined" : "/" + resource.getId();

        String serviceURI = "service/local/repo_targets" + repoTargetId;

        RepositoryTargetResourceResponse requestResponse = new RepositoryTargetResourceResponse();
        requestResponse.setData( resource );
        // now set the payload
        representation.setPayload( requestResponse );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    @SuppressWarnings( "unchecked" )
    public static List<RepositoryTargetListResource> getList()
        throws IOException
    {

        String responseText = RequestFacade.doGetRequest( "service/local/repo_targets" ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryTargetListResourceResponse resourceResponse =
            (RepositoryTargetListResourceResponse) representation.getPayload( new RepositoryTargetListResourceResponse() );

        return resourceResponse.getData();

    }

    public RepositoryTargetResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        return this.getResourceFromResponse( responseString );
    }

    public RepositoryTargetResource getResourceFromResponse( String responseText )
        throws IOException
    {
        LOG.debug( " getResourceFromResponse: " + responseText );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseText, mediaType );

        RepositoryTargetResourceResponse resourceResponse =
            (RepositoryTargetResourceResponse) representation.getPayload( new RepositoryTargetResourceResponse() );

        return resourceResponse.getData();

    }

    public void verifyTargetsConfig( RepositoryTargetResource targetResource )
        throws IOException
    {
        ArrayList<RepositoryTargetResource> targetResources = new ArrayList<RepositoryTargetResource>();
        targetResources.add( targetResource );
        this.verifyTargetsConfig( targetResources );
    }

    @SuppressWarnings( "unchecked" )
    public void verifyTargetsConfig( List<RepositoryTargetResource> targetResources )
        throws IOException
    {
        // check the nexus.xml
        Configuration config = NexusConfigUtil.getNexusConfig();

        List<CRepositoryTarget> repoTargets = config.getRepositoryTargets();

        // TODO: we can't check the size unless we reset the config after each run...
        // check to see if the size matches
        // Assert.assertTrue( "Configuration had a different number: (" + repoTargets.size()
        // + ") of targets then expected: (" + targetResources.size() + ")",
        // repoTargets.size() == targetResources.size() );

        // look for the target by id

        for ( Iterator<RepositoryTargetResource> iter = targetResources.iterator(); iter.hasNext(); )
        {
            RepositoryTargetResource targetResource = iter.next();
            boolean found = false;

            for ( Iterator<CRepositoryTarget> iterInner = repoTargets.iterator(); iterInner.hasNext(); )
            {
                CRepositoryTarget repositoryTarget = iterInner.next();

                if ( targetResource.getId().equals( repositoryTarget.getId() ) )
                {
                    found = true;
                    Assert.assertEquals( targetResource.getId(), repositoryTarget.getId() );
                    Assert.assertEquals( targetResource.getContentClass(), repositoryTarget.getContentClass() );
                    Assert.assertEquals( targetResource.getName(), repositoryTarget.getName() );
                    Assert.assertEquals( targetResource.getPatterns(), repositoryTarget.getPatterns() );

                    break;
                }

            }

            if ( !found )
            {

                Assert.fail( "Target with ID: " + targetResource.getId() + " could not be found in configuration." );
            }
        }
    }

    public void verifyCompleteTargetsConfig( List<RepositoryTargetListResource> targets )
        throws IOException
    {
        // check the nexus.xml
        Configuration config = NexusConfigUtil.getNexusConfig();

        List<CRepositoryTarget> repoTargets = config.getRepositoryTargets();
        // check to see if the size matches
        Assert.assertTrue( "Configuration had a different number: (" + repoTargets.size()
            + ") of targets then expected: (" + targets.size() + ")", repoTargets.size() == targets.size() );

        // look for the target by id

        for ( Iterator<RepositoryTargetListResource> iter = targets.iterator(); iter.hasNext(); )
        {
            RepositoryTargetListResource targetResource = iter.next();
            boolean found = false;

            for ( Iterator<CRepositoryTarget> iterInner = repoTargets.iterator(); iterInner.hasNext(); )
            {
                CRepositoryTarget repositoryTarget = iterInner.next();

                if ( targetResource.getId().equals( repositoryTarget.getId() ) )
                {
                    found = true;
                    Assert.assertEquals( targetResource.getId(), repositoryTarget.getId() );
                    Assert.assertEquals( targetResource.getContentClass(), repositoryTarget.getContentClass() );
                    Assert.assertEquals( targetResource.getName(), repositoryTarget.getName() );

                    break;
                }

            }

            if ( !found )
            {

                Assert.fail( "Target with ID: " + targetResource.getId() + " could not be found in configuration." );
            }
        }

    }

    public static void removeAllTarget()
        throws IOException
    {
        List<RepositoryTargetListResource> targets = getList();
        for ( RepositoryTargetListResource target : targets )
        {
            Status status =
                RequestFacade.sendMessage( "service/local/repo_targets/" + target.getId(), Method.DELETE ).getStatus();
            Assert.assertTrue( "Failt to delete: " + status.getDescription(), status.isSuccess() );
        }
    }

}
