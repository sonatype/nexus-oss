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

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.raw.internal.RawLocatorFacet.RawLocator;
import org.sonatype.nexus.repository.raw.internal.proxy.Locator;
import org.sonatype.nexus.repository.raw.internal.proxy.PayloadStorage;
import org.sonatype.nexus.repository.view.Payload;

import static org.sonatype.nexus.repository.raw.internal.RawContentPayloadMarshaller.toContent;
import static org.sonatype.nexus.repository.raw.internal.RawContentPayloadMarshaller.toPayload;

/**
 * @since 3.0
 */
public class RawPayloadStorage
    extends FacetSupport
    implements PayloadStorage
{
  @Override
  public Payload get(final Locator locator) throws IOException {

    final RawLocator rawLocator = (RawLocator) locator;

    final RawContent rawContent = storage().get(rawLocator.path());
    if (rawContent == null) {
      return null;
    }

    return toPayload(rawContent);
  }

  @Override
  public Payload put(final Locator locator, final Payload payload) throws IOException {
    final RawLocator rawLocator = (RawLocator) locator;

    final RawContent put = storage().put(rawLocator.path(), toContent(payload));

    return toPayload(put);
  }

  @Override
  public boolean delete(final Locator locator) {
    throw new UnsupportedOperationException("not implemented");
  }

  private RawStorageFacet storage() {
    return getRepository().facet(RawStorageFacet.class);
  }
}
