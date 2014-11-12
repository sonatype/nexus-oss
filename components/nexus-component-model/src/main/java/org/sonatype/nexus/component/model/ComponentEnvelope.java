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
package org.sonatype.nexus.component.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * A handle on a {@link Component} and its constituent {@link Asset}s, which are not necessarily stored,
 * and therefore may have {@code null} ids.
 *
 * @since 3.0
 */
public class ComponentEnvelope<C extends Component, A extends Asset>
{
  private final C component;

  private final Iterable<A> assets;

  public ComponentEnvelope(final C component, final Iterable<A> assets) {
    this.component = checkNotNull(component);
    this.assets = checkNotNull(assets);
  }

  public C getComponent() {
    return component;
  }

  public Iterable<A> getAssets() {
    return assets;
  }

  public static <C extends Component, A extends Asset> ComponentEnvelope<C, A> simpleEnvelope(C component, A asset) {
    return new ComponentEnvelope<C, A>(component, asList(asset));
  }
}
