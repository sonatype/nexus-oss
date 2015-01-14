package org.sonatype.nexus.blobstore.api;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @since 3.0
 */
public class BlobRefTest
{
  @Test
  public void testToString() {
    final BlobRef blobRef = new BlobRef("node", "store", "blobId");
    final String spec = blobRef.toString();
    final BlobRef reconstituted = BlobRef.parse(spec);

    assertThat(reconstituted, is(equalTo(blobRef)));
  }
}
