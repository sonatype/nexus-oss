/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.treeview;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.Field;
import org.sonatype.nexus.index.IteratorSearchRequest;
import org.sonatype.nexus.index.IteratorSearchResponse;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.SearchType;
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

    @Deprecated
    public TreeNode listNodes( TreeNodeFactory factory, String path )
        throws IOException
    {
        return listNodes( new TreeViewRequest( factory, path ) );
    }

    public TreeNode listNodes( TreeViewRequest request )
        throws IOException
    {
        // get the last path elem
        String name = null;

        if ( !"/".equals( request.getPath() ) )
        {

            if ( request.getPath().endsWith( "/" ) )
            {
                name = request.getPath().substring( 0, request.getPath().length() - 1 );
            }
            else
            {
                name = request.getPath();
            }

            name = name.substring( name.lastIndexOf( '/' ) + 1, name.length() );

            // root is "/"
            if ( !name.equals( "/" ) && name.endsWith( "/" ) )
            {
                name = name.substring( 0, name.length() - 1 );
            }

        }
        else
        {
            name = "/";
        }

        // the root node depends on request we have, so let's see
        TreeNode result = request.getFactory().createGNode( this, request.getPath(), name );

        if ( request.hasFieldHints() )
        {
            listChildren( result, request, null );
        }
        else
        {
            // non hinted way, the "old" way
            if ( "/".equals( request.getPath() ) )
            {
                // get root groups and finish
                Set<String> rootGroups = request.getFactory().getIndexingContext().getRootGroups();

                for ( String group : rootGroups )
                {
                    if ( group.length() > 0 )
                    {
                        result.getChildren().add(
                            request.getFactory().createGNode( this, request.getPath() + group + "/", group ) );
                    }
                }
            }
            else
            {
                Set<String> allGroups = request.getFactory().getIndexingContext().getAllGroups();

                listChildren( result, request, allGroups );
            }
        }

        return result;
    }

    /**
     * @param root
     * @param factory
     * @param allGroups
     * @throws IOException
     */
    protected void listChildren( TreeNode root, TreeViewRequest request, Set<String> allGroups )
        throws IOException
    {
        String path = root.getPath();

        Map<String, TreeNode> folders = new HashMap<String, TreeNode>();

        String rootPartialGroupId = StringUtils.strip( root.getPath().replaceAll( "/", "." ), "." );

        folders.put( Type.G + ":" + rootPartialGroupId, root );

        IteratorSearchResponse artifacts = getArtifacts( root, request );

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
                    TreeNode groupParentResource = root;

                    TreeNode groupResource = root;

                    // here comes the twist: we have to search for parent G node, but _backwards_ to obey the
                    // initial
                    // request's path!

                    String partialGroupId = null;

                    String[] groupIdElems = ai.groupId.split( "\\." );

                    for ( String groupIdElem : groupIdElems )
                    {
                        if ( partialGroupId == null )
                        {
                            partialGroupId = groupIdElem;
                        }
                        else
                        {
                            partialGroupId = partialGroupId + "." + groupIdElem;
                        }

                        String groupKey = Type.G + ":" + partialGroupId;

                        groupResource = folders.get( groupKey );

                        // it needs to be created only if not found (is null) and is _below_ groupParentResource
                        if ( groupResource == null
                            && groupParentResource.getPath().length() < getPathForAi( ai, MAVEN.GROUP_ID ).length() )
                        {
                            String gNodeName =
                                partialGroupId.lastIndexOf( '.' ) > -1 ? partialGroupId.substring(
                                    partialGroupId.lastIndexOf( '.' ) + 1, partialGroupId.length() ) : partialGroupId;

                            groupResource =
                                request.getFactory().createGNode( this,
                                    "/" + partialGroupId.replaceAll( "\\.", "/" ) + "/", gNodeName );

                            groupParentResource.getChildren().add( groupResource );

                            folders.put( groupKey, groupResource );

                            groupParentResource = groupResource;
                        }
                        else if (groupResource != null)
                        {
                            // we found it as already existing, break if this is the node we want
                            if ( groupResource.getPath().equals( getPathForAi( ai, MAVEN.GROUP_ID ) ) )
                            {
                                break;
                            }

                            groupParentResource = groupResource;
                        }
                    }

                    artifactResource =
                        request.getFactory().createANode( this, ai, getPathForAi( ai, MAVEN.ARTIFACT_ID ) );

                    groupParentResource.getChildren().add( artifactResource );

                    folders.put( artifactKey, artifactResource );
                }

                versionResource = request.getFactory().createVNode( this, ai, getPathForAi( ai, MAVEN.VERSION ) );

                artifactResource.getChildren().add( versionResource );

                folders.put( versionKey, versionResource );
            }

            String nodePath = getPathForAi( ai, null );

            versionResource.getChildren().add( request.getFactory().createArtifactNode( this, ai, nodePath ) );
        }

        if ( !request.hasFieldHints() )
        {
            Set<String> groups = getGroups( path, allGroups );

            for ( String group : groups )
            {
                TreeNode groupResource = root.findChildByPath( path + group + "/", Type.G );

                if ( groupResource == null )
                {
                    groupResource = request.getFactory().createGNode( this, path + group + "/", group );

                    root.getChildren().add( groupResource );
                }
                else
                {
                    // if the folder has been created as an artifact name,
                    // we need to check for possible nested groups as well
                    listChildren( groupResource, request, allGroups );
                }
            }
        }
    }

    /**
     * Builds a path out from ArtifactInfo. The field paramter controls "how deep" the path goes. Possible values are
     * MAVEN.GROUP_ID (builds a path from groupId only), MAVEN.ARTIFACT_ID (builds a path from groupId + artifactId),
     * MAVEN.VERSION (builds a path up to version) or anything else (including null) will build "full" artifact path.
     * 
     * @param ai
     * @param field
     * @return path
     */
    protected String getPathForAi( ArtifactInfo ai, Field field )
    {
        StringBuilder sb = new StringBuilder( "/" );

        sb.append( ai.groupId.replaceAll( "\\.", "/" ) );

        if ( MAVEN.GROUP_ID.equals( field ) )
        {
            // stop here
            return sb.append( "/" ).toString();
        }

        sb.append( "/" ).append( ai.artifactId );

        if ( MAVEN.ARTIFACT_ID.equals( field ) )
        {
            // stop here
            return sb.append( "/" ).toString();
        }

        sb.append( "/" ).append( ai.version );

        if ( MAVEN.VERSION.equals( field ) )
        {
            // stop here
            return sb.append( "/" ).toString();
        }

        sb.append( "/" ).append( ai.artifactId ).append( "-" ).append( ai.version );

        if ( ai.classifier != null )
        {
            sb.append( "-" ).append( ai.classifier );
        }

        sb.append( "." ).append( ai.fextension == null ? "jar" : ai.fextension );

        return sb.toString();
    }

    protected Set<String> getGroups( String path, Set<String> allGroups )
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

    protected IteratorSearchResponse getArtifacts( TreeNode root, TreeViewRequest request )
        throws IOException
    {
        if ( request.hasFieldHints() )
        {
            return getHintedArtifacts( root, request );
        }

        String path = root.getPath();

        IteratorSearchResponse result = null;

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

        result = getArtifactsByG( g, request );

        if ( result.getTotalHits() > 0 )
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

            result = getArtifactsByGA( g, a, request );

            if ( result.getTotalHits() > 0 )
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

                result = getArtifactsByGAV( g, a, v, request );

                if ( result.getTotalHits() > 0 )
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
        return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
    }

    protected IteratorSearchResponse getHintedArtifacts( TreeNode root, TreeViewRequest request )
        throws IOException
    {
        // we know that hints are there: G hint, GA hint or GAV hint
        if ( request.hasFieldHint( MAVEN.GROUP_ID, MAVEN.ARTIFACT_ID, MAVEN.VERSION ) )
        {
            return getArtifactsByGAV( request.getFieldHint( MAVEN.GROUP_ID ),
                request.getFieldHint( MAVEN.ARTIFACT_ID ), request.getFieldHint( MAVEN.VERSION ), request );
        }
        else if ( request.hasFieldHint( MAVEN.GROUP_ID, MAVEN.ARTIFACT_ID ) )
        {
            return getArtifactsByGA( request.getFieldHint( MAVEN.GROUP_ID ), request.getFieldHint( MAVEN.ARTIFACT_ID ),
                request );
        }
        else if ( request.hasFieldHint( MAVEN.GROUP_ID ) )
        {
            return getArtifactsByG( request.getFieldHint( MAVEN.GROUP_ID ), request );
        }
        else
        {
            // if we are here, no hits found or something horribly went wrong?
            return IteratorSearchResponse.EMPTY_ITERATOR_SEARCH_RESPONSE;
        }
    }

    protected IteratorSearchResponse getArtifactsByG( String g, TreeViewRequest request )
        throws IOException
    {
        return getArtifactsByGAVField( g, null, null, request );
    }

    protected IteratorSearchResponse getArtifactsByGA( String g, String a, TreeViewRequest request )
        throws IOException
    {
        return getArtifactsByGAVField( g, a, null, request );
    }

    protected IteratorSearchResponse getArtifactsByGAV( String g, String a, String v, TreeViewRequest request )
        throws IOException
    {
        return getArtifactsByGAVField( g, a, v, request );
    }

    protected IteratorSearchResponse getArtifactsByGAVField( String g, String a, String v, TreeViewRequest request )
        throws IOException
    {
        assert g != null;

        Query groupIdQ = null;
        Query artifactIdQ = null;
        Query versionQ = null;

        // minimum must have
        groupIdQ = getNexusIndexer().constructQuery( MAVEN.GROUP_ID, g, SearchType.EXACT );

        if ( StringUtils.isNotBlank( a ) )
        {
            artifactIdQ = getNexusIndexer().constructQuery( MAVEN.ARTIFACT_ID, a, SearchType.EXACT );
        }

        if ( StringUtils.isNotBlank( v ) )
        {
            versionQ = getNexusIndexer().constructQuery( MAVEN.VERSION, v, SearchType.EXACT );
        }

        BooleanQuery q = new BooleanQuery();

        q.add( new BooleanClause( groupIdQ, BooleanClause.Occur.MUST ) );

        if ( artifactIdQ != null )
        {
            q.add( new BooleanClause( artifactIdQ, BooleanClause.Occur.MUST ) );
        }

        if ( versionQ != null )
        {
            q.add( new BooleanClause( versionQ, BooleanClause.Occur.MUST ) );
        }

        IteratorSearchRequest searchRequest = new IteratorSearchRequest( q, request.getArtifactInfoFilter() );

        IteratorSearchResponse result = getNexusIndexer().searchIterator( searchRequest );

        return result;
    }
}
