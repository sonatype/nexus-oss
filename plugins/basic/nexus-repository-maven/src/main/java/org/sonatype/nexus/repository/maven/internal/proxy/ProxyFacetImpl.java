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
package org.sonatype.nexus.repository.maven.internal.proxy;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Named;

import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.maven.Content;
import org.sonatype.nexus.repository.maven.internal.storage.MavenStorageFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

import static org.sonatype.nexus.repository.maven.internal.ContentHelper.contentName;
import static org.sonatype.nexus.repository.maven.internal.ContentHelper.toContent;
import static org.sonatype.nexus.repository.maven.internal.ContentHelper.toPayload;

/**
 * @since 3.0
 */
@Named
public class ProxyFacetImpl
    extends ProxyFacetSupport
{
  @Override
  protected Payload getCachedPayload(final Context context) throws IOException {
    final String name = contentName(context);
    final Content content = storage().get(name);
    if (content == null) {
      return null;
    }
    return toPayload(content);
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException {
    final Content content = storage().get(contentName(context));
    return content != null ? content.getLastUpdated() : null;
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {
    storage().updateLastUpdated(contentName(context), new DateTime());
  }

  @Override
  protected void store(final Context context, final Payload payload) throws IOException, InvalidContentException {
    final String name = contentName(context);
    storage().put(name, toContent(payload, new DateTime()));
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return contentName(context);
  }

  private MavenStorageFacet storage() {
    return getRepository().facet(MavenStorageFacet.class);
  }
}
