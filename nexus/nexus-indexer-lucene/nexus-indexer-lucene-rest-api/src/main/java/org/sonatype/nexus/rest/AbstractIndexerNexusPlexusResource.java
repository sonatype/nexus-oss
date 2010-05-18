package org.sonatype.nexus.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Request;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IteratorResultSet;
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
                            null, null, null, VersionUtils.isSnapshot( ai.version ), false, null, false, null );

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
            catch ( IllegalArtifactCoordinateException e )
            {
                getLogger().warn( "Illegal artifact coordinate.", e );

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
