package org.damian;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.GroupedSearchRequest;
import org.sonatype.nexus.index.GroupedSearchResponse;
import org.sonatype.nexus.index.Grouping;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.search.grouping.GAVGrouping;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;

/**
 * Sample app to show how to integrate with the nexus indexer.  Note that this is a simple plexus
 * component extending the SampleApp interface
 * 
 * public interface SampleApp
 * {
 *    void index() 
 *        throws IOException;
 *    
 *    Set<ArtifactInfo> searchIndexFlat( String field, String value ) 
 *        throws IOException;
 *    
 *    Set<ArtifactInfo> searchIndexFlat( Query query )
 *        throws IOException;
 *    
 *    Map<String, ArtifactInfoGroup> searchIndexGrouped( String field, String value )
 *        throws IOException;
 *    
 *    Map<String, ArtifactInfoGroup> searchIndexGrouped( String field, String value, Grouping grouping )
 *        throws IOException;
 *    
 *    Map<String, ArtifactInfoGroup> searchIndexGrouped( Query q, Grouping grouping )
 *        throws IOException;
 *        
 *    void publishIndex( File targetDirectory )
 *        throws IOException;
 *        
 *    void updateRemoteIndex()
 *        throws IOException;
 * }
 * 
 * @author Damian
 *
 */
@Component( role = SampleApp.class )
public class DefaultSampleApp
    implements SampleApp,
        Initializable,
        Disposable
{
    // The nexus indexer
    @Requirement
    private NexusIndexer indexer;
    
    // The nexus index packer
    @Requirement
    private IndexPacker indexPacker;
    
    // The nexus index updater
    @Requirement
    private IndexUpdater indexUpdater;
    
    // The list of index creators we will be using (all of them)
    @Requirement( role = IndexCreator.class )
    private List<IndexCreator> indexCreators;
    
    // The indexing context
    private IndexingContext context = null;
    
    // The path to the repository to index, value will be pulled from
    // the plexus context
    @Configuration( value = "${repository.path}" )
    private File repositoryDirectoryPath;
    
    // The path to store index files, value will be pulled from 
    // the plexus context
    @Configuration( value = "${index.path}")
    private File indexDirectoryPath;
    
    // Initialize the index context
    public void initialize()
        throws InitializationException
    {
        try
        {
            // Add the indexing context
            context = indexer.addIndexingContext( 
                // id of the context
                "sample", 
                // id of the repository
                "sampleRepo", 
                // directory containing repository
                repositoryDirectoryPath,
                // directory where index will be stored
                indexDirectoryPath,
                // remote repository url...not in this example
                null, 
                // index update url...not in this example
                null, 
                // list of index creators
                indexCreators );
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            throw new InitializationException( "Error initializing IndexingContext", e );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Error initializing IndexingContext", e );
        }
    }
    
    // clean up the context
    public void dispose()
    {
        if ( context != null )
        {
            // Remove the index files, typically would not want to remove the index files, so
            // would pass in false, but this is just a test app...
            try
            {
                indexer.removeIndexingContext( context, true );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
    
    // index the repository
    public void index() 
        throws IOException
    {        
        // Perform the scan, which will index all artifacts in the repository directory
        // once this is done, searching will be available
        indexer.scan( context );
    }
    
    // search for artifacts
    public Set<ArtifactInfo> searchIndexFlat( String field, String value ) 
        throws IOException
    {
        // Build a query that will search the documents for the field set to the supplied value
        // This uses predefined logic to define the query
        // See http://svn.sonatype.org/nexus/trunk/nexus-indexer/src/main/java/org/sonatype/nexus/index/DefaultQueryCreator.java
        // for details
        Query query = indexer.constructQuery( field, value );
        
        return searchIndexFlat( query );
    }
    
    // search for artifacts using pre-built query
    public Set<ArtifactInfo> searchIndexFlat( Query query )
        throws IOException
    {
        // Build the request
        FlatSearchRequest request = new FlatSearchRequest( query );
        
        // Perform the search
        FlatSearchResponse response = indexer.searchFlat( request );
        
        // Return the artifact info objects
        return response.getResults();
    }
    
    public Map<String, ArtifactInfoGroup> searchIndexGrouped( String field, String value )
        throws IOException
    {
        // We will simply use the GAV grouping, meaning that each groupId/artifactId/version/classifier
        // will have its own entry in the returned map        
        return searchIndexGrouped( field, value, new GAVGrouping() );
    }
    
    public Map<String, ArtifactInfoGroup> searchIndexGrouped( String field, String value, Grouping grouping )
        throws IOException
    {
        // Build a query that will search the documents for the field set to the supplied value
        // This uses predefined logic to define the query
        // See http://svn.sonatype.org/nexus/trunk/nexus-indexer/src/main/java/org/sonatype/nexus/index/DefaultQueryCreator.java
        // for details
        Query query = indexer.constructQuery( field, value );
        
        return searchIndexGrouped( query, grouping );
    }
    
    public Map<String, ArtifactInfoGroup> searchIndexGrouped( Query q, Grouping grouping )
        throws IOException
    {
        GroupedSearchRequest request = new GroupedSearchRequest( q, grouping );
        
        GroupedSearchResponse response = indexer.searchGrouped( request );
        
        return response.getResults();
    }
    
    public void publishIndex( File targetDirectory )
        throws IOException
    {
        IndexPackingRequest packReq = new IndexPackingRequest( context, targetDirectory );
        packReq.setCreateChecksumFiles( true );
        packReq.setCreateIncrementalChunks( true );
        
        //NOTE: There are numerous other options you can set in the index pack request
        
        indexPacker.packIndex( packReq );
    }
    
    public void updateRemoteIndex()
        throws IOException
    {
        IndexUpdateRequest updRequest = new IndexUpdateRequest( context );
        
        //not too much to configure with the IndexUpdateRequest, but you can
        //supply your own ResourceFetcher if you would like to use some other
        //means to retrieve the index file than the default.  You can also
        //add auth and proxy info to default ResourceFetcher, and you can set
        //a transfer listener to be notified of transfer events.
        
        //But by default, will simply use the remote index url assigned to the index
        //context to retrieve new index file, and merge locally
        
        //also you can force full update, which will wipe out what is local, and replace
        //with the latest remote content.
        
        indexUpdater.fetchAndUpdateIndex( updRequest );
    }
}
