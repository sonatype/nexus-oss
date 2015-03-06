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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static org.sonatype.nexus.repository.maven.internal.ContextHelper.name;

/**
 * Maven tokenizer handler, that should be among very 1st handlers, as it prepares "maven state".
 *
 * @since 3.0
 */
// TODO: better would be if this replaced TokenMatcher, if it would be extensible to accept external TokenParser implementations?
public class MavenTokenizerHandler
    extends ComponentSupport
    implements Handler
{
  public static interface State
  {
    @Nonnull
    String getPath();

    @Nullable
    Map<String, String> getCoordinates();
  }

  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final String path = name(context);
    final MavenFacet mavenFacet = context.getRepository().facet(MavenFacet.class);
    final Map<String, String> coordinates = mavenFacet.getPathParser().parsePath(path);
    context.getAttributes().set(State.class, new State()
    {
      @Nonnull
      @Override
      public String getPath() {
        return path;
      }

      @Nullable
      @Override
      public Map<String, String> getCoordinates() {
        return coordinates;
      }
    });
    return context.proceed();
  }
}
