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
package org.sonatype.nexus.repository.view;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;

import static com.google.common.base.Preconditions.checkState;

/**
 * Support for {@link ViewFacet} implementations.
 *
 * @since 3.0
 */
public abstract class ViewFacetSupport
  extends FacetSupport
  implements ViewFacet
{
  private Router router;

  @Override
  protected void doInit(final Repository repository) throws Exception {
    router = createRouter();
    log.debug("Created router: {}", router);
  }

  protected abstract Router createRouter() throws Exception;

  @Override
  public Router getRouter() {
    checkState(router != null);
    return router;
  }

  @Override
  public Response dispatch(final Request request) throws Exception {
    return getRouter().dispatch(request);
  }
}
