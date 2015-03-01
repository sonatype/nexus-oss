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
package com.sonatype.nexus.repository.nuget.internal.proxy;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

/**
 * Serves NuGet packages, and also exposes proxy configuration and content-fetching.
 *
 * @since 3.0
 */
public class NugetProxyFacet
    extends ProxyFacetSupport
{
  @Override
  protected Payload getCachedPayload(final Context context) throws IOException {
    // TODO via NEXUS-8164
    return null;
  }

  @Override
  protected void store(final Context context, final Payload payload) throws IOException, InvalidContentException {
    // TODO via NEXUS-8164
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException {
    // TODO via NEXUS-8164
    return null;
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {
    // TODO via NEXUS-8164
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    // TODO via NEXUS-8164
    return null;
  }
}
