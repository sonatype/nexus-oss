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
package org.sonatype.nexus.testsuite.maven.chits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;
import com.google.common.net.MediaType;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Content generator for Maven metadata files.
 */
public class MavenMetadataGenerator
    extends Generator
{
  private static final MetadataXpp3Writer writer = new MetadataXpp3Writer();

  private final Metadata metadata;

  public MavenMetadataGenerator(final Metadata metadata) {
    this.metadata = checkNotNull(metadata);
  }

  @Override
  public String getContentType() {
    return MediaType.XML_UTF_8.toString();
  }

  @Override
  public long getExactContentLength(final long length) {
    return asByteArray().length;
  }

  @Override
  public InputStream generate(final long length) {
    return new ByteArrayInputStream(asByteArray());
  }

  private byte[] asByteArray() {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      writer.write(byteArrayOutputStream, generate());
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
    return byteArrayOutputStream.toByteArray();
  }

  private Metadata generate() {
    return metadata;
  }
}
