/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
import java.util.Collections;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilitiesPlexusResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.*;
import com.thoughtworks.xstream.XStream;

public class CapabilitiesMessageUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskScheduleUtil.class );
    private static XStream xstream;

    static {
        xstream = XStreamFactory.getXmlXStream();
        new CapabilitiesPlexusResource().configureXStream(xstream);
    }

    public static List<CapabilityListItemResource> list()
        throws IOException {
        String entityText = RequestFacade.doGetForText("service/local/capabilities", not(inError()));
        XStreamRepresentation representation = new XStreamRepresentation(xstream, entityText, MediaType.APPLICATION_XML);
        CapabilitiesListResponseResource scheduleResponse =
            (CapabilitiesListResponseResource) representation.getPayload(new CapabilitiesListResponseResource());
        return scheduleResponse.getData();
    }

    public static CapabilityListItemResource create(final CapabilityResource capability)
        throws IOException {
        XStreamRepresentation representation = new XStreamRepresentation(xstream, "", MediaType.APPLICATION_XML);
        CapabilityRequestResource envelope = new CapabilityRequestResource();
        envelope.setData(capability);
        representation.setPayload(envelope);

        final String entityText = RequestFacade.doPostForText("service/local/capabilities", representation, not(inError()));
        representation = new XStreamRepresentation(xstream, entityText, MediaType.APPLICATION_XML);
        CapabilityStatusResponseResource scheduleResponse =
            (CapabilityStatusResponseResource) representation.getPayload(new CapabilityStatusResponseResource());
        return scheduleResponse.getData();
    }

    public static CapabilityResource read(String id)
        throws IOException {
        String uriPart = "service/local/capabilities/" + id;
        String entityText = RequestFacade.doGetForText(uriPart, not(inError()));
        XStreamRepresentation representation =
            new XStreamRepresentation(xstream, entityText, MediaType.APPLICATION_XML);

        CapabilityResponseResource scheduleResponse =
            (CapabilityResponseResource) representation.getPayload(new CapabilityResponseResource());

        return scheduleResponse.getData();
    }

    public static CapabilityListItemResource update(CapabilityResource resource)
        throws IOException {
        XStreamRepresentation representation = new XStreamRepresentation(xstream, "", MediaType.APPLICATION_XML);
        CapabilityRequestResource envelope = new CapabilityRequestResource();
        envelope.setData(resource);
        representation.setPayload(envelope);

        String uriPart = "service/local/capabilities/" + resource.getId();
        String text = RequestFacade.doPutForText(uriPart, representation, not(inError()));
            RequestFacade.sendMessage("service/local/capabilities/" + resource.getId(), Method.PUT, representation);

        representation = new XStreamRepresentation(xstream, text, MediaType.APPLICATION_XML);

        CapabilityStatusResponseResource scheduleResponse =
            (CapabilityStatusResponseResource) representation.getPayload(new CapabilityStatusResponseResource());

        return scheduleResponse.getData();
    }

    public static void delete(String id)
        throws IOException {
        RequestFacade.doDelete("service/local/capabilities/" + id, not(inError()));
    }
}
