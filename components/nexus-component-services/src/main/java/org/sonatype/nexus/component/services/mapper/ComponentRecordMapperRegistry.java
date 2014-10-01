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
package org.sonatype.nexus.component.services.mapper;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Component;

/**
 * A registry of {@link ComponentRecordMapper}s, keyed by {@link Component} (domain) class.
 *
 * @since 3.0
 */
public interface ComponentRecordMapperRegistry
{
  /**
   * Registers a mapper.
   *
   * @throws IllegalStateException if a mapper already exists in the registry with the same component class.
   */
  <T extends Component> void registerMapper(ComponentRecordMapper<T> mapper);

  /**
   * Gets the registered mapper for the given component class, or {@code null} if it doesn't exist.
   */
  @Nullable
  <T extends Component> ComponentRecordMapper<T> getMapper(Class<T> componentClass);
}
