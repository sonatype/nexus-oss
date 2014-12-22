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
package org.sonatype.nexus.repository.raw;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpClientFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;

import org.apache.http.client.methods.HttpGet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A handler that retargets the context Request to the remote HTTP resource contained by the repository's {@link
 * HttpClientFacet}.
 *
 * @since 3.0
 */
public class RemoteFetchHandler
    implements Handler
{
  private final Repository repository;

  public RemoteFetchHandler(final Repository repository) {
    this.repository = repository;
  }

  @Override
  public Response handle(final Context context) throws Exception {
    final Request request = context.getRequest();

    checkArgument(request.getAction().equals("GET"), "%s can only retarget GET requests", getClass().getSimpleName());

    final HttpClientFacet httpFacet = repository.facet(HttpClientFacet.class);

    // TODO: Map more fields of the request. Presumably the cache handler, higher in the stack, should add an 'if modified since' header to the request
    final String path = request.getPath();

    return httpFacet.getResponse(path);
  }
}
