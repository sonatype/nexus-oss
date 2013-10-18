/**
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

package org.sonatype.nexus.atlas

import groovy.transform.ToString
import org.sonatype.nexus.atlas.SupportZipGenerator.Request
import org.sonatype.nexus.atlas.SupportZipGenerator.Result

/**
 * Generates a support zip file.
 *
 * @since 2.7
 */
interface SupportZipGenerator
{
  @ToString(includeNames=true)
  static class Request
  {
    boolean applicationProperties

    boolean threadDump

    boolean configurationFiles

    boolean securityFiles

    boolean logFiles

    boolean limitSize
  }

  // TODO: Drop if we only need to return a File
  @ToString(includeNames=true)
  static class Result
  {
    File zipFile
  }

  /**
   * Generate a support-zip for the given request.
   */
  Result generate(Request request)
}