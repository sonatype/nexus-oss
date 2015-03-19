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
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.maven.internal.policy.ChecksumPolicy;
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;

/**
 * Maven facet, present on all Maven repositories.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface MavenFacet
    extends Facet
{
  @Nonnull
  VersionPolicy getVersionPolicy();

  @Nonnull
  ChecksumPolicy getChecksumPolicy();

  @Nullable
  BlobPayload get(MavenPath path) throws IOException;

  void put(MavenPath path, Payload content) throws IOException, InvalidContentException;

  boolean delete(MavenPath path) throws IOException;
}
