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

import org.restlet.data.Form;
import org.restlet.data.Response;

public class StaticHeaderUtil
{
  // cstamas:
  // Um, this is NOT the way to do this. Representation#setExpirationDate()!!!
  // Read APIDocs!
  // These kind of trickeries should be AVOIDED! Adding directly headers to a response (that belong to entity in response body)
  // without even knowing that a response have any entity is a way to hell! And bugs...
  @Deprecated
  public static void addResponseHeaders(Response response) {
    Form responseHeaders = (Form) response.getAttributes().get("org.restlet.http.headers");

    if (responseHeaders == null) {
      responseHeaders = new Form();
      response.getAttributes().put("org.restlet.http.headers", responseHeaders);
    }

    // Default cache for 30 days
    responseHeaders.add("Cache-Control", "max-age=2592000");
  }
}
