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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since 3.0
 */
public class JsonHeaderFormatTest
{
  @Test
  public void simpleHeaders() throws IOException {
    final Map<String, String> map = ImmutableMap.of("hi", "bye");
    assertThat("headers", roundTripFormatting(map), equalTo(map));
  }

  @Test
  public void evilKeys() throws IOException {
    final Map<String, String> headers = ImmutableMap.of("\n\n''\"\"\"\"\t", "charlie");
    assertThat("evil headers", roundTripFormatting(headers), equalTo(headers));
  }

  private Map<String, String> roundTripFormatting(final Map<String, String> map) throws IOException {
    final JsonHeaderFormat formatter = new JsonHeaderFormat();

    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    formatter.write(map, b);

    final byte[] bytes = b.toByteArray();

    final ByteArrayInputStream input = new ByteArrayInputStream(bytes);

    return formatter.read(input);
  }
}
