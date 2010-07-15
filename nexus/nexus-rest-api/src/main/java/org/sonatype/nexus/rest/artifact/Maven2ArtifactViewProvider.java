package org.sonatype.nexus.rest.artifact;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Request;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.RequestRoute;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResource;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResourceRespose;

/**
 * Returns Maven2 artifact information.
 * 
 * @author Brian Demers
 * @author cstamas
 */
@Component( role = ArtifactViewProvider.class, hint = "maven2" )
public class Maven2ArtifactViewProvider
    implements ArtifactViewProvider
{
    @Requirement
    private Logger logger;

    public Object retrieveView( ResourceStore store, ResourceStoreRequest request, StorageItem item, Request req )
        throws IOException
    {
        Repository itemRepository = null;

        String itemPath = null;

        // We can always say the M2 info only based on path
        // but how to get stuff needed for that is kinda two-fold: either the item is present/cached and is passed in
        // or it is not present. In 1st case, we use the item itself to get the "needed" stuff, otherwise we can
        // easily calculate what it would be.
        if ( item == null )
        {
            if ( store instanceof RepositoryRouter )
            {
                RepositoryRouter repositoryRouter = (RepositoryRouter) store;
                // item is either not present or is not here yet (remote index)
                // the we can "simulate" what route would be used to get it, and just get info from the route
                RequestRoute route;

                try
                {
                    route = repositoryRouter.getRequestRouteForRequest( request );
                }
                catch ( ItemNotFoundException e )
                {
                    // this is thrown while getting routes for any path "outside" of legal ones is given
                    // like /content/foo/bar, since 2nd pathelem may be "repositories", "groups", "shadows", etc
                    // (depends on
                    // type of registered reposes)
                    return null;
                }

                // request would be processed by targeted repository
                itemRepository = route.getTargetedRepository();

                // request would be processed against this repository path
                itemPath = route.getRepositoryPath();
            }
            else if ( store instanceof Repository )
            {
                itemRepository = (Repository) store;

                itemPath = request.getRequestPath();
            }
            else
            {
                return null;
            }
        }
        else
        {
            // item is here, use it to get information
            // get item's repository, from where it is actually coming
            itemRepository = item.getRepositoryItemUid().getRepository();

            // get item's in-repository path (not same as item.getPath()!)
            itemPath = item.getRepositoryItemUid().getPath();
        }

        // is this a MavenRepository at all? If not, this view is not applicable
        if ( !itemRepository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
        {
            // this items comes from a non-maven repository, this view is not applicable
            return null;
        }
        else
        {
            // we need maven repository for this operation, but we actually don't care is this
            // maven2 or mave1 repository! Let's handle this in generic way.
            MavenRepository mavenRepository = itemRepository.adaptToFacet( MavenRepository.class );

            try
            {
                // use maven repository's corresponding GavCalculator instead of "wired in" one!
                Gav gav = mavenRepository.getGavCalculator().pathToGav( itemPath );

                if ( gav == null || gav.isSignature() || gav.isHash() )
                {
                    // if we cannot calculate the gav, it is not a maven artifact (or hash/sig), return null;
                    return null;
                }

                // if we are here, we have GAV, so just pack it and send it back
                Maven2ArtifactInfoResourceRespose response = new Maven2ArtifactInfoResourceRespose();
                Maven2ArtifactInfoResource data = new Maven2ArtifactInfoResource();
                response.setData( data );

                data.setGroupId( gav.getGroupId() );
                data.setArtifactId( gav.getArtifactId() );
                data.setBaseVersion( gav.getBaseVersion() );
                data.setVersion( gav.getVersion() );
                data.setExtension( gav.getExtension() );
                data.setClassifier( gav.getClassifier() );

                data.setDependencyXmlChunk( generateDependencyXml( gav ) );

                return response;
            }
            catch ( IllegalArtifactCoordinateException e )
            {
                logger.debug( "Failed to calculate maven 2 path." );

                // could not convert item path to GAV, probably a file in maven repository not on "maven1/2 layout"
                // return just "not applicable" (example: maven-metadata.xml or any other file that is not addressable
                // by maven1/2)
                return null;
            }
        }
    }

    private String generateDependencyXml( Gav gav )
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append( "<dependency>\n" );
        buffer.append( "  <groupId>" ).append( gav.getGroupId() ).append( "</groupId>\n" );
        buffer.append( "  <artifactId>" ).append( gav.getArtifactId() ).append( "</artifactId>\n" );
        buffer.append( "  <version>" ).append( gav.getBaseVersion() ).append( "</version>\n" );

        if ( StringUtils.isNotEmpty( gav.getClassifier() ) )
        {
            buffer.append( "  <classifier>" ).append( gav.getClassifier() ).append( "</classifier>\n" );
        }

        if ( StringUtils.isNotEmpty( gav.getExtension() ) && !StringUtils.equalsIgnoreCase( "jar", gav.getExtension() ) )
        {
            buffer.append( "  <type>" ).append( gav.getExtension() ).append( "</type>\n" );
        }

        buffer.append( "</dependency>" );

        return buffer.toString();
    }

}
