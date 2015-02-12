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
package org.sonatype.nexus.repository.search;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON related utilities.
 *
 * @since 3.0
 */
@Named
@Singleton
public class JsonUtils
{

  /**
   * Converts any object to JSON.
   */
  public static String from(final Object value) throws IOException {
    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
  }

  /**
   * Merges json objects.
   */
  public static String merge(final String... jsons) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode merged = mapper.readTree("{}");
    for (String json : jsons) {
      merged = merge(merged, mapper.readTree(json));
    }
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(merged);
  }

  /**
   * Merges json objects.
   */
  public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode jsonNode = mainNode.get(fieldName);
      // if field exists and is an embedded object
      if (jsonNode != null && jsonNode.isObject()) {
        merge(jsonNode, updateNode.get(fieldName));
      }
      else {
        if (mainNode instanceof ObjectNode) {
          // Overwrite field
          JsonNode value = updateNode.get(fieldName);
          ((ObjectNode) mainNode).put(fieldName, value);
        }
      }
    }
    return mainNode;
  }

}
