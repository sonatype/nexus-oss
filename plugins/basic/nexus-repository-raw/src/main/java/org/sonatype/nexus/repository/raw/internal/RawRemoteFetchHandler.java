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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A handler that retargets the context Request to the remote HTTP resource contained by the repository's {@link
 * HttpClientFacet}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RawRemoteFetchHandler
    implements Handler
{
  @Override
  public Response handle(final Context context) throws Exception {
    final Request request = context.getRequest();

    checkArgument(request.getAction().equals("GET"), "%s can only retarget GET requests", getClass().getSimpleName());

    final HttpClientFacet httpFacet = context.getRepository().facet(HttpClientFacet.class);

    // TODO: Map more fields of the request. Presumably the cache handler, higher in the stack, should add an 'if modified since' header to the request

    final RawRemoteSourceFacet facet = context.getRepository().facet(RawRemoteSourceFacet.class);

    final String remoteUrlBase = facet.getRemoteUrlBase();

    final HttpGet httpGet = new HttpGet(remoteUrlBase + request.getPath());

    final HttpClient httpClient = httpFacet.getHttpClient();

    // TODO: Actually wire this up.
    final HttpResponse execute = httpClient.execute(httpGet);

    return null;
  }
}
