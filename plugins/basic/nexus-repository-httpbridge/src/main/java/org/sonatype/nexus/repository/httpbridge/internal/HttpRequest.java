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
package org.sonatype.nexus.repository.httpbridge.internal;

import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.view.Request;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link Request} adapter for {@link HttpServletRequest}.
 *
 * @since 3.0
 */
public class HttpRequest
  extends Request
{
  public HttpRequest(final HttpServletRequest httpServletRequest, final Repository repository, final String path) {
    checkNotNull(httpServletRequest);

    // decode http-servlet-request into normalized request bits
    this.repository = checkNotNull(repository);
    this.action = httpServletRequest.getMethod();
    this.path = checkNotNull(path);
    this.parameters = new HttpParameters(httpServletRequest);
    this.headers = new HttpHeaders(httpServletRequest);
    this.attributes = Maps.newHashMap();

    // TODO: wrap payload if there is one
    this.payload = null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "action='" + action + '\'' +
        ", path='" + path + '\'' +
        ", parameters=" + parameters +
        ", payload=" + payload +
        '}';
  }
}
