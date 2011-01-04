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
package org.sonatype.nexus.test.utils;

import java.io.IOException;


import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

public class RepositoryTemplateMessageUtil
{
    private static final Logger LOG = Logger.getLogger( RepositoryTemplateMessageUtil.class );

    public static final String TEMPLATE_PROXY_SNAPSHOT = "default_proxy_snapshot";

    public static final String TEMPLATE_PROXY_RELEASE = "default_proxy_release";

    public RepositoryBaseResource getTemplate( String id )
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/templates/repositories/" + id );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().isError() )
        {
            Assert.fail( "Error on request: " + response.getStatus() + "\n" + responseText );
        }

        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        RepositoryResourceResponse resourceResponse = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

        return resourceResponse.getData();
    }
}
