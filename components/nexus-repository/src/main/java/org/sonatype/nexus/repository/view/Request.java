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
package org.sonatype.nexus.repository.view;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.collect.AttributesMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

/**
 * View request.
 *
 * @since 3.0
 */
public class Request
{
  /**
   * Custom attributes to configure backing and logging.
   */
  private static class Attributes
      extends AttributesMap
  {
    public Attributes() {
      super(Maps.<String, Object>newHashMap());
    }
  }

  private final Attributes attributes = new Attributes();

  protected String action;

  protected String requestUrl;

  protected String path;

  protected Parameters parameters;

  protected Headers headers;

  protected Payload payload;

  protected boolean multipart;

  protected Iterable<Payload> multiPayloads;

  protected Request() {
    // empty
  }

  @VisibleForTesting
  public Request(final String path) {
    this.path = path;
  }

  public AttributesMap getAttributes() {
    return attributes;
  }

  // TODO: Add setters and sanity conditions, and/or introduce a builder to be used for direct api usage?

  // FIXME: Sort out 'action' or 'method'

  public String getAction() {
    return action;
  }

  @Deprecated
  public String getRequestUrl() {
    return requestUrl;
  }

  public String getPath() {
    return path;
  }

  public Parameters getParameters() {
    return parameters;
  }

  public Headers getHeaders() {
    return headers;
  }

  @Nullable
  public Payload getPayload() {
    return payload;
  }

  public boolean isMultipart() {
    return multipart;
  }

  @Nullable
  public Iterable<Payload> getMultiparts() {
    return multiPayloads;
  }

  @Override
  public String toString() {
    // Excluding some members which could lead to very verbose logging
    return getClass().getSimpleName() + "{" +
        "action='" + action + '\'' +
        ", requestUrl='" + requestUrl + '\'' +
        ", path='" + path + '\'' +
        ", parameters=" + parameters +
        ", payload=" + payload +
        '}';
  }
}
