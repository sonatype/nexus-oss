/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.util;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;

public class PlexusUserMessageUtil
{
    private static final Logger LOG = Logger.getLogger( UserMessageUtil.class );

    @SuppressWarnings( "unchecked" )
    public List<PlexusUserResource> getList()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/plexus_users/allConfigured" );
        String responseText = response.getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        // make sure we have a success
        Assert.assertTrue( "Status: " + response.getStatus() + "\n" + responseText, response.getStatus().isSuccess() );

        PlexusUserListResourceResponse resourceResponse = (PlexusUserListResourceResponse) representation
            .getPayload( new PlexusUserListResourceResponse() );

        return resourceResponse.getData();
    }

}
