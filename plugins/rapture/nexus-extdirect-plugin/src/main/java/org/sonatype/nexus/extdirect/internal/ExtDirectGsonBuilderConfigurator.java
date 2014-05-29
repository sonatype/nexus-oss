/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.extdirect.internal;

import java.lang.reflect.Type;

import org.sonatype.nexus.extdirect.model.Base64String;
import org.sonatype.nexus.extdirect.model.Password;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.softwarementors.extjs.djn.config.GlobalConfiguration;
import com.softwarementors.extjs.djn.gson.DefaultGsonBuilderConfigurator;
import org.apache.shiro.codec.Base64;

/**
 * Additional GSon type adapters.
 *
 * @since 3.0
 */
public class ExtDirectGsonBuilderConfigurator
    extends DefaultGsonBuilderConfigurator
{

  @Override
  public void configure(final GsonBuilder builder, final GlobalConfiguration configuration) {
    if (configuration.getDebug()) {
      builder.setPrettyPrinting();
    }
    builder.serializeNulls();
    builder.disableHtmlEscaping();

    builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    builder.registerTypeAdapter(Base64String.class, new Base64StringSerializer());
    builder.registerTypeAdapter(Password.class, new PasswordSerializer());

    builder.registerTypeAdapter(Base64String.class, new Base64StringDeserializer());
    builder.registerTypeAdapter(Password.class, new PasswordDeserializer());
  }

  private static class Base64StringSerializer
      implements JsonSerializer<Base64String>
  {
    public JsonElement serialize(final Base64String src, final Type typeOfSrc, final JsonSerializationContext context) {
      if (src.getValue() != null) {
        return new JsonPrimitive(Base64.encodeToString(src.getValue().getBytes()));
      }
      return null;
    }
  }

  private static class PasswordSerializer
      implements JsonSerializer<Password>
  {
    public JsonElement serialize(final Password src, final Type typeOfSrc, final JsonSerializationContext context) {
      if (src.getValue() != null) {
        return new JsonPrimitive(Base64.encodeToString(src.getValue().getBytes()));
      }
      return null;
    }
  }

  private static class Base64StringDeserializer
      implements JsonDeserializer<Base64String>
  {
    public Base64String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
      return new Base64String(decode(json));
    }

    static String decode(JsonElement json)
        throws JsonParseException
    {
      if (!json.isJsonPrimitive()) {
        throw new JsonParseException("The value for a Base64String must be a valid String");
      }
      JsonPrimitive primitive = (JsonPrimitive) json;
      if (!primitive.isString()) {
        throw new JsonParseException("The value for a Base64String must be a valid String");
      }
      return Base64.decodeToString(primitive.getAsString());
    }
  }

  private static class PasswordDeserializer
      implements JsonDeserializer<Password>
  {
    public Password deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
      return new Password(Base64StringDeserializer.decode(json));
    }
  }

}
