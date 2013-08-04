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

package org.sonatype.appcontext;

/**
 * Thrown when some fatal exception happens during interpolation, like cycle detected in expressions.
 *
 * @author cstamas
 * @since 3.0
 */
public class AppContextInterpolationException
    extends AppContextException
{
  private static final long serialVersionUID = 7958491320532121743L;

  /**
   * @param message
   */
  public AppContextInterpolationException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public AppContextInterpolationException(String message, Throwable cause) {
    super(message, cause);
  }
}
