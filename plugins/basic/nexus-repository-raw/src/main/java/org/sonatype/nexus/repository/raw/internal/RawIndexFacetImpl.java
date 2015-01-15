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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

/**
 * Default {@link RawIndexFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class RawIndexFacetImpl
    extends FacetSupport
    implements RawIndexFacet
{
  private final Provider<Client> client;

  @Inject
  public RawIndexFacetImpl(final Provider<Client> client) {
    this.client = checkNotNull(client);
  }

  @Override
  @Guarded(by = STARTED)
  public void put(final String name) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder()
        .startObject()
        .field("name", name)
        .endObject();

    client.get().prepareIndex("repository", getRepository().getName(), name)
        .setSource(builder.string())
        .execute()
        .actionGet();
  }

  @Override
  @Guarded(by = STARTED)
  public void delete(final String name) {
    client.get().prepareDelete("repository", getRepository().getName(), name)
        .execute()
        .actionGet();
  }
}
