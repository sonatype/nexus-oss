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
package org.sonatype.nexus.test.utils;

import java.io.IOException;


import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.rest.model.LogConfigResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.*;
import com.thoughtworks.xstream.XStream;

/**
 * @author juven
 */
public class LogConfigMessageUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( LogConfigMessageUtil.class );

    private static final String SERVICE_URL = "service/local/log/config";

    private XStream xstream;

    private MediaType mediaType;

    public LogConfigMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;

        this.mediaType = mediaType;
    }

    public LogConfigResource getLogConfig()
        throws IOException
    {
        final String responseText = RequestFacade.doGetForText( SERVICE_URL );

        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseText, mediaType );

        LogConfigResourceResponse resourceResponse = (LogConfigResourceResponse) representation
            .getPayload( new LogConfigResourceResponse() );

        return resourceResponse.getData();

    }

    public void updateLogConfig( LogConfigResource resource )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        LogConfigResourceResponse resourceRequest = new LogConfigResourceResponse();

        resourceRequest.setData( resource );

        representation.setPayload( resourceRequest );

        LOG.debug( "requestText: \n" + representation.getText() );

        RequestFacade.doPut(SERVICE_URL, representation, isSuccessful());
    }
}
