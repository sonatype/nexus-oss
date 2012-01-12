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
package org.sonatype.nexus.plugins.p2.repository.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.metadata.AbstractP2MetadataSource;
import org.sonatype.nexus.plugins.p2.repository.metadata.Artifacts;
import org.sonatype.nexus.plugins.p2.repository.metadata.ArtifactsMerge;
import org.sonatype.nexus.plugins.p2.repository.metadata.Content;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataMergeException;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataSource;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = P2MetadataSource.class, hint = "group" )
public class P2GroupMetadataSource
    extends AbstractP2MetadataSource<P2GroupRepository>
{
    private static final String ATTR_HASH_PREFIX = "original";

    protected List<Repository> getMemberRepositories( final P2GroupRepository repository )
        throws StorageException
    {
        return repository.getMemberRepositories();
    }

    @Override
    protected Xpp3Dom doRetrieveArtifactsDom( final Map<String, Object> context, final P2GroupRepository repository )
        throws StorageException, ItemNotFoundException
    {
        final ArrayList<Xpp3Dom> doms =
            parseItemContent( P2Constants.ARTIFACTS_XML, P2Constants.ARTIFACTS_JAR, context, repository );

        final ArrayList<Artifacts> artifacts = new ArrayList<Artifacts>();
        for ( final Xpp3Dom dom : doms )
        {
            artifacts.add( new Artifacts( dom ) );
        }

        final ArtifactsMerge m = new ArtifactsMerge();

        try
        {
            final Artifacts metadata = m.mergeArtifactsMetadata( getName( repository ), artifacts );

            final LinkedHashMap<String, String> properties = metadata.getProperties();
            // properties.put( P2Facade.PROP_REPOSITORY_ID, repository.getId() );
            metadata.setProperties( properties );

            return metadata.getDom();
        }
        catch ( final P2MetadataMergeException e )
        {
            throw new StorageException( e );
        }

    }

    @Override
    protected Xpp3Dom doRetrieveContentDom( final Map<String, Object> context, final P2GroupRepository repository )
        throws StorageException, ItemNotFoundException
    {
        final ArrayList<Xpp3Dom> doms =
            parseItemContent( P2Constants.CONTENT_XML, P2Constants.CONTENT_JAR, context, repository );

        final ArrayList<Content> artifacts = new ArrayList<Content>();
        for ( final Xpp3Dom dom : doms )
        {
            artifacts.add( new Content( dom ) );
        }

        final ArtifactsMerge m = new ArtifactsMerge();

        try
        {
            final Content metadata = m.mergeContentMetadata( getName( repository ), artifacts );

            final LinkedHashMap<String, String> properties = metadata.getProperties();
            // properties.put( P2Facade.PROP_REPOSITORY_ID, repository.getId() );
            metadata.setProperties( properties );

            return metadata.getDom();
        }
        catch ( final P2MetadataMergeException e )
        {
            throw new StorageException( e );
        }
    }

    private ArrayList<Xpp3Dom> parseItemContent( final String xmlName, final String jarName,
                                                 final Map<String, Object> context, final P2GroupRepository repository )
        throws StorageException, ItemNotFoundException
    {
        final ArrayList<Xpp3Dom> doms = new ArrayList<Xpp3Dom>();

        for ( final Repository repo : getMemberRepositories( repository ) )
        {
            if ( repo.getLocalStatus().shouldServiceRequest() )
            {
                doms.add( parseItem( repo, jarName, xmlName, context ) );
            }
        }

        return doms;
    }

    @Override
    protected StorageItem doRetrieveRemoteItem( final Repository repo, final String path,
                                                final Map<String, Object> context )
        throws ItemNotFoundException, StorageException
    {
        final ResourceStoreRequest req = new ResourceStoreRequest( path );
        req.getRequestContext().putAll( context );

        try
        {
            return repo.retrieveItem( req );
        }
        catch ( final IllegalOperationException e )
        {
            throw new StorageException( e );
        }
        catch ( final AccessDeniedException e )
        {
            throw new ItemNotFoundException( req, repo );
        }
    }

    @Override
    protected boolean isArtifactsOld( final AbstractStorageItem artifactsItem, final P2GroupRepository repository )
        throws StorageException
    {
        final Map<String, Object> context = new HashMap<String, Object>();

        return isOld( artifactsItem, P2Constants.ARTIFACTS_JAR, P2Constants.ARTIFACTS_XML, context, repository );
    }

    @Override
    protected boolean isContentOld( final AbstractStorageItem contentItem, final P2GroupRepository repository )
        throws StorageException
    {
        final Map<String, Object> context = new HashMap<String, Object>();

        return isOld( contentItem, P2Constants.CONTENT_JAR, P2Constants.CONTENT_XML, context, repository );
    }

    @Override
    protected void setItemAttributes( final StorageFileItem item, final Map<String, Object> context,
                                      final P2GroupRepository repository )
        throws StorageException
    {
        if ( P2Constants.ARTIFACTS_JAR.equals( item.getPath() ) || P2Constants.ARTIFACTS_XML.equals( item.getPath() ) )
        {
            item.getAttributes().putAll(
                getMemberHash( P2Constants.ARTIFACTS_JAR, P2Constants.ARTIFACTS_XML, context, repository ) );
        }
        else if ( P2Constants.CONTENT_JAR.equals( item.getPath() ) || P2Constants.CONTENT_XML.equals( item.getPath() ) )
        {
            item.getAttributes().putAll(
                getMemberHash( P2Constants.CONTENT_JAR, P2Constants.CONTENT_XML, context, repository ) );
        }
    }

    private boolean isOld( final AbstractStorageItem artifactsItem, final String jar, final String xml,
                           final Map<String, Object> context, final P2GroupRepository repository )
        throws StorageException
    {
        final TreeMap<String, String> memberHash = getMemberHash( jar, xml, context, repository );

        final LinkedHashMap<String, String> hash = new LinkedHashMap<String, String>();

        final Map<String, String> attributes = artifactsItem.getAttributes();
        for ( final Map.Entry<String, String> entry : attributes.entrySet() )
        {
            if ( entry.getKey().startsWith( ATTR_HASH_PREFIX ) )
            {
                hash.put( entry.getKey(), entry.getValue() );
            }
        }

        return !hash.equals( memberHash );
    }

    private TreeMap<String, String> getMemberHash( final String jar, final String xml,
                                                   final Map<String, Object> context, final P2GroupRepository repository )
    {
        final TreeMap<String, String> memberHash = new TreeMap<String, String>();

        int count = 0;

        try
        {
            for ( final StorageItem original : retrieveItems( jar, xml, context, repository ) )
            {
                final String hash = original.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
                if ( hash == null )
                {
                    // assume it has changed
                    return null;
                }

                memberHash.put( ATTR_HASH_PREFIX + count + "." + original.getRepositoryItemUid().toString(), hash );

                count++;
            }
        }
        catch ( final StorageException e )
        {
            // assume it has changed
            return null;
        }

        return memberHash;
    }

    private List<StorageItem> retrieveItems( final String jar, final String xml, final Map<String, Object> context,
                                             final P2GroupRepository repository )
        throws StorageException
    {
        final ArrayList<StorageItem> items = new ArrayList<StorageItem>();
        for ( final Repository repo : getMemberRepositories( repository ) )
        {
            if ( repo.getLocalStatus().shouldServiceRequest() )
            {
                try
                {
                    items.add( doRetrieveRemoteItem( repo, jar, context ) );
                }
                catch ( final ItemNotFoundException e )
                {
                    try
                    {
                        items.add( doRetrieveRemoteItem( repo, xml, context ) );
                    }
                    catch ( final ItemNotFoundException e1 )
                    {
                        throw new StorageException( "Could not retrieve neither " + jar + " nor " + xml + " from "
                            + repo.getId() );
                    }
                }
            }
        }
        return items;
    }
}
