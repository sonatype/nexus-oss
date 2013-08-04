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

package org.sonatype.nexus.error.reporting.bundle;

import com.thoughtworks.xstream.XStream;

public class AbstractXmlAssembler
{
  /**
   * XStream is used for a deep clone (TODO: not sure if this is a great idea)
   */
  private static XStream xstream = new XStream();

  protected static final String PASSWORD_MASK = "*****";

  protected Object cloneViaXml(Object configuration) {
    if (configuration == null) {
      return null;
    }

    return xstream.fromXML(xstream.toXML(configuration));
  }
}
