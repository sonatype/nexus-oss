/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.plugins.rest.StaticResource;

public class StaticResourceFinder
    extends Finder
{
    private final Context context;

    private final StaticResource resource;

    public StaticResourceFinder( Context context, StaticResource resource )
    {
        this.context = context;

        this.resource = resource;
    }

    public Handler createTarget( Request request, Response response )
    {
        StaticResourceResource resourceResource = new StaticResourceResource( context, request, response, resource );

        return resourceResource;
    }

}
