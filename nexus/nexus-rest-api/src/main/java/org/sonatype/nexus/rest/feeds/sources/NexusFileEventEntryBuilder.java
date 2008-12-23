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
package org.sonatype.nexus.rest.feeds.sources;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.NexusArtifactEvent;

/**
 * Build feeds entry based on files
 * 
 * @author Juven Xu
 */
@Component( role = SyndEntryBuilder.class, hint = "file" )
public class NexusFileEventEntryBuilder
    extends AbstractNexusItemEventEntryBuilder
{

    @Override
    protected String buildTitle( NexusArtifactEvent event )
    {
        return buildFileName( event );
    }

    private String buildFileName( NexusArtifactEvent event )
    {
        return buildFilePath( event ).substring( buildFilePath( event ).lastIndexOf( "/" ) + 1 );
    }

    private String buildFilePath( NexusArtifactEvent event )
    {
        return event.getNexusItemInfo().getPath();
    }

    @Override
    protected String buildDescriptionMsgItem( NexusArtifactEvent event )
    {
        StringBuffer msg = new StringBuffer();

        msg.append( "The file '" );

        msg.append( buildFileName( event ) );

        msg.append( "' in repository '" );

        msg.append( event.getNexusItemInfo().getRepositoryId() );

        msg.append( "' with path '" );

        msg.append( buildFilePath( event ) );

        msg.append( "'" );

        return msg.toString();
    }

}
