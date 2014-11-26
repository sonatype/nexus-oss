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
package org.sonatype.nexus.component.services.storage;

import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.model.Envelope;
import org.sonatype.nexus.component.services.adapter.AssetAdapter;
import org.sonatype.nexus.component.services.adapter.ComponentAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;

import com.google.common.base.Predicate;
import org.eclipse.sisu.Mediator;

/**
 * Provides CRUD and query operations on components and assets.
 * <p>
 * <em>Note:</em> Many methods of this interface accept a <code>className</code> argument which denotes the storage
 * class in which to find the entity or entities in question. For best performance, this should be specified as the
 * most specific known storage class. If unknown, or the query should span multiple types, one of the base class
 * names, {@link AssetAdapter#CLASS_NAME}, {@link ComponentAdapter#CLASS_NAME}, or {@link EntityAdapter#CLASS_NAME},
 * may be used.
 *
 * @since 3.0
 */
public interface ComponentStore
{
  /**
   * Ensures that the store is prepared to handle the given types of entities.
   * <p>
   * All {@link EntityAdapter}s supporting the given classes must be registered by the time this is called.
   * Usally this is done automatically using a {@link Mediator}). If the underlying storage classes have not been
   * created yet, this call will ensure that they are.
   */
  void prepareStorage(String... classNames);

  /**
   * Gets the number of components or assets of the given class matching the given restriction.
   *
   * @param className the storage class in which to run the query, or {@code null} to search all storage classes.
   * @param restriction the query restriction, or {@code null} to count all entities in the given storage class.
   */
  long count(@Nullable String className, @Nullable MetadataQueryRestriction restriction);

  /**
   * Gets the list of components matching the given query.
   * <p>
   * <em>Note:</em> For large result sets, the query should specify a {@link MetadataQuery#limit()}, and use
   * {@link MetadataQuery#skip()} or {@link MetadataQuery#skipEntityId()}-based paging, if needed.
   *
   * @param className the storage class in which to run the query, or {@code null} to search all component classes.
   * @param metadataQuery the query, or {@code null} to return all entities in the given storage class.
   */
  List<Component> findComponents(@Nullable String className, @Nullable MetadataQuery metadataQuery);

  /**
   * Gets the list of assets matching the given query.
   * <p>
   * <em>Note:</em> For large result sets, the query should specify a {@link MetadataQuery#limit()}, and use
   * {@link MetadataQuery#skip()} or {@link MetadataQuery#skipEntityId()}-based paging, if needed.
   *
   * @param className the storage class in which to run the query, or {@code null} to search all asset classes.
   * @param metadataQuery the query, or {@code null} to return all entities in the given storage class.
   */
  List<Asset> findAssets(@Nullable String className, @Nullable MetadataQuery metadataQuery);

  /**
   * Stores a new component with no assets.
   *
   * @param sourceComponent the component upon which the new one should be based. Its {@code P_ID} and
   *        {@code P_ASSETS}, if present, will be ignored.
   * @return the new component with a system-generated {@code P_ID}, an empty set of {@code P_ASSETS}, and all other
   *         properties matching those of the source.
   * @see EntityAdapter#P_ID
   * @see ComponentAdapter#P_ASSETS
   */
  Component createComponent(Component sourceComponent);

  /**
   * Stores a new asset associated with an existing stored component.
   *
   * @param componentId identifies the component the asset belongs to.
   * @param sourceAsset the asset upon which the new one should be based. Its {@code P_ID}, {@code P_COMPONENT},
   *        {@code P_FIRST_CREATED}, {@code P_LAST_MODIFIED}, {@code P_CONTENT_LENGTH}, and {@code P_BLOB_REFS} values,
   *        if present, will be ignored, and its {@link Asset#openStream()} method must not return {@code null}.
   * @return the new asset with a system-generated {@code P_ID}, a {@code P_COMPONENT} matching the first argument,
   *         a {@code P_FIRST_CREATED} and {@code P_LAST_MODIFIED} date representing the time of the operation, a
   *         {@code P_CONTENT_LENGTH} value matching the number of bytes in the source asset stream, a
   *         {@code P_BLOB_REFS} value with at least one entry, and all other properties matching those of the source.
   * @see EntityAdapter#P_ID
   * @see AssetAdapter#P_COMPONENT
   * @see AssetAdapter#P_FIRST_CREATED
   * @see AssetAdapter#P_LAST_MODIFIED
   * @see AssetAdapter#P_CONTENT_LENGTH
   * @see AssetAdapter#P_BLOB_REFS
   */
  Asset createAsset(EntityId componentId, Asset sourceAsset);

