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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorResultSet;
import org.apache.maven.index.MatchHighlight;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Request;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NexusArtifact;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractIndexerNexusPlexusResource
    extends AbstractNexusPlexusResource
{
    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        XStreamInitializer.init( xstream );
    }

    /**
     * Convert a collection of ArtifactInfo's to NexusArtifacts
     * 
     * @param aic
     * @return
     */
    protected Collection<NexusArtifact> ai2NaColl( Request request, Collection<ArtifactInfo> aic )
    {
        if ( aic == null )
        {
            return null;
        }

        List<NexusArtifact> result = new ArrayList<NexusArtifact>();

        for ( ArtifactInfo ai : aic )
        {
            NexusArtifact na = ai2Na( request, ai );

            if ( na != null )
            {
                result.add( na );
            }
        }
        return result;
    }

    protected Collection<NexusArtifact> ai2NaColl( Request request, IteratorResultSet aic )
    {
        List<NexusArtifact> result = new ArrayList<NexusArtifact>();

        if ( aic != null )
        {
            for ( ArtifactInfo ai : aic )
            {
                NexusArtifact na = ai2Na( request, ai );

                if ( na != null )
                {
                    result.add( na );
                }
            }
        }

        return result;
    }

    protected String getMatchHighlightHtmlSnippet( ArtifactInfo ai )
    {
        if ( ai.getMatchHighlights().size() > 0 )
        {
            // <blockquote>Artifact classes
            // <ul>
            // <li>aaaa</li>
            // <li>bbbbb</li>
            // </ul>
            // </blockquote>

            StringBuilder sb = new StringBuilder();

            for ( MatchHighlight mh : ai.getMatchHighlights() )
            {
                sb.append( "<blockquote>" ).append( mh.getField().getDescription() ).append( "<UL>" );

                // TODO: fix this!
                for ( String high : mh.getHighlightedMatch() )
                {
                    sb.append( "<LI>" ).append( high ).append( "</LI>" );
                }

                sb.append( "</UL></blockquote>" );
            }

            return sb.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert from ArtifactInfo to a NexusArtifact
     * 
     * @param ai
     * @return
     */
    protected NexusArtifact ai2Na( Request request, ArtifactInfo ai )
    {
        if ( ai == null )
        {
            return null;
        }

        NexusArtifact a = new NexusArtifact();

        a.setGroupId( ai.groupId );

        a.setArtifactId( ai.artifactId );

        a.setVersion( ai.version );

        a.setClassifier( ai.classifier );

        a.setPackaging( ai.packaging );

        a.setExtension( ai.fextension );

        a.setRepoId( ai.repository );

        a.setContextId( ai.context );

        a.setHighlightedFragment( getMatchHighlightHtmlSnippet( ai ) );

        if ( ai.repository != null )
        {
            a.setPomLink( createPomLink( request, ai ) );

            a.setArtifactLink( createArtifactLink( request, ai ) );

            try
            {
                Repository repository = getUnprotectedRepositoryRegistry().getRepository( ai.repository );

                if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) )
                {
                    MavenRepository mavenRepository = (MavenRepository) repository;

                    Gav gav =
                        new Gav( ai.groupId, ai.artifactId, ai.version, ai.classifier,
                            mavenRepository.getArtifactPackagingMapper().getExtensionForPackaging( ai.packaging ),
                            null, null, null, false, null, false, null );

                    ResourceStoreRequest req =
                        new ResourceStoreRequest( mavenRepository.getGavCalculator().gavToPath( gav ) );

                    a.setResourceURI( createRepositoryReference( request, ai.repository, req.getRequestPath() ).toString() );
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().warn( "No such repository: '" + ai.repository + "'.", e );

                return null;
            }
        }

        return a;
    }

    protected String createPomLink( Request request, ArtifactInfo ai )
    {
        if ( StringUtils.isNotEmpty( ai.classifier ) )
        {
            return "";
        }

        String suffix =
            "?r=" + ai.repository + "&g=" + ai.groupId + "&a=" + ai.artifactId + "&v=" + ai.version + "&e=pom";

        return createRedirectBaseRef( request ).toString() + suffix;
    }

    protected String createArtifactLink( Request request, ArtifactInfo ai )
    {
        if ( StringUtils.isEmpty( ai.packaging ) || "pom".equals( ai.packaging ) )
        {
            return "";
        }

        String suffix =
            "?r=" + ai.repository + "&g=" + ai.groupId + "&a=" + ai.artifactId + "&v=" + ai.version + "&e="
                + ai.fextension;

        if ( StringUtils.isNotBlank( ai.classifier ) )
        {
            suffix = suffix + "&c=" + ai.classifier;
        }

        return createRedirectBaseRef( request ).toString() + suffix;
    }
}
