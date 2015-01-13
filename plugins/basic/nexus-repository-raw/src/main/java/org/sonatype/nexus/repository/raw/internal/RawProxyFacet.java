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

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import static com.google.common.base.Preconditions.checkArgument;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

/**
 * An implementation of {@link RawStorageFacet} whose {@link #get(String)} method first checks a remote location.
 *
 * @since 3.0
 */
public class RawProxyFacet
    extends RawStorageFacetImpl
    implements RawStorageFacet
{
  @Nullable
  @Override
  @Guarded(by = STARTED)
  public RawContent get(final String path) throws IOException {
    return super.get(path);
  }

  private Object obsoleteMoveToProxyStorage(final Context context) throws Exception {

    final Request request = context.getRequest();

    checkArgument(request.getAction().equals("GET"), "%s can only retarget GET requests", getClass().getSimpleName());

    final HttpClientFacet httpFacet = context.getRepository().facet(HttpClientFacet.class);

    // TODO: Map more fields of the request. Presumably the cache handler, higher in the stack, should add an 'if modified since' header to the request

    //final RawRemoteSourceFacet facet = context.getRepository().facet(RawRemoteSourceFacet.class);

    final String remoteUrlBase = ""; //facet.getRemoteUrlBase();

    final HttpGet httpGet = new HttpGet(remoteUrlBase + request.getPath());

    final HttpClient httpClient = httpFacet.getHttpClient();

    // TODO: Actually wire this up.
    final HttpResponse execute = httpClient.execute(httpGet);

    return null;
  }

  @Nullable
  @Override
  @Guarded(by = STARTED)
  public void put(final String path, final RawContent content) throws IOException {
    throw new UnsupportedOperationException("Do not put() a proxy storage facet");
  }

  @Override
  @Guarded(by = STARTED)
  public boolean delete(final String path) throws IOException {
    throw new UnsupportedOperationException("Do not delete() from a proxy storage facet");
  }
}
