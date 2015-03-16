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
package org.sonatype.nexus.repository.httpbridge.internal.describe;

import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.nexus.repository.util.StringMultimap;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Helper to render description information.
 *
 * @since 3.0
 */
public class DescriptionUtils
{
  private DescriptionUtils() {}

  public static void describeRequest(final Description desc, final Request request) {
    desc.topic("Request");

    desc.addTable("Details", ImmutableMap.<String, Object>builder()
            .put("Action", request.getAction())
            .put("URL", request.getRequestUrl())
            .put("path", request.getPath()).build()
    );

    if (request.isMultipart()) {
      for (Payload payload : request.getMultiparts()) {
        desc.addTable("Payload", toMap(payload));
      }
    }
    else {
      if (request.getPayload() != null) {
        desc.addTable("Payload", toMap(request.getPayload()));
      }
    }

    desc.addTable("Headers", toMap(request.getHeaders()));
    desc.addTable("Attributes", toMap(request.getAttributes()));
  }

  public static void describeResponse(final Description desc, final Response response) {
    desc.topic("Response");

    desc.addTable("Headers", toMap(response.getHeaders()));
    desc.addTable("Attributes", toMap(response.getAttributes()));

    if (response instanceof PayloadResponse) {
      final PayloadResponse payloadResponse = (PayloadResponse) response;
      final Payload payload = payloadResponse.getPayload();
      desc.addTable("Payload", toMap(payload));
    }
  }

  public static ImmutableMap<String, Object> toMap(final Payload payload) {
    return ImmutableMap.<String, Object>of(
        "Content-Type", nullToEmpty(payload.getContentType()),
        "Size", payload.getSize()
    );
  }

  public static void describeException(final Description d, final Exception e) {
    d.topic("Exception during handler processing");

    for (Throwable cause : Throwables.getCausalChain(e)) {
      d.addTable(cause.getClass().getName(),
          ImmutableMap.<String, Object>of("Message", nullToEmpty(cause.getMessage())));
    }
  }

  private static Map<String, Object> toMap(final Iterable<Entry<String, Object>> entries) {
    Map<String, Object> table = Maps.newHashMap();
    for (Entry<String, Object> entry : entries) {
      table.put(entry.getKey(), entry.getValue());
    }
    return table;
  }

  private static Map<String, Object> toMap(final StringMultimap headers) {
    Map<String, Object> table = Maps.newHashMap();
    final Iterable<Entry<String, String>> entries = headers.entries();
    for (Entry<String, String> e : entries) {
      table.put(e.getKey(), e.getValue());
    }
    return table;
  }
}
