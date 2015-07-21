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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;
import com.google.common.net.MediaType;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Content generator for Maven metadata files. This generator once created is immutable, as holds the byte array
 * of metadata created by {@link MetadataXpp3Writer}. This also means that {@link Metadata} instance used by
 * caller to create this instance may be mutated to create more instances of this generator.
 */
public class MavenMetadataGenerator
    extends Generator
{
  private static final MetadataXpp3Writer writer = new MetadataXpp3Writer();

  private byte[] metadata;

  public MavenMetadataGenerator(final Metadata metadata) {
    checkNotNull(metadata);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      writer.write(byteArrayOutputStream, metadata);
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
    this.metadata = byteArrayOutputStream.toByteArray();
  }

  @Override
  public String getContentType() {
    return MediaType.XML_UTF_8.toString();
  }

  @Override
  public long getExactContentLength(final long length) {
    return metadata.length;
  }

  @Override
  public InputStream generate(final long length) {
    return new ByteArrayInputStream(metadata);
  }
}
