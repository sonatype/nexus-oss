package com.sonatype.nexus.repository.nuget.internal.odata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import com.sonatype.nexus.repository.nuget.internal.NugetPackageException;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.hash.MultiHashingInputStream;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
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
    try {
      MultiHashingInputStream hashingStream = new MultiHashingInputStream(Arrays.asList(HashAlgorithm.SHA512),
          inputStream);
      Map<String, String> metadata = NuspecSplicer.extractNuspecData(hashingStream);

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
}
