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
package org.sonatype.nexus.repository.raw.internal.negativecache;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.view.Context;

/**
 * A {@link NegativeCacheKeyProvider} facet that considers the path (only) to be the cache key.
 *
 * @since 3.0
 */
public class PathNegativeCacheKeyProvider
    extends FacetSupport
    implements NegativeCacheKeyProvider
{
  @Override
  public NegativeCacheKey cacheKey(final Context context) {
    return new NegativeCacheKey(context.getRequest().getPath());
  }
}
