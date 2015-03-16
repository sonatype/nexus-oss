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
package org.sonatype.nexus.repository.maven.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Named;

import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

/**
 * Maven proxy facet that talks to {@link MavenFacet} and {@link MavenTransportFacet}.
 *
 * @since 3.0
 */
@Named
public class MavenProxyFacet
    extends ProxyFacetSupport
{
  private static final String NOW = MavenProxyFacet.class.getName() + ".now";

  private MavenFacet mavenFacet;

  private MavenTransportFacet mavenTransportFacet;

  @Override
  protected void doConfigure() throws Exception {
    super.doConfigure();
    mavenFacet = getRepository().facet(MavenFacet.class);
    mavenTransportFacet = getRepository().facet(MavenTransportFacet.class);
  }

  @Override
  public Payload get(final Context context) throws IOException {
    context.getAttributes().set(NOW, DateTime.now());
    return super.get(context);
  }

  @Override
  protected MavenPayload getCachedPayload(final Context context) throws IOException {
    final MavenPath mavenPath = context.getAttributes().require(MavenPath.class);
    return mavenFacet.get(mavenPath);
  }

  @Override
  protected MavenPayload fetch(final String url, final Context context, final Payload stalePayload)
      throws IOException
  {
    MavenPath mavenPath = context.getAttributes().require(MavenPath.class);
    final MavenPayload payload = mavenTransportFacet
        .fetch(mavenPath, now(context), getRemoteUrl().resolve(url), (MavenPayload) stalePayload);
    if (stalePayload != null && payload == stalePayload) {
      indicateUpToDate(context);
    }
    return payload;
  }

  @Override
  protected void store(final Context context, final Payload payload) throws IOException, InvalidContentException {
    final MavenPath mavenPath = context.getAttributes().get(MavenPath.class);
    mavenFacet.put(mavenPath, (MavenPayload) payload);
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context, final Payload payload)
      throws IOException
  {
    return ((MavenPayload)payload).getLastVerified();
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {
    final MavenPath mavenPath = context.getAttributes().require(MavenPath.class);
    mavenFacet.verify(mavenPath, now(context));
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return context.getRequest().getPath().substring(1);
  }

  private DateTime now(final Context context) {
    return context.getAttributes().require(NOW, DateTime.class);
  }
}
