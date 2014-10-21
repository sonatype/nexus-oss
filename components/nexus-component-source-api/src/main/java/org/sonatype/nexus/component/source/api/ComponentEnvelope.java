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

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;

import static java.util.Arrays.asList;

/**
 * A transmitted component and handles for any assets.
 *
 * @since 3.0
 */
public class ComponentEnvelope<T extends Component>
{
  private final T component;

  private final Iterable<Asset> assets;

  public ComponentEnvelope(final T component, final Iterable<Asset> assets) {
    this.component = component;
    this.assets = assets;
  }

  public T getComponent() {
    return component;
  }

  /**
   * The {@link Asset}s returned are handles for remote resources: they should not be persisted directly, but
   * instead have their {@link Asset#openStream() streams} read. {@link Asset#getComponentId} will be {@code null}.
   */
  public Iterable<Asset> getAssets() {
    return assets;
  }

  public static <T extends Component> ComponentEnvelope<T> simpleEnvelope(T component, Asset asset) {
    return new ComponentEnvelope<T>(component, asList(asset));
  }
}
