/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
import java.util.Set;

import org.sonatype.nexus.proxy.item.ContentLocator;

/**
 * A utility component for working with MIME type detection, either "without touching" (the content), that is
 * "best effort guess" based, or doing proper MIME type detection based on MIME magic database. This component
 * supersedes the {@link MimeUtil} one that is completely deprecated and should be avoided.
 *
 * @author cstamas
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
   * Makes a "guess" (usually based on file extension) about all the applicable MIME types for the given path. The
   * "guess" is fast, but not so precise as detection, where content is needed.
   *
   * @param path to guess for.
   * @return the set of applicable mime types.
   */
  Set<String> guessMimeTypesFromPath(String path);

  /**
   * Performs a real MIME type detection by matching the "magic bytes" of a content to a known database. Is the most
   * precise way for detection but is costly since it does IO (reads several bytes up from content to perform
   * matching).
   *
   * @param content to perform MIME magic matching against.
   * @return all of the applicable MIME types in relevance order (best fit first).
   * @throws IOException in case of IO problems.
   */
  Set<String> detectMimeTypesFromContent(ContentLocator content)
      throws IOException;
}
