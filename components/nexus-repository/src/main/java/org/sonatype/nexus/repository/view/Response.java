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

import org.sonatype.nexus.common.collect.AttributesMap;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * View response.
 *
 * @since 3.0
 */
public class Response
{
  private Status status;

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

  private Attributes attributes;

  private Headers headers;

  public Response(final Status status) {
    setStatus(status);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(final Status status) {
    this.status = checkNotNull(status);
  }

  public AttributesMap getAttributes() {
    if (attributes == null) {
      attributes = new Attributes();
    }
    return attributes;
  }

  public Headers getHeaders() {
    if (headers == null) {
      headers = new Headers();
    }
    return headers;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "status=" + status +
        '}';
  }
}
