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
package org.sonatype.nexus.repository.maven;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

/**
 * Maven facet, present on all Maven repositories.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface MavenFacet
    extends Facet
{
  /**
   * Returns the format specific {@link MavenPathParser}.
   */
  @Nonnull
  MavenPathParser getMavenPathParser();

  /**
   * Returns the version policy in effect for this repository.
   */
  @Nonnull
  VersionPolicy getVersionPolicy();

  // HTTP operations

  @Nullable
  Content get(MavenPath path) throws IOException;

  void put(MavenPath path, Payload payload) throws IOException;

  boolean delete(MavenPath... paths) throws IOException;

  // proxy operations

  DateTime getLastVerified(MavenPath path) throws IOException;

  boolean setLastVerified(MavenPath path, DateTime verified) throws IOException;
}
