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
import org.sonatype.nexus.repository.maven.internal.Content;
import org.sonatype.nexus.repository.maven.internal.storage.MavenContentsFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

import static org.sonatype.nexus.repository.maven.internal.ContentHelper.toContent;
import static org.sonatype.nexus.repository.maven.internal.ContentHelper.toPayload;
import static org.sonatype.nexus.repository.maven.internal.ContextHelper.coordinates;
import static org.sonatype.nexus.repository.maven.internal.ContextHelper.name;

/**
 * @since 3.0
 */
@Named
public class ProxyFacetImpl
    extends ProxyFacetSupport
{
  @Override
  protected Payload getCachedPayload(final Context context) throws IOException {
    final Content content = storage().get(coordinates(context));
    if (content == null) {
      return null;
    }
    return toPayload(content);
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException {
    final Content content = storage().get(coordinates(context));
    return content != null ? content.getLastUpdated() : null;
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {
    storage().updateLastUpdated(coordinates(context), new DateTime());
  }

  @Override
  protected void store(final Context context, final Payload payload) throws IOException, InvalidContentException {
    storage().put(coordinates(context), toContent(payload, new DateTime()));
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return name(context);
  }

  private MavenContentsFacet storage() {
    return getRepository().facet(MavenContentsFacet.class);
  }
}
