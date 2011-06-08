/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
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

    protected List<Repository> getMemberRepositories( P2GroupRepository repository )
        throws StorageException
    {
        return repository.getMemberRepositories();
    }

    @Override
    protected Xpp3Dom doRetrieveArtifactsDom( Map<String, Object> context, P2GroupRepository repository )
        throws StorageException, ItemNotFoundException
    {
        ArrayList<Xpp3Dom> doms =
            parseItemContent( P2Constants.ARTIFACTS_XML, P2Constants.ARTIFACTS_JAR, context, repository );

        ArrayList<Artifacts> artifacts = new ArrayList<Artifacts>();
        for ( Xpp3Dom dom : doms )
        {
            artifacts.add( new Artifacts( dom ) );
        }

        ArtifactsMerge m = new ArtifactsMerge();

        try
        {
            Artifacts metadata = m.mergeArtifactsMetadata( getName( repository ), artifacts );

            LinkedHashMap<String, String> properties = metadata.getProperties();
//            properties.put( P2Facade.PROP_REPOSITORY_ID, repository.getId() );
            metadata.setProperties( properties );

            return metadata.getDom();
        }
        catch ( P2MetadataMergeException e )
        {
            throw new StorageException( e );
        }

    }

    @Override
    protected Xpp3Dom doRetrieveContentDom( Map<String, Object> context, P2GroupRepository repository )
        throws StorageException, ItemNotFoundException
    {
        ArrayList<Xpp3Dom> doms =
            parseItemContent( P2Constants.CONTENT_XML, P2Constants.CONTENT_JAR, context, repository );

        ArrayList<Content> artifacts = new ArrayList<Content>();
        for ( Xpp3Dom dom : doms )
        {
            artifacts.add( new Content( dom ) );
        }

        ArtifactsMerge m = new ArtifactsMerge();

        try
        {
            Content metadata = m.mergeContentMetadata( getName( repository ), artifacts );

            LinkedHashMap<String, String> properties = metadata.getProperties();
//            properties.put( P2Facade.PROP_REPOSITORY_ID, repository.getId() );
            metadata.setProperties( properties );

            return metadata.getDom();
        }
        catch ( P2MetadataMergeException e )
        {
            throw new StorageException( e );
        }
    }

    private ArrayList<Xpp3Dom> parseItemContent( String xmlName, String jarName, Map<String, Object> context,
                                                 P2GroupRepository repository )
        throws StorageException, ItemNotFoundException
    {
        ArrayList<Xpp3Dom> doms = new ArrayList<Xpp3Dom>();

        for ( Repository repo : getMemberRepositories( repository ) )
        {
            doms.add( parseItem( repo, jarName, xmlName, context ) );
        }

        return doms;
    }

    @Override
    protected StorageItem doRetrieveRemoteItem( Repository repo, String path, Map<String, Object> context )
        throws ItemNotFoundException, StorageException
    {
        ResourceStoreRequest req = new ResourceStoreRequest( path );
        req.getRequestContext().putAll( context );

        try
        {
            return repo.retrieveItem( req );
        }
        catch ( IllegalOperationException e )
        {
            throw new StorageException( e );
        }
        catch ( AccessDeniedException e )
        {
            throw new ItemNotFoundException( req, repo );
        }
    }

    @Override
    protected boolean isArtifactsOld( AbstractStorageItem artifactsItem, P2GroupRepository repository )
        throws StorageException
    {
        Map<String, Object> context = new HashMap<String, Object>();

        return isOld( artifactsItem, P2Constants.ARTIFACTS_JAR, P2Constants.ARTIFACTS_XML, context, repository );
    }

    @Override
    protected boolean isContentOld( AbstractStorageItem contentItem, P2GroupRepository repository )
        throws StorageException
    {
        Map<String, Object> context = new HashMap<String, Object>();

        return isOld( contentItem, P2Constants.CONTENT_JAR, P2Constants.CONTENT_XML, context, repository );
    }

    @Override
    protected void setItemAttributes( StorageFileItem item, Map<String, Object> context, P2GroupRepository repository )
        throws StorageException
    {
        if ( P2Constants.ARTIFACTS_JAR.equals( item.getPath() ) || P2Constants.ARTIFACTS_XML.equals( item.getPath() ) )
        {
            item.getAttributes().putAll(
                                         getMemberHash( P2Constants.ARTIFACTS_JAR, P2Constants.ARTIFACTS_XML, context,
                                                        repository ) );
        }
        else if ( P2Constants.CONTENT_JAR.equals( item.getPath() ) || P2Constants.CONTENT_XML.equals( item.getPath() ) )
        {
            item.getAttributes().putAll(
                                         getMemberHash( P2Constants.CONTENT_JAR, P2Constants.CONTENT_XML, context,
                                                        repository ) );
        }
    }

    private boolean isOld( AbstractStorageItem artifactsItem, String jar, String xml, Map<String, Object> context,
                           P2GroupRepository repository )
        throws StorageException
    {
        TreeMap<String, String> memberHash = getMemberHash( jar, xml, context, repository );

        LinkedHashMap<String, String> hash = new LinkedHashMap<String, String>();

        Map<String, String> attributes = artifactsItem.getAttributes();
        for ( Map.Entry<String, String> entry : attributes.entrySet() )
        {
            if ( entry.getKey().startsWith( ATTR_HASH_PREFIX ) )
            {
                hash.put( entry.getKey(), entry.getValue() );
            }
        }

        return !hash.equals( memberHash );
    }

    private TreeMap<String, String> getMemberHash( String jar, String xml, Map<String, Object> context,
                                                   P2GroupRepository repository )
    {
        TreeMap<String, String> memberHash = new TreeMap<String, String>();

        int count = 0;

        try
        {
            for ( StorageItem original : retrieveItems( jar, xml, context, repository ) )
            {
                String hash = original.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
                if ( hash == null )
                {
                    // assume it has changed
                    return null;
                }

                memberHash.put( ATTR_HASH_PREFIX + count + "." + original.getRepositoryItemUid().toString(), hash );

                count++;
            }
        }
        catch ( StorageException e )
        {
            // assume it has changed
            return null;
        }

        return memberHash;
    }

    private List<StorageItem> retrieveItems( String jar, String xml, Map<String, Object> context,
                                             P2GroupRepository repository )
        throws StorageException
    {
        ArrayList<StorageItem> items = new ArrayList<StorageItem>();
        for ( Repository repo : getMemberRepositories( repository ) )
        {
            try
            {
                items.add( doRetrieveRemoteItem( repo, jar, context ) );
            }
            catch ( ItemNotFoundException e )
            {
                try
                {
                    items.add( doRetrieveRemoteItem( repo, xml, context ) );
                }
                catch ( ItemNotFoundException e1 )
                {
                    throw new StorageException( "Could not retrieve neither " + jar + " nor " + xml + " from "
                        + repo.getId() );
                }
            }
        }
        return items;
    }
}
