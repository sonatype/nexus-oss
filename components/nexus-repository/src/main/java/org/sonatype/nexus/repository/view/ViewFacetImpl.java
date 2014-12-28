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

import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link ViewFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class ViewFacetImpl
  extends FacetSupport
  implements ViewFacet
{
  private Router router;

  @Override
  public void install(final Router router) {
    checkNotNull(router);
    checkState(this.router == null, "Router already installed");
    this.router = router;
  }

  @Override
  public Response dispatch(final Request request) throws Exception {
    checkState(router != null, "Router not installed");
    return router.dispatch(request);
  }
}
