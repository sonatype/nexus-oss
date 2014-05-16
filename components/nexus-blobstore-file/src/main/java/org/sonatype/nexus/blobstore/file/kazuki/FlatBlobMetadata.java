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
package org.sonatype.nexus.blobstore.file.kazuki;

import java.util.Map;

import org.sonatype.nexus.blobstore.file.State;

import org.joda.time.DateTime;

/**
 * A flattened version of blob metadata, suitable for storing in Kazuki KV stores.
 *
 * TODO: Consider getting rid of the duplication with BlobMetadata.
 *
 * @since 3.0
 */
class FlatBlobMetadata
{
  private State state;

  private Map<String, String> headers;

  private DateTime creationTime;

  private String sha1Hash;

  private long contentSize;

  public State getState() {
    return state;
  }

  public void setState(final State state) {
    this.state = state;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(final Map<String, String> headers) {
    this.headers = headers;
  }

  public DateTime getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(final DateTime creationTime) {
    this.creationTime = creationTime;
  }

  public String getSha1Hash() {
    return sha1Hash;
  }

  public void setSha1Hash(final String sha1Hash) {
    this.sha1Hash = sha1Hash;
  }

  public long getContentSize() {
    return contentSize;
  }

  public void setContentSize(final long contentSize) {
    this.contentSize = contentSize;
  }

  @Override
  public String toString() {
    return "FlatBlobMetadata{" +
        ", state=" + state +
        ", headers=" + headers +
        ", creationTime=" + creationTime +
        ", sha1Hash='" + sha1Hash + '\'' +
        ", contentSize=" + contentSize +
        '}';
  }
}
