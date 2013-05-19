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

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * A simple restlet that is returned as root, while allowing to recreate roots in applications per application request.
 * 
 * @author cstamas
 */
public class RetargetableRestlet
    extends Filter
{
    public RetargetableRestlet( Context context )
    {
        super( context );
    }

    @Override
    protected int doHandle( Request request, Response response )
    {
        if ( getNext() != null )
        {
            return super.doHandle( request, response );
        }
        else
        {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );

            return CONTINUE;
        }

    }
}
