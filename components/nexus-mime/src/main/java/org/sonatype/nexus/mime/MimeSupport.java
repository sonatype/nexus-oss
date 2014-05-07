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
package org.sonatype.nexus.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A utility component for working with MIME type detection, either "without touching" (the content), that is
 * "best effort guess" based, or doing proper MIME type detection based on MIME magic database.
 *
 * @since 2.0
 */
public interface MimeSupport
{
  /**
   * Makes a "guess" (usually based on file extension) about the MIME type that is most applicable to the given path
   * taking into consideration the requester MimeRulesSource MIME rules. When no "hard rule" present from
   * {@link MimeRulesSource}, this method falls back to {@link #guessMimeTypeFromPath(String)}. The "guess" is fast,
   * but not so precise as detection, where content is needed. This method should be used whenever a MIME type of a
   * file item that is contained <b>within</b> given context that has MimeRulesSource is about to be guessed.
   *
   * @param path to guess for.
   * @return the most applicable MIME type as String.
   */
  String guessMimeTypeFromPath(MimeRulesSource mimeRulesSource, String path);

  /**
   * Makes a "guess" (usually based on file extension) about the MIME type that is most applicable to the given path.
   * The "guess" is fast, but not so precise as detection, where content is needed.
   *
   * @param path to guess for.
   * @return the most applicable MIME type as String.
   */
  String guessMimeTypeFromPath(String path);

  /**
   * Makes a "guess" (based on file name and/or extension) about all the applicable MIME types for the given path. The
   * "guess" is fast, but not so precise as detection, where content is needed. The list of mime types are in
   * descending order (most specific 1st, least specific last).
   *
   * @param path to guess for.
   * @return the list of applicable mime types.
   * @since 2.8
   */
  List<String> guessMimeTypesListFromPath(final String path);

  /**
   * @since 3.0
   */
  List<String> detectMimeTypes(InputStream input, @Nullable String fileName) throws IOException;

  /**
   * @since 3.0
   * @see #detectMimeTypes(InputStream, String)
   */
  String detectMimeType(InputStream input, @Nullable String fileName) throws IOException;
}
