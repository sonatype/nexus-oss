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

package org.sonatype.nexus.rest.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.sonatype.nexus.internal.DevModeResources;

import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Finder} that will lookup files via {@link DevModeResources#getResourceIfOnFileSystem(String)}.
 *
 * @since 2.7
 */
public class DevModeResourceFinder
    extends Finder
{

  private static Logger log = LoggerFactory.getLogger(DevModeResourceFinder.class);

  private final Context context;

  private final String basePath;

  public DevModeResourceFinder(final Context context, final String basePath) {
    this.context = context;
    this.basePath = basePath;
  }

  public Handler createTarget(final Request request, final Response response) {
    return new Handler(context, request, response)
    {

      @Override
      public boolean allowGet() {
        return true;
      }

      @Override
      public void handleGet() {
        String path = basePath + request.getResourceRef().getRemainingPart(true, false);
        URL url = DevModeResources.getResourceIfOnFileSystem(path);
        if (url != null) {
          try {
            URLConnection urlConnection = url.openConnection();
            FileRepresentation representation = new FileRepresentation(
                url.toExternalForm(),
                MediaType.valueOf(urlConnection.getContentType())
            );
            response.setEntity(representation);
            return;
          }
          catch (IOException e) {
            log.warn("Could not handle request for '{}'", path, e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
          }
        }
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      }

    };
  }

}
