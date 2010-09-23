package org.sonatype.nexus.plugins.mac.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoFilter;
import org.sonatype.nexus.index.IndexArtifactFilter;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.plugins.mac.MacPlugin;
import org.sonatype.nexus.plugins.mac.MacRequest;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A simple resource that returns _all_ existing archetypes (all versions of the same GA!).
 * 
 * @author cstamas
 */
public class MacRepositoryPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{
    @Requirement
    private MacPlugin macPlugin;

    @Requirement( hint = "maven2" )
    private ContentClass maven2ContentClass;

    @Requirement
    private IndexerManager indexerManager;

    @Requirement
    private IndexArtifactFilter indexArtifactFilter;

    protected MacPlugin getMacPlugin()
    {
        return macPlugin;
    }

    protected ContentClass getMaven2ContentClass()
    {
        return maven2ContentClass;
    }

    @Override
    public Object getPayloadInstance()
    {
        // this happens to be RO resource
        return null;
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.APPLICATION_XML ) );

        return result;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // unprotected resource
        return new PathProtectionDescriptor( "/nexus-archetype-plugin/*/archetype-catalog.xml", "anon" );
    }

    @Override
    public String getResourceUri()
    {
        return "/nexus-archetype-plugin/{key}/archetype-catalog.xml";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String key = (String) request.getAttributes().get( "key" );

        try
        {
            // get and inspect a repo, we are allowing only maven2 reposes
            Repository repository = getRepositoryRegistry().getRepository( key );

            if ( !getMaven2ContentClass().isCompatible( repository.getRepositoryContentClass() ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "The repository with ID='" + key
                    + "' does not hold a Maven2 repository!" );
            }

            String repositoryReference = null;

            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                repositoryReference = createRootReference( request, "content/groups/" + key ).toString();
            }
            else
            {
                repositoryReference = createRootReference( request, "content/repositories/" + key ).toString();
            }

            // get the list FILTERED by user perms
            MacRequest req = new MacRequest( key, repositoryReference, new ArtifactInfoFilter()
            {
                public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
                {
                    return indexArtifactFilter.filterArtifactInfo( ai );
                }
            } );

            // get the catalog
            ArchetypeCatalog catalog =
                getMacPlugin().listArcherypesAsCatalog( req,
                    indexerManager.getRepositoryBestIndexContext( req.getRepositoryId() ) );

            // serialize it to XML
            ArchetypeCatalogXpp3Writer writer = new ArchetypeCatalogXpp3Writer();

            StringWriter sw = new StringWriter();

            writer.write( sw, catalog );

            return new StringRepresentation( sw.toString(), variant.getMediaType() );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
    }
}
