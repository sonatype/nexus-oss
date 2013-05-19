/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plexus.rest.xstream.xml;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

/**
 * A simple testing resource. Will "publish" itself on passed in token URI (/token) and will emit "token" for GETs and
 * respond with HTTP 200 to all other HTTP methods (PUT, POST) only if the token equals to entity passed in.
 *
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "XStreamPlexusResource" )
public class XStreamPlexusResource
    extends AbstractPlexusResource
{

    public XStreamPlexusResource()
    {
        super();

        setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new SimpleTestObject();
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( SimpleTestObject.class );
    }

    @Override
    public String getResourceUri()
    {
        return "/XStreamPlexusResource";
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        SimpleTestObject obj = (SimpleTestObject) payload;
        if ( obj.getData() == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }

}
