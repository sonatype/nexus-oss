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
package org.sonatype.nexus.repository.httpbridge.internal;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.repository.view.Request;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link Request} adapter for {@link HttpServletRequest}.
 *
 * @since 3.0
 */
class HttpRequestAdapter
    extends Request
{
  public HttpRequestAdapter(final HttpServletRequest httpServletRequest,
                            final String path)
  {
    checkNotNull(httpServletRequest);

    this.action = httpServletRequest.getMethod();
    this.requestUrl = httpServletRequest.getRequestURL().toString();
    this.path = checkNotNull(path);
    this.parameters = new HttpParametersAdapter(httpServletRequest);
    this.headers = new HttpHeadersAdapter(httpServletRequest);

    // copy http-servlet-request attributes
    Enumeration<String> attributes = httpServletRequest.getAttributeNames();
    while (attributes.hasMoreElements()) {
      String name = attributes.nextElement();
      getAttributes().set(name, httpServletRequest.getAttribute(name));
    }

    this.payload = new HttpRequestPayloadAdapter(httpServletRequest);

    // We're circumventing ServletFileUpload.isMultipartContent as some clients (nuget) use PUT for multipart uploads
    this.multipart = FileUploadBase.isMultipartContent(new ServletRequestContext(httpServletRequest));
    if (multipart) {
      this.multiPayloads = new HttpPartIteratorAdapter(httpServletRequest);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "action='" + action + '\'' +
        ", path='" + path + '\'' +
        ", parameters=" + parameters +
        ", payload=" + payload +
        '}';
  }

}