  /**
   * Stores a new component and a set of associated assets as an atomic operation.
   *
   * @param sourceEnvelope contains a source component and a series of assets to base the new ones on. These are
   *        treated exactly as if a call to {@link #createComponent(Component)} was made, followed by a series of
   *        calls to {@link #createAsset(EntityId, Asset)}.
   * @return the new component and assets.
   */
  Envelope createComponentWithAssets(Envelope sourceEnvelope);

  /**
   * Batch version of {@link #createComponentWithAssets(Envelope)}.
   *
   * @param sourceEnvelopes contains the source components and assets to base the new ones on.
   * @param progressListener if specified, this predicate will be called after each successful commit in the batch,
   *        and will recieve the ids of all newly-committed components. If it returns {@code true}, batch processing
   *        will continue normally. If it returns {@code false}, processing will stop, and if there are more envelopes
   *        in the sequence, this method will return {@code false} to signify to the caller that processing ended early.
   * @param commitFrequency the maximum number of envelopes to include in each commit operation. This must be a
   *        positive number, or zero to signify that all components should be committed in a single transaction.
   * @return {@code true} if processing completed normally, or {@code false} if processing stopped before all requested
   *         components could be created.
   */
  boolean createComponentsWithAssets(Iterable<Envelope> sourceEnvelopes,
                                     @Nullable Predicate<List<EntityId>> progressListener,
                                     int commitFrequency);

  /**
   * Retrieves a stored component.
   *
   * @param className the storage class in which to find the entity, or {@code null} to check all component classes.
   * @param componentId the id of the component.
   */
  Component readComponent(@Nullable String className, EntityId componentId);

  /**
   * Retrieves a stored asset.
   *
   * @param className the storage class in which to find the entity, or {@code null} to check all asset classes.
   * @param assetId the id of the asset.
   */
  Asset readAsset(@Nullable String className, EntityId assetId);

  /**
   * Retrieves a stored component and its assets in a single operation.
   *
   * @param componentClassName the storage class in which to find the entity, or {@code null} to check all component
   *        storage classes.
   * @param componentId the id of the component.
   */
  Envelope readComponentWithAssets(@Nullable String componentClassName, EntityId componentId);

  /**
   * Modifies a stored component.
   *
   * @param componentId denotes the component to modify.
   * @param sourceComponent the component representing the new desired state. System-controlled properties will be
   *        ignored and all other values will be set as given.
   * @return the updated component. System-controlled properties will not change.
   */
  Component updateComponent(EntityId componentId, Component sourceComponent);

  /**
   * Modifies a stored asset.
   *
   * @param assetId denotes the entity to modify.
   * @param sourceAsset the asset representing the new desired state. System-controlled properties will be ignored
   *        and all other values will be set as given. The content will only be modified if
   *        {@link Asset#openStream()} returns a non-{@code null} value.
   * @return the updated asset. System-controlled properties will not change unless unless the content was modified.
   *         In that case, the {@link AssetAdapter#P_LAST_MODIFIED} date will now reflect the time of the operation
   *         and the {@link AssetAdapter#P_CONTENT_LENGTH} and {@link AssetAdapter#P_BLOB_REFS} will be updated
   *         accordingly.
   */
  Asset updateAsset(EntityId assetId, Asset sourceAsset);

  /**
   * Deletes a stored component or asset.
   * <p>
   * If the entity is a component, its metadata and all associated asset metadata and content will be deleted.
   * If the entity is an asset, its metadata and content will be deleted, and it will be removed from the component's
   * {@link ComponentAdapter#P_ASSETS}.
   *
   * @param className the storage class from which to delete the entity, or {@code null} if all storage classes should
   *        be checked.
   * @return {@code true} if it previously existed.
   */
  boolean delete(@Nullable String className, EntityId entityId);
}
