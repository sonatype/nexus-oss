/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.raw.internal;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.raw.internal.proxy.Locator;
import org.sonatype.nexus.repository.raw.internal.proxy.LocatorFacet;
import org.sonatype.nexus.repository.view.Context;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides 'raw'-specific {@link Locator}s for the standard ProxyFacet.
 *
 * @since 3.0
 */
public class RawLocatorFacet
    extends FacetSupport
    implements LocatorFacet
{
  @Override
  public Locator locator(final Context context) {
    return new RawLocator(context.getRequest().getPath());
  }

  /**
   * A {@link Locator} based on the path.
   */
  public static class RawLocator
      implements Locator
  {
    private final String path;

    public RawLocator(final String path) {
      this.path = checkNotNull(path);
    }

    @Override
    public String describe() {
      return null;
    }

    @Override
    public String uri() {
      return null;
    }

    public String path() {
      return path;
    }
  }
}
