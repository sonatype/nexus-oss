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

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * A filter that tries to recognize browsers a "clients", and "fixes" their Acccept headers.
 *
 * @author cstamas
 * @see http://article.gmane.org/gmane.comp.java.restlet/4205/match=browser
 */
public class BrowserSensingFilter
    extends Filter
{

  /**
   * The filter constructor.
   */
  public BrowserSensingFilter(Context context) {
    super(context);
  }

  /**
   * A beforeHandle will simply embed in request attributes a Nexus interface implemntor, depending on key used to
   * name it.
   */
  protected int beforeHandle(Request request, Response response) {
    String agentInfo = request.getClientInfo().getAgent() != null ? request
        .getClientInfo().getAgent().toLowerCase() : "unknown";

    // This solution was the only that came on my mind :)
    // should work only if client specified more then one "alternatives"
    if (StringUtils.indexOfAny(agentInfo, new String[]{"mozilla", "firefox", "msie", "opera", "safari"}) > -1
        && request.getClientInfo().getAcceptedMediaTypes().size() > 1) {
      // overriding client preferences, since it is a browser to TEXT/HTML
      // doing this by adding text/html as firxt to accepted media types with quality 1
      request
          .getClientInfo().getAcceptedMediaTypes().add(0, new Preference<MediaType>(MediaType.TEXT_HTML, 1));
    }

    return CONTINUE;
  }

}
