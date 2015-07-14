/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.maven.concurrency.generators;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Content generator for XML files. Note: this class does NOT generate valid XML files.
 */
public class XmlGenerator
    extends Generator
{
  private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  private static final String EMPTY = "<empty/>";

  @Override
  public String getContentType() {
    return MediaType.XML_UTF_8.toString();
  }

  @Override
  public long getExactContentLength(final long length) {
    if (length <= XML_PREAMBLE.length() + EMPTY.length()) {
      return XML_PREAMBLE.length() + "\n".length() + EMPTY.length();
    }
    else {
      long correctedLength = length - XML_PREAMBLE.length();
      long times = correctedLength / EMPTY.length();
      return XML_PREAMBLE.length() + (times * EMPTY.length());
    }
  }

  @Override
  public InputStream generate(final long length) {
    checkArgument(length > 0);
    if (length <= XML_PREAMBLE.length() + EMPTY.length()) {
      // XML must be complete
      return new ByteArrayInputStream((XML_PREAMBLE + "\n" + EMPTY).getBytes(Charsets.UTF_8));
    }
    else {
      long correctedLength = length - XML_PREAMBLE.length();
      long times = correctedLength / EMPTY.length();
      return new SequenceInputStream(new ByteArrayInputStream(XML_PREAMBLE.getBytes(Charsets.UTF_8)),
          repeat(EMPTY.getBytes(Charsets.UTF_8), times));
    }
  }
}
