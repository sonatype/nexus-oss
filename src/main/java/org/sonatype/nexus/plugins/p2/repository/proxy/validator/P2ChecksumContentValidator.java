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
package org.sonatype.nexus.plugins.p2.repository.proxy.validator;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.mappings.ArtifactMapping;
import org.sonatype.nexus.plugins.p2.repository.mappings.ArtifactPath;
import org.sonatype.nexus.plugins.p2.repository.proxy.P2ProxyMetadataSource;
import org.sonatype.nexus.plugins.p2.repository.proxy.P2ProxyRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.maven.AbstractChecksumContentValidator;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RemoteHashResponse;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * P2 checksum content validator.
 * 
 * @author velo
 */
@Component( role = ItemContentValidator.class, hint = "P2ChecksumContentValidator" )
public class P2ChecksumContentValidator
    extends AbstractChecksumContentValidator
    implements ItemContentValidator
{

    @Override
    protected ChecksumPolicy getChecksumPolicy( final ProxyRepository proxy, final AbstractStorageItem item )
        throws LocalStorageException
    {
        if ( P2ProxyMetadataSource.isP2MetadataItem( item.getRepositoryItemUid().getPath() ) )
        {
            // the checksum is on metadata files
            return ChecksumPolicy.IGNORE;
        }

        if ( !proxy.getRepositoryKind().isFacetAvailable( P2ProxyRepository.class ) )
        {
            return ChecksumPolicy.IGNORE;
        }

        final P2ProxyRepository p2repo = proxy.adaptToFacet( P2ProxyRepository.class );

        final ChecksumPolicy checksumPolicy = p2repo.getChecksumPolicy();

        if ( checksumPolicy == null || !checksumPolicy.shouldCheckChecksum()
            || !( item instanceof DefaultStorageFileItem ) )
        {
            // there is either no need to validate or we can't validate the item content
            return ChecksumPolicy.IGNORE;
        }

        final ResourceStoreRequest req = new ResourceStoreRequest( P2Constants.ARTIFACT_MAPPINGS_XML );
        req.setRequestLocalOnly( true );
        try
        {
            p2repo.retrieveItem( true, req );
        }
        catch ( final Exception e )
        {
            // no way to calculate
            getLogger().debug( "Unable to find artifact-mapping.xml", e );
            return ChecksumPolicy.IGNORE;
        }

        return checksumPolicy;
    }

    @Override
    protected void cleanup( final ProxyRepository proxy, final RemoteHashResponse remoteHash, final boolean contentValid )
        throws LocalStorageException
    {
        // no know cleanup for p2 repos
    }

    @Override
    protected RemoteHashResponse retrieveRemoteHash( final AbstractStorageItem item, final ProxyRepository proxy,
                                                     final String baseUrl )
        throws LocalStorageException
    {
        final P2ProxyRepository p2repo = proxy.adaptToFacet( P2ProxyRepository.class );

        Map<String, ArtifactPath> paths;
        try
        {
            final ArtifactMapping artifactMapping = p2repo.getArtifactMappings().get( baseUrl );
            if ( artifactMapping == null )
            {
                getLogger().debug( "Unable to retrive remote has for " + item.getPath() );
                return null;
            }
            paths = artifactMapping.getArtifactsPath();
        }
        catch ( StorageException e )
        {
            throw new LocalStorageException( e );
        }
        catch ( final IllegalOperationException e )
        {
            getLogger().error( "Unable to open artifactsMapping.xml", e );
            return null;
        }
        final String md5 = paths.get( item.getPath() ).getMd5();
        if ( md5 == null )
        {
            return null;
        }
        return new RemoteHashResponse( DigestCalculatingInspector.DIGEST_MD5_KEY, md5, null );
    }

}
