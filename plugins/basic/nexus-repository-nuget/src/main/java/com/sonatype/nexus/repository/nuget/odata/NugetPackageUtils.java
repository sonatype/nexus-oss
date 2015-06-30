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
package com.sonatype.nexus.repository.nuget.odata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sonatype.nexus.repository.nuget.internal.NugetPackageException;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.hash.MultiHashingInputStream;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.hash.HashCode;
import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.PACKAGE_HASH;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.PACKAGE_HASH_ALGORITHM;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.PACKAGE_SIZE;

/**
 * @since 3.0
 */
public class NugetPackageUtils
{
  /**
   * Determine the metadata for a nuget package.
   * - nuspec data (comes from .nuspec)
   * - size (package size)
   * - hash(es) (package hash sha-512)
   */
  public static Map<String, String> packageMetadata(final InputStream inputStream)
      throws IOException, NugetPackageException
  {
    try (MultiHashingInputStream hashingStream =
             new MultiHashingInputStream(Arrays.asList(HashAlgorithm.SHA512), inputStream)) {
      final byte[] nuspec = extractNuspec(hashingStream);
      Map<String, String> metadata = NuspecSplicer.extractNuspecData(new ByteArrayInputStream(nuspec));

      ByteStreams.copy(hashingStream, ByteStreams.nullOutputStream());

      metadata.put(PACKAGE_SIZE, String.valueOf(hashingStream.count()));
      HashCode code = hashingStream.hashes().get(HashAlgorithm.SHA512);
      metadata.put(PACKAGE_HASH, new String(Base64.encodeBase64(code.asBytes()), Charsets.UTF_8));
      metadata.put(PACKAGE_HASH_ALGORITHM, "SHA512");

      return metadata;
    }
    catch (XmlPullParserException e) {
      throw new NugetPackageException("Unable to read .nuspec from package stream", e);
    }
  }

  private static byte[] extractNuspec(final InputStream is)
  {
    try {
      ZipInputStream zis = new ZipInputStream(is);
      for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
        if (e.getName().endsWith(".nuspec")) {
          return ByteStreams.toByteArray(zis);
        }
      }
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
    throw new RuntimeException("Missing nuspec");
  }
}
