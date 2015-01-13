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

import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * A handler that retargets the context Request to the remote HTTP resource contained by the repository's {@link
 * HttpClientFacet}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RawProxyHandler
    extends ComponentSupport
    implements Handler
{
  @Override
  public Response handle(final Context context) throws Exception {
    final RawStorageFacet storage = context.getRepository().facet(RawStorageFacet.class);
    final String path = context.getRequest().getPath();
    final RawContent rawContent = storage.get(path);

    if (rawContent == null) {
      return HttpResponses.notFound(path);
    }

    final Payload payload = RawContentPayloadMarshaller.toPayload(rawContent);

    return HttpResponses.ok(payload);
  }
}
