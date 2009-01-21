/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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

        msg.append( getRepositoryName( event ) );

        msg.append( "' with path '" );

        msg.append( buildFilePath( event ) );

        msg.append( "'" );

        return msg.toString();
    }

}
