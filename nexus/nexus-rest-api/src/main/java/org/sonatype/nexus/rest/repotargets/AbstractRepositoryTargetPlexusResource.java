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
package org.sonatype.nexus.rest.repotargets;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;

public abstract class AbstractRepositoryTargetPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private TargetRegistry targetRegistry;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    protected TargetRegistry getTargetRegistry()
    {
        return targetRegistry;
    }

    protected RepositoryTypeRegistry getRepositoryTypeRegistry()
    {
        return repositoryTypeRegistry;
    }

    protected RepositoryTargetResource getNexusToRestResource( Target target, Request request )
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        resource.setId( target.getId() );

        resource.setName( target.getName() );

        resource.setResourceURI( request.getResourceRef().getPath() );

        resource.setContentClass( target.getContentClass().getId() );

        List<String> patterns = new ArrayList<String>( target.getPatternTexts() );

        for ( String pattern : patterns )
        {
            resource.addPattern( pattern );
        }

        return resource;
    }

    protected Target getRestToNexusResource( RepositoryTargetResource resource )
        throws ConfigurationException, PatternSyntaxException
    {
        ContentClass cc = getRepositoryTypeRegistry().getContentClasses().get( resource.getContentClass() );

        if ( cc == null )
        {
            throw new ConfigurationException( "Content class with ID=\"" + resource.getContentClass()
                + "\" does not exists!" );
        }

        Target target = new Target( resource.getId(), resource.getName(), cc, resource.getPatterns() );

        return target;
    }

    protected boolean validate( boolean isNew, RepositoryTargetResource resource )
    {
        if ( isNew )
        {
            if ( resource.getId() == null )
            {
                resource.setId( Long.toHexString( System.nanoTime() ) );
            }
        }

        if ( resource.getId() == null )
        {
            return false;
        }
        
        return true;
    }

}
