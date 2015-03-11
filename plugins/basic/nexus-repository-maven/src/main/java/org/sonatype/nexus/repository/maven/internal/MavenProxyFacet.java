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
import org.sonatype.nexus.repository.maven.internal.policy.ChecksumPolicy;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

/**
 * Maven proxy facet.
 *
 * @since 3.0
 */
@Named
public class MavenProxyFacet
    extends ProxyFacetSupport
{
  public static final String CONFIG_KEY = "maven";

  private ChecksumPolicy checksumPolicy;

  @Override
  protected void doConfigure() throws Exception {
    super.doConfigure();
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    checksumPolicy = ChecksumPolicy.valueOf(
        attributes.get("checksumPolicy", String.class, ChecksumPolicy.WARN.name())
    );
  }

  @Override
  protected Payload getCachedPayload(final Context context) throws IOException {
    final MavenFacet contentsFacet = getRepository().facet(MavenFacet.class);
    if (context.getAttributes().contains(ArtifactPath.class)) {
      final ArtifactPath coordinates = context.getAttributes().require(ArtifactPath.class);
      return contentsFacet.getArtifact(coordinates);
    }
    else if (context.getAttributes().contains(Path.class)) {
      final Path path = context.getAttributes().require(Path.class);
      return contentsFacet.getMetadata(path);
    }
    return null;
  }

  @Override
  protected void store(final Context context, final Payload payload) throws IOException, InvalidContentException {
    final MavenFacet contentsFacet = getRepository().facet(MavenFacet.class);
    if (context.getAttributes().contains(ArtifactPath.class)) {
      final ArtifactPath coordinates = context.getAttributes().require(ArtifactPath.class);
      contentsFacet.putArtifact(coordinates, payload);
    }
    else if (context.getAttributes().contains(Path.class)) {
      final Path path = context.getAttributes().require(Path.class);
      contentsFacet.putMetadata(path, payload);
    }
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException {
    return new DateTime();
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {

  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return context.getRequest().getPath();
  }
}
