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
package org.sonatype.nexus.repository.proxy.remote;

import java.io.IOException;
import java.net.URI;

import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * @since 3.0
 */
public class ManuallyBlockedState
    extends ComponentSupport
    implements RemoteState
{

  @Override
  public FetchResult fetch(final URI uri) throws IOException {
    return new FetchResult(HttpStatus.SERVICE_UNAVAILABLE);
  }

  @Override
  public String getStatus() {
    return "Remote Manually Blocked";
  }

  @Override
  public String getStatusReason() {
    return null;
  }

}
