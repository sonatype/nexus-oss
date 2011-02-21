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
package org.sonatype.nexus.rest;

import org.restlet.data.Form;
import org.restlet.data.Response;

public class StaticHeaderUtil
{
    // cstamas:
    // Um, this is NOT the way to do this. Representation#setExpirationDate()!!!
    // Read APIDocs!
    // These kind of trickeries should be AVOIDED! Adding directly headers to a response (that belong to entity in response body)
    // without even knowing that a response have any entity is a way to hell! And bugs...
    @Deprecated
    public static void addResponseHeaders( Response response )
    {
        Form responseHeaders = (Form) response.getAttributes().get("org.restlet.http.headers");   
        
        if (responseHeaders == null)   
        {   
            responseHeaders = new Form();   
            response.getAttributes().put("org.restlet.http.headers", responseHeaders);   
        }   
          
        // Default cache for 30 days
        responseHeaders.add("Cache-Control", "max-age=2592000");
    }
}
