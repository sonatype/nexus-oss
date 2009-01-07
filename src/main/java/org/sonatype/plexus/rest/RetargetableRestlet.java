/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * A simple restlet that is returned as root, while allowing to recreate roots in applications per application request.
 * 
 * @author cstamas
 */
public class RetargetableRestlet
    extends Restlet
{
    private Restlet root;

    public RetargetableRestlet( Context context )
    {
        super( context );
    }

    @Override
    public void handle( Request request, Response response )
    {
        super.handle( request, response );

        Restlet next = getRoot();

        if ( next != null )
        {
            next.handle( request, response );
        }
        else
        {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
        }
    }

    public Restlet getRoot()
    {
        return root;
    }

    public void setRoot( Restlet root )
    {
        this.root = root;
    }
}
