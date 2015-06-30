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
package com.sonatype.nexus.repository.nuget.internal;

import javax.annotation.Nonnull;

import com.sonatype.nexus.repository.nuget.odata.ODataFeedUtils;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Response;

/**
 * @since 3.0
 */
public class NugetStaticFeedHandler
    extends AbstractNugetHandler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final String path = context.getRequest().getPath();

    switch (path) {
      case "/":
        return xmlPayload(200, ODataFeedUtils.root(getRepositoryBase(context)));
      case "/$metadata":
        return xmlPayload(200, ODataFeedUtils.metadata());
      default:
        throw new IllegalStateException();
    }
  }
}
