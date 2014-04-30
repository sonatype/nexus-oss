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
package org.sonatype.nexus.blobstore.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.io.CharStreams;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reads and writes blob headers as UTF8-encoded JSON.
 *
 * @since 3.0
 */
public class JsonHeaderFormat
    implements HeaderFileFormat
{
  private static final Charset HEADER_FILE_ENCODING = Charset.forName("UTF-8");

  @Override
  public void write(final Map<String, String> headers, final OutputStream outputStream) throws IOException {
    writeString(outputStream, new JSONObject(headers).toString());
  }

  @Override
  public Map<String, String> read(final InputStream inputStream) throws IOException {
    StringWriter writer = new StringWriter();

    CharStreams.copy(new InputStreamReader(inputStream, HEADER_FILE_ENCODING), writer);

    try {
      return toMap(new JSONObject(writer.toString()));
    }
    catch (JSONException e) {
      throw new IOException(e);
    }
  }

  private Map<String, String> toMap(final JSONObject json) throws JSONException {
    Map<String, String> map = new HashMap<>();
    for (Iterator<String> it = json.keys(); it.hasNext(); ) {
      final String key = it.next();
      map.put(key, json.getString(key));
    }
    return map;
  }

  private void writeString(final OutputStream outputStream, final String string) throws IOException {
    BufferedWriter b = new BufferedWriter(new OutputStreamWriter(outputStream, HEADER_FILE_ENCODING));
    b.write(string);
    b.flush();
    // Don't close the stream, that's done elsewhere
  }
}
