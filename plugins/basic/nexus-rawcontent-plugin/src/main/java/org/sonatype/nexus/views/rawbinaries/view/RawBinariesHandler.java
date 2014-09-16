/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.views.rawbinaries.view;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.componentviews.responses.Responses;
import org.sonatype.nexus.componentviews.responses.StatusResponse;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinary;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import com.google.common.base.Throwables;
import com.google.common.html.HtmlEscapers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The handler that implements the runtime logic of this format - receiving, listing, serving, and deleting binary
 * files.
 *
 * @since 3.0
 */
public class RawBinariesHandler
    implements Handler
{
  private final RawBinaryStore binaryStore;

  public RawBinariesHandler(final RawBinaryStore binaryStore) {
    this.binaryStore = checkNotNull(binaryStore);
  }

  @Override
  public ViewResponse handle(final ViewRequest req) {
    switch (req.getMethod()) {

      case PUT:
        try {
          final boolean result = binaryStore.create(req.getPath(), req.getContentType(), req.getInputStream());

          if (result) {
            return Responses.created();
          }
          else {
            return new StatusResponse(409,
                "Conflict: binary already exists. To replace it, DELETE it first.");
          }
        }
        catch (IOException e) {
          Throwables.propagate(e);
        }

      case DELETE:
        if (binaryStore.delete(req.getPath())) {
          return Responses.deleted();
        }
        else {
          return Responses.notFound("No artifact to be deleted at that location.");
        }

      case GET:
        final List<RawBinary> forPath = binaryStore.getForPath(req.getPath());

        // Do we have a single result whose path matches the request exactly?
        if (isSuccesfulRequestForSingleArtifact(req, forPath)) {
          final RawBinary binary = forPath.get(0);
          return Responses
              .streamResponse(binary.getInputStream(), binary.getMimeType(), binary.getModifiedDate().toDate());
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        // missing HTML encoding

        html.append("<h1>").append(HtmlEscapers.htmlEscaper().escape(req.getPath())).append("</h1>");
        html.append("<ul>");

        for (RawBinary binary : forPath) {
          html.append(new Formatter().format("<li><a href=\"$s\">$s</a></li>", binary.getPath(), binary.getPath()));
        }
        html.append("</ul>");
        html.append("</body></html>");

        return Responses.html(html.toString());
      default:
        return Responses.methodNotAllowed();
    }
  }

  private boolean isSuccesfulRequestForSingleArtifact(final ViewRequest req, final List<RawBinary> forPath) {
    return forPath.size() == 1 && forPath.get(0).getPath().equals(req.getPath());
  }
}
