/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.feeds.sources;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Build feeds entry based on maven artifacts( poms and claasifiers)
 * 
 * @author Juven Xu
 */
@Component( role = SyndEntryBuilder.class, hint = "artifact" )
public class NexusArtifactEventEntryBuilder
    extends AbstractNexusItemEventEntryBuilder
{

    @Override
    protected String buildTitle( NexusArtifactEvent event )
    {
        return getGAVString( event );
    }

    @Override
    protected String buildDescriptionMsgItem( NexusArtifactEvent event )
    {
        StringBuilder msg = new StringBuilder();

        msg.append( "The artifact '" );

        msg.append( getGAVString( event ) );

        msg.append( "' in repository '" );

        msg.append( getRepositoryName( event ) );

        msg.append( "'" );

        return msg.toString();
    }

    private Gav buildGAV( NexusArtifactEvent event )
    {
        if ( event.getNexusItemInfo() == null )
        {
            return null;
        }
        try
        {
            Repository repo = getRepositoryRegistry().getRepository( event.getNexusItemInfo().getRepositoryId() );

            if ( MavenRepository.class.isAssignableFrom( repo.getClass() ) )
            {
                return ( (MavenRepository) repo ).getGavCalculator().pathToGav( event.getNexusItemInfo().getPath() );
            }

            return null;
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().debug(
                "Feed entry contained invalid repository id " + event.getNexusItemInfo().getRepositoryId(),
                e );

            return null;
        }
    }

    private String getGAVString( NexusArtifactEvent event )
    {
        if ( event.getNexusItemInfo() == null )
        {
            return "unknown:unknown:unknown";
        }

        Gav gav = buildGAV( event );

        if ( gav == null )
        {
            return event.getNexusItemInfo().getPath();
        }

        StringBuilder result = new StringBuilder( gav.getGroupId() )
            .append( ":" ).append( gav.getArtifactId() ).append( ":" ).append(
                gav.getVersion() != null ? gav.getVersion() : "unknown" );

        if ( gav.getClassifier() != null )
        {
            result.append( ":" ).append( gav.getClassifier() );
        }

        return result.toString();

    }

    /**
     * Only when it's a pom file or the file has a classifier will we build the maven artifact entry
     */
    @Override
    public boolean shouldBuildEntry( NexusArtifactEvent event )
    {
        if ( !super.shouldBuildEntry( event ) )
        {
            return false;
        }

        Gav gav = buildGAV( event );

        if ( gav == null )
        {
            return false;
        }

        if ( !StringUtils.isEmpty( gav.getExtension() ) && gav.getExtension().toLowerCase().equals( "pom" ) )
        {
            return true;
        }

        if ( !StringUtils.isEmpty( gav.getClassifier() ) )
        {
            return true;
        }

        return false;
    }
}
