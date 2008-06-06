/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.attributes;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.rest.restore.AbstractRestoreResourceHandler;
import org.sonatype.nexus.tasks.RebuildAttributesTask;

public class AttributesResourceHandler
    extends AbstractRestoreResourceHandler
{

    public AttributesResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public void handleDelete()
    {
        RebuildAttributesTask task = (RebuildAttributesTask) createTaskInstance( RebuildAttributesTask.class.getName() );

        task.setRepositoryId( getRepositoryId() );

        task.setRepositoryGroupId( getRepositoryGroupId() );

        super.handleDelete( task );
    }

}
