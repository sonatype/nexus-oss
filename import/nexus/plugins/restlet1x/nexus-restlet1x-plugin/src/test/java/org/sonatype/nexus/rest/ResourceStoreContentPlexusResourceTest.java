/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.restlet.Context;
import org.restlet.data.Conditions;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.item.uid.IsRemotelyAccessibleAttribute;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link AbstractResourceStoreContentPlexusResource}
 */
public class ResourceStoreContentPlexusResourceTest
    extends TestSupport
{

    private Map<String, ArtifactViewProvider> views = Maps.newHashMap();

    private AbstractResourceStoreContentPlexusResource underTest;

    @Mock
    private SecuritySystem security;

    @Mock
    private Nexus nexus;

    @Mock
    private Context context;

    @Mock
    private Request request;

    @Mock
    private Variant variant;

    @Mock
    private ResourceStore resourceStore;

    @Mock
    private StorageCollectionItem collectionItem;

    @Mock
    private StorageFileItem fileItem;

    @Mock
    private StorageLinkItem linkItem;

    @Mock
    private HttpResponse response;

    @Mock
    private HttpServerCall httpCall;

    @Mock
    private Series headers;

    @Mock
    private Reference reference;

    @Mock
    private Attributes attributes;

    @Mock
    private RepositoryItemUid itemUid;

    @Before
    public void setup()
    {

        underTest = new AbstractResourceStoreContentPlexusResource( security, views )
        {
            @Override
            protected ResourceStore getResourceStore( final Request request )
                throws NoSuchResourceStoreException, ResourceException
            {
                return null;
            }

            @Override
            public String getResourceUri()
            {
                return null;
            }

            @Override
            public PathProtectionDescriptor getResourceProtection()
            {
                return null;
            }

            @Override
            public Object getPayloadInstance()
            {
                return null;
            }

            @Override
            protected Logger getLogger()
            {
                return LoggerFactory.getLogger( ResourceStoreContentPlexusResourceTest.class );
            }

            @Override
            protected Nexus getNexus()
            {
                return nexus;
            }
        };

        when( request.getResourceRef() ).thenReturn( reference );
        when( reference.toString() ).thenReturn( "" );
        when( reference.getQueryAsForm() ).thenReturn( new Form() );

        when( request.getConditions() ).thenReturn( new Conditions() );

        when( response.getHttpCall() ).thenReturn( httpCall );
        when( httpCall.getResponseHeaders() ).thenReturn( headers );

        when( itemUid.getBooleanAttributeValue( IsRemotelyAccessibleAttribute.class ) ).thenReturn( true );

        when( collectionItem.getRepositoryItemUid() ).thenReturn( itemUid );
        when( fileItem.getRepositoryItemUid() ).thenReturn( itemUid );

        when( fileItem.getRepositoryItemAttributes() ).thenReturn( attributes );
        when( fileItem.getItemContext() ).thenReturn( new RequestContext() );

    }

    @Test
    public void testNexus5155CacheHeadersForCollectionItems()
        throws ResourceException, NoSuchResourceStoreException, IOException, IllegalOperationException,
        ItemNotFoundException, AccessDeniedException
    {
        underTest.renderStorageCollectionItem( context, request, response, variant, resourceStore, collectionItem );

        verify( headers ).add( "Pragma", "no-cache" );
        verify( headers ).add( "Cache-Control", "no-cache, no-store, max-age=0, must-revalidate" );
    }

    @Test
    public void testNexus5155CacheHeadersForLinkedCollectionItems()
        throws Exception
    {
        when( nexus.dereferenceLinkItem( linkItem ) ).thenReturn( collectionItem );

        underTest.renderStorageLinkItem( context, request, response, variant, resourceStore, linkItem );

        verify( headers ).add( "Pragma", "no-cache" );
        verify( headers ).add( "Cache-Control", "no-cache, no-store, max-age=0, must-revalidate" );
    }

    @Test
    public void testNexus5155OmitCacheHeadersForFileItems()
        throws Exception
    {
        underTest.renderStorageFileItem( request, fileItem );

        verifyZeroInteractions( headers );
    }

    @Test
    public void testNexus5155OmitCacheHeadersForLinkedFileItems()
        throws Exception
    {
        when( nexus.dereferenceLinkItem( linkItem ) ).thenReturn( fileItem );

        underTest.renderStorageLinkItem( context, request, response, variant, resourceStore, linkItem );

        verifyZeroInteractions( headers );
    }

}
