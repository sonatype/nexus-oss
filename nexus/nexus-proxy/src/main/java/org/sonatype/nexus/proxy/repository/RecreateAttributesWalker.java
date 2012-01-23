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

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class RecreateAttributesWalker
    extends AbstractFileWalkerProcessor
{
    private final Repository repository;

    private final Map<String, String> initialData;

    public RecreateAttributesWalker( Repository repository, Map<String, String> initialData )
    {
        this.repository = repository;

        this.initialData = initialData;
    }

    @Override
    protected void processFileItem( WalkerContext ctx, StorageFileItem item )
        throws IOException
    {
        if ( getInitialData() != null )
        {
            item.getRepositoryItemAttributes().putAll( initialData );
        }

        getRepository().getAttributesHandler().storeAttributes( item, item.getContentLocator() );
    }

    public Repository getRepository()
    {
        return repository;
    }

    public Map<String, String> getInitialData()
    {
        return initialData;
    }

}
