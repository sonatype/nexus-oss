/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.support.UuidBlobIdFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;

/**
 * Integration test for the file blob store. As the design becomes clearer, I expect to move functionality out of this
 * into unit tests.
 *
 * @since 3.0
 */
public class FileBlobStoreIT
{
  private FileBlobStore blobStore;

  private byte[] tinyBlob = new byte[100];

  @Before
  public void setUp() throws Exception {
    final Path fileBlobStore = Files.createTempDirectory("fileBlobStore");
    blobStore = new FileBlobStore(fileBlobStore, null);

    // Similarly, this will be replaced with mocking, or in a final IT, auto-injection
    blobStore.setBlobIdFactory(new UuidBlobIdFactory());
    blobStore.setFileLocationPolicy(new FlatDirectoryLocationPolicy());
    blobStore.setHeaderFormat(new JsonHeaderFormat());
    blobStore.setFileOperations(new SimpleFileOperations());

    new Random().nextBytes(tinyBlob);
  }

  @Test
  public void headersSurviveRoundTrip() throws Exception {

    final InputStream data = new ByteArrayInputStream(tinyBlob);
    final Map<String, String> headers = ImmutableMap.of("name", "tinyblob", "originalFilename", "image.jpg");

    final BlobId blobId = blobStore.create(data, headers).getId();

    final Blob blob = blobStore.get(blobId);
    final @Nullable Map<String, String> retrievedHeaders = blob.getHeaders();

    assertThat("headers", retrievedHeaders, equalTo(headers));
  }

  @Test
  public void blobContentSurvivesRoundTrip() throws Exception {

    final InputStream data = new ByteArrayInputStream(tinyBlob);
    final Map<String, String> headers = ImmutableMap.of("name", "tinyblob");

    final BlobId id = blobStore.create(data, headers).getId();
    final Blob blob = blobStore.get(id);

    assertThat("foo", tinyBlob, equalTo(extractContentBytes(blob)));
  }

  @Test
  public void metricsAreCorrect() throws Exception {
    final InputStream data = new ByteArrayInputStream(tinyBlob);
    final Map<String, String> headers = ImmutableMap.of("name", "tinyblob");

    final Blob blob = blobStore.create(data, headers);

    final BlobMetrics metrics = blob.getMetrics();

    assertThat("content fileSize", metrics.getContentSize(), equalTo((long) tinyBlob.length));
    assertThat("totals fileSize", metrics.getTotalSize(), equalTo(metrics.getHeaderSize() + tinyBlob.length));


    final long oneHourAgo = System.currentTimeMillis() - 3600 * 1000;
    assertThat("creation date", metrics.getCreationTime().getTime(), greaterThan(oneHourAgo));
  }

  @Test
  public void deletedBlobsAreUnavailable() throws Exception {
    final ByteArrayInputStream data = new ByteArrayInputStream(tinyBlob);
    final Map<String, String> headers = ImmutableMap.of("name", "tinyblob");

    final BlobId id = blobStore.create(data, headers).getId();

    assertThat(blobStore.delete(id), equalTo(true));
    assertThat(blobStore.get(id), nullValue());
  }

  @Test
  public void deletingNonexistentBlob() throws Exception {
    final BlobId fakeBlobId = new BlobId("fake");

    assertThat(blobStore.delete(fakeBlobId), equalTo(false));
  }

  private byte[] extractContentBytes(final Blob blob) throws IOException {
    try (InputStream storedData = blob.getInputStream();
         ByteArrayOutputStream b = new ByteArrayOutputStream()) {
      ByteStreams.copy(storedData, b);
      return b.toByteArray();
    }
  }
}
