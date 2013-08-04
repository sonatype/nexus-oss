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

package org.sonatype.sisu.jetty.custom;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * Simple rule to perform a HTTP to HTTPS redirect. Usable for testing and such, but Jetty has real solutions and
 * handlers to perform this.
 *
 * @author jdcasey
 */
public class RedirectToHttpsRule
    extends Rule
{
  private static final Logger LOG = Log.getLogger(RedirectToHttpsRule.class.getName());

  private Integer httpsPort;

  public RedirectToHttpsRule() {
    setTerminating(true);
  }

  public int getHttpsPort() {
    return httpsPort;
  }

  public void setHttpsPort(int httpsPort) {
    LOG.debug("HTTPS port set to: {}", httpsPort, null);

    this.httpsPort = httpsPort;
  }

  @Override
  public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    StringBuffer requestURL = request.getRequestURL();
    LOG.debug("Original URL: {}", requestURL, null);

    if (!requestURL.toString().startsWith("https")) {
      if ("POST".equals(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "POST to HTTP not supported. Please use HTTPS"
            + (httpsPort == null ? "" : " (Port: " + httpsPort + ")") + " instead.");
        return target;
      }

      URL url = new URL(requestURL.toString());

      StringBuilder result = new StringBuilder();
      result.append("https://").append(url.getHost());

      if (httpsPort != null) {
        result.append(':').append(httpsPort);
      }

      result.append(url.getPath());

      String queryString = request.getQueryString();
      if (queryString != null) {
        LOG.debug("Adding query string to redirect: {}", queryString, null);
        result.append('?').append(queryString);
      }

      LOG.debug("Redirecting to URL: {}", result, null);
      response.sendRedirect(result.toString());
      return target;
    }
    else {
      LOG.debug("NOT redirecting. Already HTTPS: {}", requestURL, null);
      return null;
    }
  }
}
