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

import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.sisu.goodies.common.ByteSize;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.tests.http.server.api.Behaviour;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Content generator {@link Behaviour} that uses {@link Generator} to generate content of required length, and some
 * other properties like last modified etc.
 */
public class GeneratorBehaviour
    extends ComponentSupport
    implements Behaviour
{
  private final Generator generator;

  private ByteSize length;

  private boolean reportLength;

  private DateTime lastModified;

  private String etag;

  public GeneratorBehaviour(final Generator generator) {
    this.generator = checkNotNull(generator);
    setContentProperties(ByteSize.kiloBytes(1L), true, DateTime.now(), null);
  }

  public void setContentProperties(final ByteSize length,
                                   final boolean reportLength,
                                   @Nullable final DateTime lastModified,
                                   @Nullable final String etag)
  {
    checkArgument(length.value() >= 0);
    this.length = length;
    this.reportLength = reportLength;
    this.lastModified = lastModified;
    this.etag = etag;
  }

  @Override
  public boolean execute(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final Map<Object, Object> ctx) throws Exception
  {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setHeader(HttpHeaders.CONTENT_TYPE, generator.getContentType());
    if (lastModified != null) {
      response.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModified.getMillis());
    }
    if (!Strings2.isBlank(etag)) {
      response.setHeader(HttpHeaders.ETAG, "\n" + etag + "\"");
    }
    if (reportLength) {
      response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(generator.getExactContentLength(length.toBytes())));
    }
    try (ServletOutputStream out = response.getOutputStream()) {
      ByteStreams.copy(generator.generate(length.toBytes()), out);
    }
    return false;
  }
}
