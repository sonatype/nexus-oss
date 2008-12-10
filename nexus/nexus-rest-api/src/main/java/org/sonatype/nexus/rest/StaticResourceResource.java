/**
 * Sonatype NexusTM [Open Source Version].
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
package org.sonatype.nexus.rest;

import java.util.Collections;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.rest.StaticResource;

public class StaticResourceResource
    extends Resource
{
    private final StaticResource resource;

    public StaticResourceResource( Context ctx, Request req, Response rsp, StaticResource resource )
    {
        super( ctx, req, rsp );

        setVariants( Collections.singletonList( new Variant( MediaType.valueOf( resource.getContentType() ) ) ) );

        this.resource = resource;
    }

    public Representation represent( Variant variant )
        throws ResourceException
    {
        return new StaticResourceRepresentation( resource );
    }

}
