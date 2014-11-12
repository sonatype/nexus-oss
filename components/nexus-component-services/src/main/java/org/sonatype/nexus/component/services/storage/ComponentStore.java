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
import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentEnvelope;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryRestriction;

import com.google.common.base.Predicate;

/**
 * Provides CRUD and query operations on {@link Component}s and {@link Asset}s.
 *
 * @since 3.0
 */
public interface ComponentStore
{
  /**
   * Gets all known asset classes.
   */
  Set<Class<? extends Asset>> assetClasses();

  /**
   * Gets all known component classes.
   */
  Set<Class<? extends Component>> componentClasses();

  /**
   * Gets the number of assets matching the given restriction.
   */
  <A extends Asset> long countAssets(Class<A> assetClass, @Nullable MetadataQueryRestriction restriction);

  /**
   * Gets the number of components matching the given restriction.
   */
  <C extends Component> long countComponents(Class<C> componentClass, @Nullable MetadataQueryRestriction restriction);

  /**
   * Gets the list of assets matching the given query.
   *
   * <em>Note:</em> For large result sets, the query should specify a {@link MetadataQuery#limit()}, and use
   * {@link MetadataQuery#skip()} or {@link MetadataQuery#skipEntityId()}-based paging, if needed.
   */
  <A extends Asset> List<A> findAssets(Class<A> assetClass, @Nullable MetadataQuery metadataQuery);

  /**
   * Gets the list of components matching the given query.
   *
   * <em>Note:</em> For large result sets, the query should specify a {@link MetadataQuery#limit()}, and use
   * {@link MetadataQuery#skip()} or {@link MetadataQuery#skipEntityId()}-based paging, if needed.
   */
  <C extends Component> List<C> findComponents(Class<C> componentClass, @Nullable MetadataQuery metadataQuery);

  /**
   * Stores a new component with no assets.
   *
   * @param sourceComponent the component upon which the new one should be based. Its {@code id} and {@code assetIds},
   *        if present, will be ignored.
   * @return the new component with a system-generated {@code id}, an empty set of {@code assetIds}, and all other
   *         properties matching those of the source.
   */
  <C extends Component> C createComponent(C sourceComponent);

  /**
   * Stores a new asset associated with an existing stored component.
   *
   * @param componentId identifies the component the asset belongs to.
   * @param sourceAsset the asset upon which the new one should be based. Its {@code id}, {@code componentId},
   *        {@code firstCreated}, {@code lastModified}, and {@code contentLength} values, if present, will be ignored,
   *        and its {@link Asset#openStream()} method must not return {@code null}.
   * @return the new asset with a system-generated {@code id}, a {@code componentId} matching the first argument,
   *         a {@code firstCreated} and {@code lastModified} date representing the time of the operation, a
   *         {@code contentLength} value matching the number of bytes in the source asset stream, and all
   *         other properties matching those of the source.
   */
  <A extends Asset> A createAsset(EntityId componentId, A sourceAsset);

  /**
   * Stores a new component and a set of associated assets as an atomic operation.
   *
   * @param sourceEnvelope contains a source component and a series of assets to base the new ones on. These are
   *        treated exactly as if a call to {@link #createComponent(Component)} was made, followed by a series of
   *        calls to {@link #createAsset(EntityId, Asset)}.
   * @return the new component with a system-generated {@code id}, a set of new {@code assetIds}, and all other
   *         properties matching those of the source.
   */
  <C extends Component, A extends Asset> C createComponentWithAssets(ComponentEnvelope<C, A> sourceEnvelope);

  /**
   * Batch version of {@link #createComponentWithAssets(ComponentEnvelope)}.
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
  <C extends Component, A extends Asset> boolean createComponentsWithAssets(
      Iterable<ComponentEnvelope<C, A>> sourceEnvelopes,
      @Nullable Predicate<List<EntityId>> progressListener,
      int commitFrequency);

  /**
   * Retrieves a stored {@code Asset}.
   */
  <A extends Asset> A readAsset(Class<A> assetClass, EntityId assetId);

  /**
   * Retrieves a stored {@code Component}.
   */
  <C extends Component> C readComponent(Class<C> componentClass, EntityId componentId);

  /**
   * Retrieves a stored {@code Component} and its {@code Asset}s in a single operation.
   */
  <C extends Component, A extends Asset> ComponentEnvelope<C, A> readComponentWithAssets(
      Class<C> componentClass,
      Class<A> assetClass,
      EntityId componentId);

  /**
   * Modifies a stored {@code Asset}.
   *
   * @param assetId denotes the asset to modify.
   * @param sourceAsset the asset representing the new desired state. System-generated ids and dates will be ignored,
   *        and all other values will be set as given. The stream will only be modified if {@link Asset#openStream()}
   *        returns a non-{@code null} value.
   * @return the updated asset with correct system-generated properties.
   */
  <A extends Asset> A updateAsset(EntityId assetId, A sourceAsset);

  /**
   * Modifies a stored {@code Component}.
   *
   * @param componentId denotes the component to modify.
   * @param sourceComponent the component representing the new desired state. System-generated ids will be ignored,
   *        and all other values will be set as given.
   * @return the updated component with the correct system-generated ids.
   */
  <C extends Component> C updateComponent(EntityId componentId, C sourceComponent);

  /**
   * Deletes a stored {@code Asset}.
   *
   * The asset's metadata and content will be deleted, and the reference to it will be removed from the component.
   *
   * @param assetId denotes the asset to delete.
   * @return {@code true} if it previously existed.
   */
  <A extends Asset> boolean deleteAsset(Class<A> assetClass, EntityId assetId);

  /**
   * Deletes a stored {@code Component}.
   *
   * The component's metadata and all associated asset metadata and content will be deleted.
   *
   * @param componentId denotes the component to delete.
   * @return {@code true} if it previously existed.
   */
  <C extends Component> boolean deleteComponent(Class<C> componentClass, EntityId componentId);
}
