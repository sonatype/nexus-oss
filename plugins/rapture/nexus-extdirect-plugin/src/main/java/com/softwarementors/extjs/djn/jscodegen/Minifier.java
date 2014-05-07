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
package com.softwarementors.extjs.djn.jscodegen;

import com.softwarementors.extjs.djn.StringUtils;

/**
 * This is an override of original Minifier code, that will not do any minification, or order to get rid of
 * yui compressor / rhino.
 */
public class Minifier
{

  public static String getMinifiedFileName(String file) {
    return file.replace(".js", "-min.js");
  }

  public static final String minify( String input, String inputFilename, int debugCodeLength ) {
    throw new IllegalStateException("Minification is not supposed to be used");
  }

}
