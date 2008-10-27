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
package org.sonatype.nexus.proxy.maven.maven2ns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.router.AbstractSearchableBasedRepositoryRouter;
import org.sonatype.nexus.util.AlphanumComparator;
import org.sonatype.nexus.util.ArtifactInfoComparator;
import org.sonatype.nexus.util.StorageItemComparator;

/**
 * The Class M2NamespaceBasedRepositoryRouter.
 * 
 * @author cstamas DISABLED PLEXUS COMPONENT, UNUSED plexus.component role-hint="m2namespace"
 */
public class M2NamespaceBasedRepositoryRouter
    extends AbstractSearchableBasedRepositoryRouter
{
    public static final String ID = "m2namespace";

    /**
     * The ContentClass.
     * 
     * @plexus.requirement role-hint="maven2-namespace"
     */
    private ContentClass contentClass;

    protected static final String[] ROOT = {
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z" };

    public String getId()
    {
        return ID;
    }

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }

    protected List<StorageItem> renderVirtualPath( ResourceStoreRequest request, boolean list )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            StorageException
    {
        // prefix
        // gid
        // aid
        // version
        // files

        List<String> requestPathList = explodeUriToList( request.getRequestPath() );

        String prefix = null;
        String gid = null;
        String aid = null;
        String version = null;
        String name = null;

        if ( requestPathList.size() > 0 )
        {
            prefix = requestPathList.get( 0 );
        }
        if ( requestPathList.size() > 1 )
        {
            gid = requestPathList.get( 1 );
        }
        if ( requestPathList.size() > 2 )
        {
            aid = requestPathList.get( 2 );
        }
        if ( requestPathList.size() > 3 )
        {
            version = requestPathList.get( 3 );
        }
        if ( requestPathList.size() > 4 )
        {
            name = requestPathList.get( 4 );
        }

        if ( list )
        {

            BooleanQuery query = null;

            if ( prefix == null )
            {
                // we are generating the "root"
                List<StorageItem> artifactList = new ArrayList<StorageItem>();

                for ( String pref : ROOT )
                {
                    DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                        this,
                        RepositoryItemUid.PATH_ROOT + pref.toUpperCase(),
                        true,
                        false );

                    artifactList.add( coll );
                }

                return artifactList;
            }
            else
            {
                query = new BooleanQuery();

                if ( gid == null )
                {
                    query.add( new BooleanClause( new PrefixQuery( new Term( ArtifactInfo.GROUP_ID, prefix.length() > 1
                        ? prefix.toLowerCase() + "."
                        : prefix.toLowerCase() ) ), BooleanClause.Occur.MUST ) );
                }
            }

            // if ( request.getRequestRepositoryId() != null )
            // {
            // searchExpr += " +r:" + request.getRequestRepositoryId();
            // }
            // if ( request.getRequestRepositoryGroupId() != null )
            // {
            // searchExpr += " +g:" + request.getRequestRepositoryGroupId();
            // }

            if ( gid != null )
            {
                query.add( new BooleanClause(
                    new TermQuery( new Term( ArtifactInfo.GROUP_ID, gid ) ),
                    BooleanClause.Occur.MUST ) );
            }
            if ( aid != null )
            {
                query.add( new BooleanClause(
                    new TermQuery( new Term( ArtifactInfo.ARTIFACT_ID, aid ) ),
                    BooleanClause.Occur.MUST ) );
            }
            if ( version != null )
            {
                query.add( new BooleanClause(
                    new TermQuery( new Term( ArtifactInfo.VERSION, version ) ),
                    BooleanClause.Occur.MUST ) );
            }

            // make list unique and ordered on smthn

            try
            {
                List<StorageItem> artifactList = null;

                if ( version != null )
                {
                    artifactList = sortAndMakeUnique( getIndexer().searchFlat(
                        new ArtifactInfoComparator( new AlphanumComparator() ),
                        query ), RepositoryItemUid.PATH_ROOT + prefix + RepositoryItemUid.PATH_ROOT + gid
                        + RepositoryItemUid.PATH_SEPARATOR + aid + RepositoryItemUid.PATH_SEPARATOR + version, null );
                }
                else if ( aid != null )
                {
                    artifactList = sortAndMakeUnique( getIndexer().searchFlat(
                        new ArtifactInfoComparator( new AlphanumComparator() ),
                        query ), RepositoryItemUid.PATH_ROOT + prefix + RepositoryItemUid.PATH_ROOT + gid
                        + RepositoryItemUid.PATH_SEPARATOR + aid, "version" );
                }
                else if ( gid != null )
                {
                    artifactList = sortAndMakeUnique( getIndexer().searchFlat(
                        new ArtifactInfoComparator( new AlphanumComparator() ),
                        query ), RepositoryItemUid.PATH_ROOT + prefix + RepositoryItemUid.PATH_ROOT + gid, "artifactId" );
                }
                else if ( prefix != null )
                {
                    artifactList = sortAndMakeUnique( getIndexer().searchFlat(
                        new ArtifactInfoComparator( new AlphanumComparator() ),
                        query ), RepositoryItemUid.PATH_ROOT + prefix, "groupId" );
                }

                return artifactList;
            }
            catch ( Exception e )
            {
                throw new StorageException( "Index operation failed: ", e );
            }

        }
        else
        {
            List<StorageItem> artifactList = new ArrayList<StorageItem>();
            if ( name != null )
            {
                // file needed
                // TODO: this is work in progress and this DOES NOT WORK
                DefaultStorageLinkItem file = new DefaultStorageLinkItem( this, name, true, false, null );

                artifactList.add( file );
            }
            else
            {
                StringBuffer sb = new StringBuffer();

                if ( prefix != null )
                {
                    sb.append( RepositoryItemUid.PATH_SEPARATOR );
                    sb.append( prefix );
                }
                if ( gid != null )
                {
                    sb.append( RepositoryItemUid.PATH_SEPARATOR );
                    sb.append( gid );
                }
                if ( aid != null )
                {
                    sb.append( RepositoryItemUid.PATH_SEPARATOR );
                    sb.append( aid );
                }
                if ( version != null )
                {
                    sb.append( RepositoryItemUid.PATH_SEPARATOR );
                    sb.append( version );
                }

                DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, sb.toString(), true, false );

                artifactList.add( coll );
            }

            return artifactList;

        }
    }

    /**
     * Explode uri to list.
     * 
     * @param uri the uri
     * @return the list< string>
     */
    protected List<String> explodeUriToList( String uri )
    {
        List<String> result = new ArrayList<String>();

        String[] explodedUri = uri.split( "/" );

        for ( int i = 0; i < explodedUri.length; i++ )
        {
            if ( explodedUri[i].length() > 0 )
            {
                result.add( explodedUri[i] );
            }
        }
        return result;
    }

    /**
     * Sort and make unique.
     * 
     * @param artifactList the artifact list
     * @param path the path
     * @param metadataKey the metadata key
     * @return the list< storage item>
     */
    protected List<StorageItem> sortAndMakeUnique( Collection<ArtifactInfo> artifactInfos, String path,
        String metadataKey )
    {
        // we do a little trick here, moving the needed key to name field
        List<StorageItem> result = new ArrayList<StorageItem>( artifactInfos.size() );

        List<String> uniqueItemList = new ArrayList<String>( artifactInfos.size() );

        for ( ArtifactInfo info : artifactInfos )
        {
            String val = info.groupId;

            if ( metadataKey != null )
            {
                try
                {
                    val = (String) getValueIncludingSuperclasses( metadataKey, info );
                }
                catch ( IllegalAccessException e )
                {
                    val = info.groupId;
                }
            }

            if ( !uniqueItemList.contains( val ) )
            {
                uniqueItemList.add( val );

                AbstractStorageItem spoofed = null;

                if ( metadataKey != null )
                {
                    DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, path
                        + RepositoryItemUid.PATH_SEPARATOR + val, true, false );
                    spoofed = coll;
                }
                else
                {
                    DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, path
                        + RepositoryItemUid.PATH_SEPARATOR + val, true, false );
                    spoofed = coll;
                }
                result.add( spoofed );
            }
        }

        Collections.sort( result, new StorageItemComparator( new AlphanumComparator() ) );

        return result;
    }

    // private stuff

    private Field getFieldByNameIncludingSuperclasses( String fieldName, Class<?> clazz )
    {
        Field retValue = null;

        try
        {
            retValue = clazz.getDeclaredField( fieldName );
        }
        catch ( NoSuchFieldException e )
        {
            Class<?> superclass = clazz.getSuperclass();

            if ( superclass != null )
            {
                retValue = getFieldByNameIncludingSuperclasses( fieldName, superclass );
            }
        }

        return retValue;
    }

    private Object getValueIncludingSuperclasses( String variable, Object object )
        throws IllegalAccessException
    {

        Field field = getFieldByNameIncludingSuperclasses( variable, object.getClass() );

        field.setAccessible( true );

        return field.get( object );
    }

}
