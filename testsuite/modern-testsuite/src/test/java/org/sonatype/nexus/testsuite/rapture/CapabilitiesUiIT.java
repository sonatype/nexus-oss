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
package org.sonatype.nexus.testsuite.rapture;

import org.junit.Test;

/**
 * @since 2.8
 */
public class CapabilitiesUiIT
    extends SiestaStandardUiITSupport
{

  public CapabilitiesUiIT(final String executable, final String[] options) {
    super(executable, options);
  }

  @Test
  public void capability_directjengine() throws Exception {
    run("tests/capability/direct.js");
  }

  @Test
  public void capability_list() throws Exception {
    run("tests/capability/list.js");
  }

  @Test
  public void capability_sanity() throws Exception {
    run("tests/capability/sanity.js");
  }

}
