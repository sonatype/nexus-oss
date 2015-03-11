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

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matcher that matches for Maven artifacts only, and sets {@link ArtifactPath} in context attributes.
 *
 * @since 3.0
 */
public class MavenArtifactMatcher
    extends ComponentSupport
    implements Matcher
{
  private final ArtifactPathParser artifactPathParser;

  public MavenArtifactMatcher(final ArtifactPathParser artifactPathParser) {
    this.artifactPathParser = checkNotNull(artifactPathParser);
  }

  @Override
  public boolean matches(final Context context) {
    final String path = context.getRequest().getPath();
    final ArtifactPath artifactPath = artifactPathParser.parsePath(path);
    if (artifactPath != null) {
      context.getAttributes().set(ArtifactPath.class, artifactPath);
      return true;
    }
    return false;
  }
}
