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
package org.sonatype.nexus.proxy.attributes.perf.internal;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeManager;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * A Mock Repository, using Mockito answers + JUnitBenchmark causes some problems
 */
public class TMockRepository
    extends AbstractRepository
    implements Repository
{
    private String id;

    private String localUrl;

    private RepositoryItemUidFactory repositoryItemUidFactory;

    private AttributesHandler attributesHandler;

    public TMockRepository( String id, RepositoryItemUidFactory repositoryItemUidFactory )
    {
        this.id = id;
        this.repositoryItemUidFactory = repositoryItemUidFactory;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId( String id )
    {
        this.id = id;
    }

    @Override
    public RepositoryItemUid createUid( String path )
    {
        return new TestRepositoryItemUid( repositoryItemUidFactory, this, path );
    }

    @Override
    public String getLocalUrl()
    {
        return localUrl;
    }

    @Override
    public void setLocalUrl( String url )
        throws StorageException
    {
        this.localUrl = url;
    }

    @Override
    public AttributesHandler getAttributesHandler()
    {
        return attributesHandler;
    }

    @Override
    public void setAttributesHandler( AttributesHandler attributesHandler )
    {
        this.attributesHandler = attributesHandler;
    }

    // dummying those mising

    @Override
    public RepositoryKind getRepositoryKind()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContentClass getRepositoryContentClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Configurator getConfigurator()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
