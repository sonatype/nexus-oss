/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import java.io.IOException;
import java.util.Map;

import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.attributes.TransitioningAttributeStorage;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class RecreateAttributesWalker
    extends AbstractFileWalkerProcessor
{
    public static final String FORCE_ATTRIBUTE_RECREATION = RecreateAttributesWalker.class.getName()
        + ".forceAttributeRecreation";

    public static final String LEGACY_ATTRIBUTES_ONLY = RecreateAttributesWalker.class.getName()
        + ".forceAttributeRecreation";

    private final Repository repository;

    private final Map<String, String> initialData;

    private boolean forceAttributeRecreation;

    private boolean legacyAtributesOnly;

    public RecreateAttributesWalker( final Repository repository, final Map<String, String> initialData )
    {
        this.repository = repository;
        this.initialData = initialData;
    }

    @Override
    public void beforeWalk( WalkerContext context )
        throws Exception
    {
        forceAttributeRecreation = isForceAttributeRecreation( context );
        legacyAtributesOnly = isLegacyAttributesOnly( context );
    }

    @Override
    protected void processFileItem( final WalkerContext ctx, final StorageFileItem item )
        throws IOException
    {
        if ( legacyAtributesOnly )
        {
            if ( !item.getRepositoryItemAttributes().containsKey( TransitioningAttributeStorage.FALLBACK_MARKER_KEY ) )
            {
                // if legacyAtributesOnly and current item attributes does not carry the marker, throw it away
                return;
            }
        }

        if ( getInitialData() != null )
        {
            item.getRepositoryItemAttributes().putAll( initialData );
        }

        if ( forceAttributeRecreation )
        {
            getRepository().getAttributesHandler().storeAttributes( item, item.getContentLocator() );
        }
        else
        {
            getRepository().getAttributesHandler().storeAttributes( item, null );
        }
    }

    public Repository getRepository()
    {
        return repository;
    }

    public Map<String, String> getInitialData()
    {
        return initialData;
    }

    protected boolean isForceAttributeRecreation( final WalkerContext ctx )
    {
        final RequestContext reqestContext = ctx.getResourceStoreRequest().getRequestContext();
        if ( reqestContext.containsKey( FORCE_ATTRIBUTE_RECREATION, false ) )
        {
            // obey the "hint"
            return Boolean.parseBoolean( String.valueOf( reqestContext.get( FORCE_ATTRIBUTE_RECREATION, false ) ) );
        }
        else
        {
            // fallback to default behavior: do force it
            return true;
        }
    }

    protected boolean isLegacyAttributesOnly( final WalkerContext ctx )
    {
        final RequestContext reqestContext = ctx.getResourceStoreRequest().getRequestContext();
        if ( reqestContext.containsKey( LEGACY_ATTRIBUTES_ONLY, false ) )
        {
            // obey the "hint"
            return Boolean.parseBoolean( String.valueOf( reqestContext.get( LEGACY_ATTRIBUTES_ONLY, false ) ) );
        }
        else
        {
            // fallback to default behavior: all of them
            return false;
        }
    }
}
