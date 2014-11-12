/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.services.internal.storage;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentEnvelope;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.services.adapter.EntityAdapterRegistry;
import org.sonatype.nexus.component.services.id.EntityIdFactory;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;
import org.sonatype.nexus.component.services.storage.ComponentStore;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link ComponentStore} implementation.
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class ComponentStoreImpl
    extends ComponentStoreImplSupport
    implements ComponentStore
{
  @Inject
  public ComponentStoreImpl(@Named(ComponentMetadataDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider,
                            @Named(ComponentBlobStoreProvider.NAME) BlobStore blobStore,
                            EntityAdapterRegistry entityAdapterRegistry,
                            EntityIdFactory entityIdFactory) {
    super(databaseInstanceProvider, blobStore, entityAdapterRegistry, entityIdFactory);
  }

  @Override
  public Set<Class<? extends Asset>> assetClasses() {
    return entityAdapterRegistry.assetClasses();
  }

  @Override
  public Set<Class<? extends Component>> componentClasses() {
    return entityAdapterRegistry.componentClasses();
  }

  @Override
  public <A extends Asset> long countAssets(final Class<A> assetClass,
                                            @Nullable final MetadataQueryRestriction restriction) {
    return count(assetClass, restriction);
  }

  @Override
  public <C extends Component> long countComponents(final Class<C> componentClass,
                                                    @Nullable final MetadataQueryRestriction restriction) {
    return count(componentClass, restriction);
  }

  @Override
  public <A extends Asset> List<A> findAssets(final Class<A> assetClass,
                                              @Nullable final MetadataQuery metadataQuery) {
    return execute(new Function<ODatabaseDocumentTx, List<A>>()
    {
      @Override
      public List<A> apply(final ODatabaseDocumentTx db) {
        return findAssets(db, assetClass, metadataQuery);
      }
    });
  }

  @Override
  public <C extends Component> List<C> findComponents(final Class<C> componentClass,
                                                      @Nullable final MetadataQuery metadataQuery) {
    return execute(new Function<ODatabaseDocumentTx, List<C>>()
    {
      @Override
      public List<C> apply(final ODatabaseDocumentTx db) {
        return findComponents(db, componentClass, metadataQuery);
      }
    });
  }

  @Override
  public <C extends Component> C createComponent(final C sourceComponent) {
    return execute(new Function<ODatabaseDocumentTx, C>()
    {
      @Override
      public C apply(final ODatabaseDocumentTx db) {
        return createComponent(db, sourceComponent);
      }
    });
  }

  @Override
  public <A extends Asset> A createAsset(final EntityId componentId,
                                         final A sourceAsset) {
    final BlobTx blobTx = beginBlobTx();
    return executeInTransaction(new Function<ODatabaseDocumentTx, A>()
    {
      @Override
      public A apply(final ODatabaseDocumentTx db) {
        return createAsset(db, componentId, sourceAsset, blobTx);
      }
    }, blobTx);
  }

  @Override
  public <C extends Component, A extends Asset> C createComponentWithAssets(final ComponentEnvelope<C, A> sourceEnvelope) {
    final BlobTx blobTx = beginBlobTx();
    return executeInTransaction(new Function<ODatabaseDocumentTx, C>()
    {
      @Override
      public C apply(final ODatabaseDocumentTx db) {
        return createComponentWithAssets(db, sourceEnvelope, blobTx);
      }
    }, blobTx);
  }

  @Override
  public <C extends Component, A extends Asset> boolean createComponentsWithAssets(
      final Iterable<ComponentEnvelope<C, A>> sourceEnvelopes,
      @Nullable final Predicate<List<EntityId>> progressListener,
      final int commitFrequency)
  {
    checkNotNull(sourceEnvelopes);
    checkArgument(commitFrequency >= 0);
    final Iterator<ComponentEnvelope<C, A>> iterator = sourceEnvelopes.iterator();

    boolean stopRequested = false;
    while (iterator.hasNext() && !stopRequested) {
      final BlobTx blobTx = beginBlobTx();
      stopRequested = !executeInTransaction(new Function<ODatabaseDocumentTx, Boolean>()
      {
        @Override
        public Boolean apply(final ODatabaseDocumentTx db) {
          // create all components with assets for this transaction
          List<EntityId> componentIds = Lists.newArrayList();
          while (iterator.hasNext() && (commitFrequency == 0 || componentIds.size() < commitFrequency))
          {
            ComponentEnvelope<C, A> sourceEnvelope = iterator.next();
            C newComponent = createComponentWithAssets(db, sourceEnvelope, blobTx);
            componentIds.add(newComponent.getId());
          }
          // pass the ids to the listener and return the result, which is false if stop is requested
          return progressListener == null || progressListener.apply(componentIds);
        }
      }, blobTx);
    }

    return !iterator.hasNext();
  }

  @Override
  public <A extends Asset> A readAsset(final Class<A> assetClass, final EntityId assetId) {
    return execute(new Function<ODatabaseDocumentTx, A>()
    {
      @Override
      public A apply(final ODatabaseDocumentTx db) {
        return readAsset(db, assetClass, assetId);
      }
    });
  }

  @Override
  public <C extends Component> C readComponent(final Class<C> componentClass, final EntityId componentId) {
    return execute(new Function<ODatabaseDocumentTx, C>()
    {
      @Override
      public C apply(final ODatabaseDocumentTx db) {
        return readComponent(db, componentClass, componentId);
      }
    });
  }

  @Override
  public <C extends Component, A extends Asset> ComponentEnvelope<C, A> readComponentWithAssets(
      final Class<C> componentClass, final Class<A> assetClass, final EntityId componentId)
  {
    return execute(new Function<ODatabaseDocumentTx, ComponentEnvelope<C, A>>()
    {
      @Override
      public ComponentEnvelope<C, A> apply(final ODatabaseDocumentTx db) {
        return readComponentsWithAssets(db, componentClass, assetClass, componentId);
      }
    });
  }

  @Override
  public <A extends Asset> A updateAsset(final EntityId assetId, final A sourceAsset) {
    final BlobTx blobTx = beginBlobTx();
    return execute(new Function<ODatabaseDocumentTx, A>()
    {
      @Override
      public A apply(final ODatabaseDocumentTx db) {
        return updateAsset(db, assetId, sourceAsset, blobTx);
      }
    }, blobTx);
  }

  @Override
  public <C extends Component> C updateComponent(final EntityId componentId, final C sourceComponent) {
    return execute(new Function<ODatabaseDocumentTx, C>()
    {
      @Override
      public C apply(final ODatabaseDocumentTx db) {
        return updateComponent(db, componentId, sourceComponent);
      }
    });
  }

  @Override
  public <A extends Asset> boolean deleteAsset(final Class<A> assetClass, final EntityId assetId) {
    final BlobTx blobTx = beginBlobTx();
    return executeInTransaction(new Function<ODatabaseDocumentTx, Boolean>()
    {
      @Override
      public Boolean apply(final ODatabaseDocumentTx db) {
        return deleteAsset(db, assetClass, assetId, blobTx);
      }
    }, blobTx);
  }

  @Override
  public <C extends Component> boolean deleteComponent(final Class<C> componentClass, final EntityId componentId) {
    final BlobTx blobTx = beginBlobTx();
    return executeInTransaction(new Function<ODatabaseDocumentTx, Boolean>()
    {
      @Override
      public Boolean apply(final ODatabaseDocumentTx db) {
        return deleteComponent(db, componentClass, componentId, blobTx);
      }
    }, blobTx);
  }
}
