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
package org.sonatype.nexus.repository.maven.internal.storage;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.maven.internal.Content;
import org.sonatype.nexus.repository.maven.internal.Coordinates;

import org.joda.time.DateTime;

/**
 * Provides persistent storage for {@link Content}.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface MavenContentsFacet
    extends Facet
{
  @Nullable
  Content get(Coordinates coordinates) throws IOException;

  void put(Coordinates coordinates, Content content) throws IOException, InvalidContentException;

  boolean delete(Coordinates coordinates) throws IOException;

  boolean exists(Coordinates coordinates) throws IOException;

  void updateLastUpdated(Coordinates coordinates, final DateTime lastUpdated) throws IOException;
}
