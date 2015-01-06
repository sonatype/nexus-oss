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

import org.sonatype.nexus.repository.util.AttributesMap;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * View response.
 *
 * @since 3.0
 */
public class Response
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

  private final Status status;

  private final Headers headers = new Headers();

  public Response(final Status status) {
    this.status = checkNotNull(status);
  }

  public AttributesMap getAttributes() {
    return attributes;
  }

  public Status getStatus() {
    return status;
  }

  public Headers getHeaders() {
    return headers;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "status=" + status +
        '}';
  }
}
