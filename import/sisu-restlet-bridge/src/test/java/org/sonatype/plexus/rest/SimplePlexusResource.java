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
package org.sonatype.plexus.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * A simple testing resource. Will "publish" itself on passed in token URI (/token) and will emit "token" for GETs and
 * respond with HTTP 200 to all other HTTP methods (PUT, POST) only if the token equals to entity passed in.
 * 
 * @author cstamas
 */
public class SimplePlexusResource
    extends AbstractPlexusResource
{
    private String token;

    public SimplePlexusResource()
    {
        super();
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/" + token;
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = new ArrayList<Variant>();

        result.add( new Variant( MediaType.TEXT_PLAIN ) );

        return result;
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return token;
    }

    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        if ( !token.equals( payload.toString() ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        return null;
    }

    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        if ( !token.equals( payload.toString() ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        return null;
    }

    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        // nothing
    }

    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        return null;
    }

}
