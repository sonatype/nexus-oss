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
package org.sonatype.nexus.repository.httpclient;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkState;

/**
 * {@link AuthenticationConfig} deserializer.
 *
 * Determines the instance class by the {@code type} field.
 *
 * @since 3.0
 */
public class AuthenticationConfigDeserializer
    extends StdDeserializer<AuthenticationConfig>
{
  /**
   * Mapping of type-name to type-class.
   */
  private static final Map<String, Class<? extends AuthenticationConfig>> types = ImmutableMap.of(
      UsernameAuthenticationConfig.TYPE, UsernameAuthenticationConfig.class,
      NtlmAuthenticationConfig.TYPE, NtlmAuthenticationConfig.class
  );

  public AuthenticationConfigDeserializer() {
    super(AuthenticationConfig.class);
  }

  @Override
  public AuthenticationConfig deserialize(final JsonParser parser, final DeserializationContext context)
      throws IOException, JsonProcessingException
  {
    JsonNode node = parser.readValueAsTree();
    String typeName = node.get("type").textValue();
    Class<? extends AuthenticationConfig> type = types.get(typeName);
    checkState(type != null, "Unknown AuthenticationConfig type: %s", typeName);
    return parser.getCodec().treeToValue(node, type);
  }
}
