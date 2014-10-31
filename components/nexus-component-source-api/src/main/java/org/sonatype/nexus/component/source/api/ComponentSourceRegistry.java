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
package org.sonatype.nexus.component.source.api;

import javax.annotation.Nullable;
import javax.inject.Provider;

/**
 * A registry of the currently available component sources.
 *
 * @since 3.0
 */
public interface ComponentSourceRegistry
{
  /**
   * Return a {@link ComponentSource} for the given name, or {@code null} if none is registered. References to sources
   * should not be retained, as instances may be disposed of (and disabled) if the source configuration changes.
   */
  @Nullable
  <T extends ComponentSource> T getSource(String name);

  /**
   * Return a {@link ComponentSource} for the given name, or {@code null} if none is registered. References to sources
   * should not be retained, as instances may be disposed of (and disabled) if the source configuration changes.
   */
  @Nullable
  <T extends ComponentSource> T getSource(ComponentSourceId sourceId);

  /**
   * Obtains a reusable provider for a source, to be used each time a source is queried. This allows redefinition so
   * that the source can be reloaded/redefined.
   */
  <T extends ComponentSource> Provider<T> getSourceProvider(ComponentSourceId sourceId);

  /**
   * Obtains a reusable provider for a source, to be used each time a source is queried. This allows redefinition so
   * that the source can be reloaded/redefined.
   */
  <T extends ComponentSource> Provider<T> getSourceProvider(String sourceName);
}
