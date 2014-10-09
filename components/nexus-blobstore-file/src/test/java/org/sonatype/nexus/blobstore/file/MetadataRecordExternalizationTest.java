package org.sonatype.nexus.blobstore.file;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore.MetadataRecord;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MetadataRecordExternalizationTest
{
  @Test
  public void roundTrip() throws Exception {

    final BlobMetadata blobMetadata = new BlobMetadata(BlobState.CREATING, ImmutableMap.of("Hi", "mom"));
    blobMetadata.setMetrics(new BlobMetrics(new DateTime(), "pretend hash", 33434));

    final MetadataRecord metadata = new MetadataRecord(blobMetadata);

    final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    final ObjectOutputStream objectsOut = new ObjectOutputStream(bytesOut);

    metadata.writeExternal(objectsOut);

    objectsOut.close();

    final ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
    final ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);

    final MetadataRecord roundTripMetadata = new MetadataRecord();
    roundTripMetadata.readExternal(objectsIn);

    assertThat(roundTripMetadata.toString(), is(equalTo(metadata.toString())));
  }
}
