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
package org.sonatype.nexus.component.services.internal.query;

import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.component.assetstore.AssetStore;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.recordstore.RecordStore;
import org.sonatype.nexus.component.services.mapper.ComponentRecordMapper;
import org.sonatype.nexus.component.services.mapper.ComponentRecordMapperRegistry;
import org.sonatype.nexus.component.services.query.MetadataQuery;
import org.sonatype.nexus.component.services.query.MetadataQueryService;
import org.sonatype.nexus.component.services.query.QueryExpression;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link MetadataQueryService} implementation.
 *
 * @since 3.0
 */
public class MetadataQueryServiceImpl
    implements MetadataQueryService
{
  private final RecordStore recordStore;

  private final AssetStore assetStore;

  private final ComponentRecordMapperRegistry mapperRegistry;

  @Inject
  public MetadataQueryServiceImpl(RecordStore recordStore, AssetStore assetStore,
                                  ComponentRecordMapperRegistry mapperRegistry) {
    this.recordStore = checkNotNull(recordStore);
    this.assetStore = checkNotNull(assetStore);
    this.mapperRegistry = checkNotNull(mapperRegistry);
  }

  @Override
  public <T extends Component> long countComponents(final Class<T> componentClass,
                                                    final QueryExpression queryExpression)
  {
    ComponentRecordMapper<T> mapper = getExistingMapper(componentClass);
    return 0;
  }

  @Override
  public long countAssets(final QueryExpression queryExpression) {
    return 0;
  }

  @Override
  public <T extends Component> List<T> findComponents(final Class<T> componentClass,
                                                      final MetadataQuery metadataQuery)
  {
    ComponentRecordMapper<T> mapper = getExistingMapper(componentClass);
    return null;
  }

  @Override
  public List<Asset> findAssets(final MetadataQuery metadataQuery) {
    return null;
  }


  private <T extends Component> ComponentRecordMapper<T> getExistingMapper(final Class<T> componentClass) {
    ComponentRecordMapper<T> mapper = mapperRegistry.getMapper(componentClass);
    checkState(mapper != null, "No mapper registered for class %", componentClass);
    return mapper;
  }
}
