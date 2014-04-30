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
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreException;
import org.sonatype.nexus.blobstore.api.BlobStoreListener;
import org.sonatype.nexus.blobstore.api.BlobStoreMetrics;
import org.sonatype.nexus.blobstore.support.BlobIdFactory;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A prototype implementation of {@link BlobStore} that writes its data to the file system.
 *
 * @since 3.0
 */
public class FileBlobStore
    implements BlobStore
{
  private static final Logger logger = LoggerFactory.getLogger(FileBlobStore.class);

  private BlobStoreListener listener;

  private BlobIdFactory blobIdFactory;

  private Path dataDirectory;

  private FileLocationPolicy locations;

  private FileOperations fileOperations;

  private HeaderFileFormat headerFormat;

  public FileBlobStore(final Path dataDirectory, BlobStoreListener listener) {
    Preconditions.checkNotNull(dataDirectory);
    this.dataDirectory = dataDirectory;
    this.listener = listener;
  }

  @Override
  public Blob create(final InputStream inputStream, final Map<String, String> headers) {
    Preconditions.checkNotNull(inputStream);
    Preconditions.checkNotNull(headers);

    final BlobId blobId = blobIdFactory.createBlobId();

    // TODO: validate the headers

    try {

      // TODO: This isn't atomic, meaning that content might be stored without headers.
      // Maybe write out a 'remove' order that gets deleted at the end of this method?
      // When the blob store starts, any outstanding remove orders trigger file deletions.

      logger.debug("Writing blob {} to {}", blobId, contentPath(blobId));

      // write the headers to disk
      fileOperations.create(headerPath(blobId), toInputStream(headers));
      // write the content to disk
      System.err.println("Writing blob "+contentPath(blobId));
      fileOperations.create(contentPath(blobId), inputStream);

      final FileBlob blob = new FileBlob(blobId, contentPath(blobId), headerPath(blobId));
      if (listener != null) {
        listener.blobCreated(blob, "Blob " + blobId + " written to " + contentPath(blobId));
      }

      return blob;
    }
    catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Nullable
  @Override
  public Blob get(final BlobId blobId) {
    Preconditions.checkNotNull(blobId);

    if (!fileOperations.exists(contentPath(blobId))) {
      logger.debug("Attempt to access non-existent blob {}", blobId);
      return null;
    }

    logger.debug("Accessing blob {}", blobId);
    final FileBlob blob = new FileBlob(blobId, contentPath(blobId), headerPath(blobId));
    if (listener != null) {
      listener.blobAccessed(blob, null);
    }
    return blob;
  }

  @Override
  public boolean delete(final BlobId blobId) {
    Preconditions.checkNotNull(blobId);

    try {
      final boolean delBlob = fileOperations.delete(contentPath(blobId));
      final boolean delHeader = fileOperations.delete(headerPath(blobId));

      if (delBlob != delHeader) {
        logger.error("Deleting blob {} : {} file was missing.", blobId, !delBlob ? "content" : "header");
      }

      logger.debug("Deleting blob {}", blobId);

      if (listener != null) {
        listener.blobDeleted(blobId, "Path:" + contentPath(blobId).toAbsolutePath());
      }

      return delBlob || delHeader;
    }
    catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public boolean deleteHard(final BlobId blobId) {
    return delete(blobId);
  }

  @Override
  public BlobStoreMetrics getMetrics() {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  private class FileBlob
      implements Blob, BlobMetrics
  {
    private BlobId blobId;

    private Path contentPath;

    private Path headerPath;

    FileBlob(final BlobId blobId, final Path contentPath, final Path headerPath) {
      Preconditions.checkNotNull(blobId);
      Preconditions.checkNotNull(contentPath);
      Preconditions.checkNotNull(headerPath);
      this.blobId = blobId;
      this.contentPath = contentPath;
      this.headerPath = headerPath;
    }

    @Override
    public BlobId getId() {
      return blobId;
    }

    @Override
    public Map<String, String> getHeaders() {
      try (final InputStream inputStream = fileOperations.openInputStream(headerPath)) {
        checkExists(headerPath);
        return headerFormat.read(inputStream);
      }
      catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }

    @Override
    public InputStream getInputStream() {
      try {
        checkExists(contentPath);
        return fileOperations.openInputStream(contentPath);
      }
      catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }

    private void checkExists(final Path path) throws IOException {
      if (!fileOperations.exists(path)) {
        // I'm not completely happy with this, since it means that blob store clients can get a blob, be satisfied
        // that it exists, and then discover that it doesn't, mid-operation
        throw new BlobStoreException("Blob has been deleted.");
      }
    }

    @Override
    public BlobMetrics getMetrics() {
      return this;
    }

    @Override
    public Date getCreationTime() {
      try {
        checkExists(contentPath);
        return fileOperations.fileCreationDate(contentPath);
      }
      catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }

    @Override
    public String getSHA1Hash() {
      try {
        return fileOperations.computeSha1Hash(contentPath);
      }
      catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }

    @Override
    public long getHeaderSize() {
      try {
        return fileOperations.fileSize(headerPath);
      }
      catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }

    @Override
    public long getContentSize() {
      try {
        return fileOperations.fileSize(contentPath);
      }
      catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }

    @Override
    public long getTotalSize() {
      return getHeaderSize() + getContentSize();
    }
  }

  private Path contentPath(final BlobId blobId) {
    return locations.getBlobPath(dataDirectory, blobId);
  }

  private Path headerPath(final BlobId blobId) {
    return locations.getHeaderPath(dataDirectory, blobId);
  }

  private InputStream toInputStream(final Map<String, String> headers) throws IOException {
    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    headerFormat.write(headers, b);
    return new ByteArrayInputStream(b.toByteArray());
  }

  public void setBlobIdFactory(final BlobIdFactory blobIdFactory) {
    this.blobIdFactory = blobIdFactory;
  }

  public void setFileLocationPolicy(final FileLocationPolicy fileLocationPolicy) {
    this.locations = fileLocationPolicy;
  }

  public void setFileOperations(final FileOperations fileOperations) {
    this.fileOperations = fileOperations;
  }

  public void setHeaderFormat(final HeaderFileFormat headerFormat) {
    this.headerFormat = headerFormat;
  }
}
