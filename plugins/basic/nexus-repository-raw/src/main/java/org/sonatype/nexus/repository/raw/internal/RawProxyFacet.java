/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import javax.annotation.Nonnull;
import javax.inject.Named;

import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.joda.time.DateTime;

import static org.sonatype.nexus.repository.raw.internal.RawContentPayloadMarshaller.toContent;
import static org.sonatype.nexus.repository.raw.internal.RawContentPayloadMarshaller.toPayload;

/**
 * @since 3.0
 */
@Named
public class RawProxyFacet
    extends ProxyFacetSupport
{
  @Override
  protected Content getCachedPayload(final Context context) throws IOException {
    final String path = componentPath(context);

    final RawContent rawContent = content().get(path);
    if (rawContent == null) {
      return null;
    }

    return new Content(toPayload(rawContent));
  }

  @Override
  protected DateTime getCachedPayloadLastVerified(final Context context) throws IOException {
    final RawContent rawContent = content().get(componentPath(context));
    return rawContent != null ? rawContent.getLastVerified() : null;
  }

  @Override
  protected void indicateVerified(final Context context) throws IOException {
    content().updateLastVerified(componentPath(context), new DateTime());
  }

  @Override
  protected void store(final Context context, final Content payload) throws IOException, InvalidContentException {
    final String path = componentPath(context);
    content().put(path, toContent(payload, new DateTime()));
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return componentPath(context);
  }

  /**
   * Determines what 'component' this request relates to.
   */
  private String componentPath(final Context context) {
    final TokenMatcher.State tokenMatcherState = context.getAttributes().require(TokenMatcher.State.class);
    return tokenMatcherState.getTokens().get("name");
  }

  private RawContentFacet content() {
    return getRepository().facet(RawContentFacet.class);
  }
}
