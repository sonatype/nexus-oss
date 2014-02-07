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

package org.sonatype.nexus.proxy.attributes;

/**
 * A component that filters Attributes for usage in cases like representing it on UI or any other "external" usage. Main
 * idea is to prevent possible "leaks" of sensitive information from attributes, that might be presented to end user on
 * UI or any other surface.
 *
 * @since 2.7.2
 */
public interface AttributesFilter
{
  /**
   * Method performing the filtering/altering of attributes. The {@link Attributes} instance being passed in should
   * be modified in any way that suits the purpose of filtering. The parameter is never {@code null}.
   */
  void filterAttributes(Attributes attributes);
}
