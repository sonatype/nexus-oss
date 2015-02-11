package org.sonatype.nexus.common.hash;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.google.common.hash.HashCode;
import com.google.common.io.ByteStreams;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MultiHashingInputStreamTest
{
  @Test
  public void sha512IsAccurate() throws IOException {
    byte[] bytes = new byte[100];

    final MultiHashingInputStream hashingStream = createAndUseHashingStream(bytes);

    final HashCode hashCode = hashingStream.hashes().get(HashAlgorithm.SHA512);

    assertThat(hashCode.toString(), is(equalTo(
        "f206f4f0ef09b90837f1d15a07c6cf4bd291d817663f9f85a0fc4341ec19910719ad571b6102a366ae848cd0f187d0daef912e05898b82c35213cd49a45ee8e0")));
  }

  @Test
  public void testCountIsAccurate() throws IOException {
    final long byteArrayLength = 100;

    final MultiHashingInputStream andUseHashingStream = createAndUseHashingStream(new byte[(int) byteArrayLength]);
    assertThat(andUseHashingStream.count(), is(equalTo(byteArrayLength)));
  }

  private MultiHashingInputStream createAndUseHashingStream(final byte[] bytes) throws IOException {
    final MultiHashingInputStream hashingStream = new MultiHashingInputStream(
        Arrays.asList(HashAlgorithm.SHA512), new ByteArrayInputStream(bytes));

    ByteStreams.copy(hashingStream, ByteStreams.nullOutputStream());
    return hashingStream;
  }
}