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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.restlet.data.Form;
import org.restlet.data.Request;

public class RemoteIPFinder
{
  static final String FORWARD_HEADER = "X-Forwarded-For";

  public static String findIP(HttpServletRequest request) {
    String forwardedIP = getFirstForwardedIp(request.getHeader(FORWARD_HEADER));

    if (forwardedIP != null) {
      return forwardedIP;
    }

    return request.getRemoteAddr();
  }

  public static String findIP(Request request) {
    Form form = (Form) request.getAttributes().get("org.restlet.http.headers");

    String forwardedIP = getFirstForwardedIp(form.getFirstValue(FORWARD_HEADER));

    if (forwardedIP != null) {
      return forwardedIP;
    }

    List<String> clientAddresses = request.getClientInfo().getAddresses();

    if (clientAddresses.size() > 1) {
      // restlet1x ClientInfo.getAddresses has *reverse* order to XFF
      // (this has been fixed in restlet2x, along with a clearer API)

      String[] ipAddresses = new String[clientAddresses.size()];
      for (int i = 0, j = ipAddresses.length - 1; j >= 0; i++, j--) {
        ipAddresses[i] = clientAddresses.get(j);
      }

      forwardedIP = resolveIp(ipAddresses);

      if (forwardedIP != null) {
        return forwardedIP;
      }
    }

    return request.getClientInfo().getAddress();
  }

  /**
   * Returns the *left-most* resolvable IP from the given XFF string; otherwise null.
   */
  private static String getFirstForwardedIp(String forwardedFor) {
    if (!StringUtils.isEmpty(forwardedFor)) {
      return resolveIp(forwardedFor.split("\\s*,\\s*"));
    }

    return null;
  }

  /**
   * Returns the *left-most* resolvable IP from the given sequence.
   */
  private static String resolveIp(String[] ipAddresses) {
    for (String ip : ipAddresses) {
      InetAddress ipAdd;
      try {
        ipAdd = InetAddress.getByName(ip);
      }
      catch (UnknownHostException e) {
        continue;
      }
      if (ipAdd instanceof Inet4Address || ipAdd instanceof Inet6Address) {
        return ip;
      }
    }

    return null;
  }
}
