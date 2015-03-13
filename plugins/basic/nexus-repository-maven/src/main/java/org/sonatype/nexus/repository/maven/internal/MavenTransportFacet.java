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
import java.net.URI;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;

import org.joda.time.DateTime;

/**
 * Maven proxy facet.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface MavenTransportFacet
    extends Facet
{
  /**
   * Performs one or more remote fetches to fetch locally non-existent or stale payload. Returns {@code null} if not
   * found on remote. Returns passed in {@code stalePayload} if it is detected as up to date. Returns freshly fetched
   * {@link MavenPayload} if remote has either newer or no stalePayload existed in the first place, but exists
   * remotely.
   */
  @Nullable
  MavenPayload fetch(final Path path, final DateTime now, final URI uri, final @Nullable MavenPayload stalePayload) throws IOException;
}
