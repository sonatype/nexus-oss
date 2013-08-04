/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.rest;

import org.sonatype.nexus.plugins.rest.StaticResource;

import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class StaticResourceFinder
    extends Finder
{
  private final Context context;

  private final StaticResource resource;

  public StaticResourceFinder(Context context, StaticResource resource) {
    this.context = context;

    this.resource = resource;
  }

  public Handler createTarget(Request request, Response response) {
    StaticHeaderUtil.addResponseHeaders(response);

    StaticResourceResource resourceResource = new StaticResourceResource(context, request, response, resource);

    return resourceResource;
  }

}
