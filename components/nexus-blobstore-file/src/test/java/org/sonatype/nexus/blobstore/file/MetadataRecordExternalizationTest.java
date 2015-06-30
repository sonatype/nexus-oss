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
package org.sonatype.nexus.blobstore.file;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore.MetadataRecord;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetadataRecordExternalizationTest
{
  @Test
  public void roundTrip() throws Exception {
    final BlobMetadata blobMetadata = new BlobMetadata(BlobState.CREATING, ImmutableMap.of("Hi", "mom"));
    blobMetadata.setMetrics(new BlobMetrics(new DateTime(), "pretend hash", 33434));

    roundTrip(blobMetadata);
  }

  @Test
  public void roundTripWithEmptyObject() throws Exception {
    final Map<String, String> headers = new HashMap<>();
    headers.put(null, null);

    final BlobMetadata blobMetadata = new BlobMetadata(BlobState.CREATING, headers);
    blobMetadata.setMetrics(new BlobMetrics(null, null, 0));

    roundTrip(blobMetadata);
  }

  private void roundTrip(final BlobMetadata blobMetadata) throws IOException, ClassNotFoundException {
    MetadataRecord metadata = new MetadataRecord(blobMetadata);

    final byte[] bytes = externalize(metadata);

    final MetadataRecord roundTripMetadata = readFrom(bytes);

    assertThat(roundTripMetadata, Matchers.is(equalTo(metadata)));
  }

  private byte[] externalize(MetadataRecord metadata) throws IOException {
    final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    try (ObjectOutputStream objectsOut = new ObjectOutputStream(bytesOut)) {
      metadata.writeExternal(objectsOut);
      objectsOut.flush();
      return bytesOut.toByteArray();
    }
  }

  private MetadataRecord readFrom(final byte[] bytes) throws IOException, ClassNotFoundException {
    final ObjectInputStream objectsIn = new ObjectInputStream(new ByteArrayInputStream(bytes));

    final MetadataRecord roundTripMetadata = new MetadataRecord();
    roundTripMetadata.readExternal(objectsIn);

    return roundTripMetadata;
  }


}
