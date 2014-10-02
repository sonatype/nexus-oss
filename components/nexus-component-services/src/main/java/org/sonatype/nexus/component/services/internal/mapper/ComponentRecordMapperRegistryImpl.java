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
package org.sonatype.nexus.component.services.internal.mapper;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.services.mapper.ComponentRecordMapper;
import org.sonatype.nexus.component.services.mapper.ComponentRecordMapperRegistry;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link ComponentRecordMapper} implementation.
 *
 * @since 3.0
 */
public class ComponentRecordMapperRegistryImpl
    implements ComponentRecordMapperRegistry
{
  private final ConcurrentMap<Class<? extends Component>, ComponentRecordMapper> map = Maps.newConcurrentMap();

  @Override
  public <T extends Component> void registerMapper(final ComponentRecordMapper<T> mapper) {
    checkState(map.putIfAbsent(mapper.getComponentClass(), mapper) == null,
        "A ComponentRecordMapper is already registered for class %s", mapper.getComponentClass());
  }

  @Nullable
  @Override
  @SuppressWarnings({"unchecked"})
  public <T extends Component> ComponentRecordMapper<T> getMapper(final Class<T> componentClass) {
    return map.get(componentClass);
  }
}
