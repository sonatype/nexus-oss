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
package org.sonatype.nexus.repository.docker.internal;

import org.sonatype.nexus.repository.http.HttpStatus;

/**
 * Thrown in case of an Docker v1 protocol violation.
 *
 * @since 3.0
 */
public class V1Exception
    extends RuntimeException
{
  private final int httpCode;

  private V1Exception(final String message) {
    this(message, HttpStatus.BAD_REQUEST);
  }

  private V1Exception(final String message, final int httpCode) {
    super(message);
    this.httpCode = httpCode;
  }

  public int getHttpCode() {
    return httpCode;
  }

  static class ImagesNotFound
      extends V1Exception
  {
    public ImagesNotFound() {
      super("images not found", HttpStatus.NOT_FOUND);
    }
  }

  static class TagNotFound
      extends V1Exception
  {
    public TagNotFound() {
      super("Tag not found", HttpStatus.NOT_FOUND);
    }
  }

  static class ImageNotFound
      extends V1Exception
  {
    public ImageNotFound() {
      super("Image not found", HttpStatus.NOT_FOUND);
    }
  }

  static class ImageAlreadyExists
      extends V1Exception
  {
    public ImageAlreadyExists() {
      this("Image already exists");
    }

    public ImageAlreadyExists(final String message) {
      super(message, HttpStatus.CONFLICT);
    }
  }

  static class InvalidLayerMetadata
      extends V1Exception
  {
    public InvalidLayerMetadata(final String message) {
      super(message);
    }
  }

  static class InvalidImageId
      extends V1Exception
  {
    public InvalidImageId() {
      super("Invalid image ID", HttpStatus.NOT_FOUND);
    }
  }

  static class InvalidTagName
      extends V1Exception
  {
    public InvalidTagName() {
      super("Invalid tag name (must match '^[\\w][\\w.-]{0,127}$')");
    }
  }

  static class InvalidChecksum
      extends V1Exception
  {
    public InvalidChecksum(final String message) {
      super(message);
    }
  }

  static class InvalidJson
      extends V1Exception
  {
    public InvalidJson() {
      super("Invalid JSON");
    }

    public InvalidJson(final String message) {
      super(message);
    }
  }

  static class OldDocker
      extends V1Exception
  {
    public OldDocker() {
      super("This endpoint does not support Docker daemons older than 0.10", HttpStatus.PRECONDITION_FAILED);
    }

    public OldDocker(final String message) {
      super(
          message + ". If you are using the Docker daemon, you should upgrade to version 0.10 or later",
          HttpStatus.PRECONDITION_FAILED
      );
    }
  }

}
