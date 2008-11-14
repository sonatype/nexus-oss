package org.sonatype.nexus.index.treeview;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.TreeNode.Type;

@Component( role = IndexTreeView.class )
public class DefaultIndexTreeView
    extends AbstractLogEnabled
    implements IndexTreeView
{
    @Requirement
    private NexusIndexer nexusIndexer;

    protected NexusIndexer getNexusIndexer()
    {
        return nexusIndexer;
    }

    public TreeNode listNodes( TreeNodeFactory factory, String path )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        TreeNode result = factory.createGNode( this, path, path );

        if ( "/".equals( path ) )
        {
            // get root groups and finish
            Set<String> rootGroups = getNexusIndexer().getRootGroups( factory.getIndexingContext() );

            for ( String group : rootGroups )
            {
                if ( group.length() > 0 )
                {
                    result.getChildren().add( factory.createGNode( this, path + group + "/", group ) );
                }
            }
        }
        else
        {
            Set<String> allGroups = getNexusIndexer().getAllGroups( factory.getIndexingContext() );

            listChildren( result, factory, allGroups );
        }

        return result;
    }

    /**
     * @param root
     * @param factory
     * @param allGroups
     * @throws IndexContextInInconsistentStateException
     * @throws IOException
     */
    protected void listChildren( TreeNode root, TreeNodeFactory factory, Set<String> allGroups )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        String path = root.getPath();

        Map<String, TreeNode> folders = new HashMap<String, TreeNode>();

        Set<ArtifactInfo> artifacts = getArtifacts( path, factory.getIndexingContext() );

        for ( ArtifactInfo ai : artifacts )
        {
            String versionKey = Type.V + ":" + ai.artifactId + ":" + ai.version;

            TreeNode versionResource = folders.get( versionKey );

            if ( versionResource == null )
            {
                String artifactKey = Type.A + ":" + ai.artifactId;

                TreeNode artifactResource = folders.get( artifactKey );

                if ( artifactResource == null )
                {
                    artifactResource = factory.createANode( this, ai, path + ai.artifactId + "/" );

                    root.getChildren().add( artifactResource );

                    folders.put( artifactKey, artifactResource );
                }

                versionResource = factory.createVNode( this, ai, path + ai.artifactId + "/" + ai.version + "/" );

                artifactResource.getChildren().add( versionResource );

                folders.put( versionKey, versionResource );
            }

            String nodePath = getPathForAi( path, ai );

            versionResource.getChildren().add( factory.createArtifactNode( this, ai, nodePath ) );

            // TODO: do we need to represent these are another artifacts?
            // The correct answer is YES, but...
            // if ( ArtifactAvailablility.PRESENT.equals( ai.javadocExists ) )
            // {
            // ai.classifier = "javadoc";
            //
            // nodePath = getPathForAi( path, ai );
            //
            // versionResource.getChildren().add( factory.createArtifactNode( this, ai, nodePath ) );
            // }
            // if ( ArtifactAvailablility.PRESENT.equals( ai.sourcesExists ) )
            // {
            // ai.classifier = "sources";
            //
            // nodePath = getPathForAi( path, ai );
            //
            // versionResource.getChildren().add( factory.createArtifactNode( this, ai, nodePath ) );
            // }
        }

        Set<String> groups = getGroups( path, allGroups );

        for ( String group : groups )
        {
            TreeNode groupResource = findChildByPath( root, path + group + "/", Type.G );

            if ( groupResource == null )
            {
                groupResource = factory.createGNode( this, path + group + "/", group );

                root.getChildren().add( groupResource );
            }
            else
            {
                // if the folder has been created as an artifact name,
                // we need to check for possible nested groups as well
                listChildren( groupResource, factory, allGroups );
            }
        }
    }

    protected String getPathForAi( String path, ArtifactInfo ai )
    {
        StringBuffer sb = new StringBuffer( path ).append( ai.artifactId ).append( "/" ).append( ai.version ).append(
            "/" ).append( ai.artifactId ).append( "-" ).append( ai.version );

        if ( ai.classifier != null )
        {
            sb.append( "-" ).append( ai.classifier );
        }

        sb.append( "." ).append( ai.fextension == null ? "jar" : ai.fextension );

        return sb.toString();
    }

    protected Set<String> getGroups( String path, Set<String> allGroups )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        path = path.substring( 1 ).replace( '/', '.' );

        int n = path.length();

        Set<String> result = new HashSet<String>();

        for ( String group : allGroups )
        {
            if ( group.startsWith( path ) )
            {
                group = group.substring( n );

                int nextDot = group.indexOf( '.' );

                if ( nextDot > -1 )
                {
                    group = group.substring( 0, nextDot );
                }

                if ( group.length() > 0 && !result.contains( group ) )
                {
                    result.add( group );
                }
            }
        }

        return result;
    }

    protected Set<ArtifactInfo> getArtifacts( String path, IndexingContext indexingContext )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        Set<ArtifactInfo> result = null;

        String g = null;

        String a = null;

        String v = null;

        // "working copy" of path
        String wp = null;

        // remove last / from path
        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        // 1st try, let's consider path is a group

        // reset wp
        wp = path;

        g = wp.substring( 1 ).replace( '/', '.' );

        result = getArtifactsByG( g, indexingContext );

        if ( !result.isEmpty() )
        {
            return result;
        }

        // 2nd try, lets consider path a group + artifactId, we must ensure there is at least one / but not as root

        if ( path.lastIndexOf( "/" ) > 0 )
        {
            // reset wp
            wp = path;

            a = wp.substring( wp.lastIndexOf( "/" ) + 1, wp.length() );

            g = wp.substring( 1, wp.lastIndexOf( "/" ) ).replace( '/', '.' );

            result = getArtifactsByGA( g, a, indexingContext );

            if ( !result.isEmpty() )
            {
                return result;
            }

            // 3rd try, let's consider path a group + artifactId + version. There is no 100% way to detect this!

            try
            {
                // reset wp
                wp = path;

                v = wp.substring( wp.lastIndexOf( "/" ) + 1, wp.length() );

                wp = wp.substring( 0, wp.lastIndexOf( "/" ) );

                a = wp.substring( wp.lastIndexOf( "/" ) + 1, wp.length() );

                g = wp.substring( 1, wp.lastIndexOf( "/" ) ).replace( '/', '.' );

                result = getArtifactsByGAV( g, a, v, indexingContext );

                if ( !result.isEmpty() )
                {
                    return result;
                }
            }
            catch ( StringIndexOutOfBoundsException e )
            {
                // nothing
            }
        }

        // if we are here, no hits found
        return Collections.emptySet();
    }

    protected Set<ArtifactInfo> getArtifactsByG( String g, IndexingContext indexingContext )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        Query q = new TermQuery( new Term( ArtifactInfo.GROUP_ID, g ) );

        FlatSearchRequest searchRequest = new FlatSearchRequest( q, indexingContext );

        FlatSearchResponse searchResponse = getNexusIndexer().searchFlat( searchRequest );

        return searchResponse.getResults();
    }

    protected Set<ArtifactInfo> getArtifactsByGA( String g, String a, IndexingContext indexingContext )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        BooleanQuery q = new BooleanQuery();

        q.add( new TermQuery( new Term( ArtifactInfo.GROUP_ID, g ) ), BooleanClause.Occur.MUST );

        q.add( nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "\"" + a + "\"" ), BooleanClause.Occur.MUST );

        FlatSearchRequest searchRequest = new FlatSearchRequest( q, indexingContext );

        FlatSearchResponse searchResponse = getNexusIndexer().searchFlat( searchRequest );

        return searchResponse.getResults();
    }

    protected Set<ArtifactInfo> getArtifactsByGAV( String g, String a, String v, IndexingContext indexingContext )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        BooleanQuery q = new BooleanQuery();

        q.add( new TermQuery( new Term( ArtifactInfo.GROUP_ID, g ) ), BooleanClause.Occur.MUST );

        q.add( nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "\"" + a + "\"" ), BooleanClause.Occur.MUST );

        q.add( nexusIndexer.constructQuery( ArtifactInfo.VERSION, "\"" + v + "\"" ), BooleanClause.Occur.MUST );

        FlatSearchRequest searchRequest = new FlatSearchRequest( q, indexingContext );

        FlatSearchResponse searchResponse = getNexusIndexer().searchFlat( searchRequest );

        return searchResponse.getResults();
    }

    protected TreeNode findChildByPath( TreeNode parent, String path, Type type )
        throws IndexContextInInconsistentStateException,
            IOException
    {
        for ( TreeNode child : parent.getChildren() )
        {
            if ( path.equals( child.getPath() ) && type.equals( child.getType() ) )
            {
                return child;
            }
        }

        return null;
    }
}
