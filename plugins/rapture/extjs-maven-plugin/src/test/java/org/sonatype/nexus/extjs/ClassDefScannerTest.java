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
package org.sonatype.nexus.extjs;

import java.io.File;
import java.util.List;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ClassDefScanner}.
 */
public class ClassDefScannerTest
    extends TestSupport
{
  private ClassDefScanner scanner;

  @Before
  public void setUp() throws Exception {
    this.scanner = new ClassDefScanner();
  }

  @Test
  public void basicOrder() throws Exception {
    File basedir = util.resolveFile("src/test/resources/basic");
    DirectoryScanner files = new DirectoryScanner();
    files.setBasedir(basedir);
    files.setIncludes(new String[]{"**/*.js"});

    List<ClassDef> classes = scanner.scan(files);
    log("Classes:");
    for (ClassDef def : classes) {
      log("  {}", def.getName());
      for (String dep : def.getDependencies()) {
        log("    + {}", dep);
      }
    }

    assertThat(classes, hasSize(3));

    assertThat(classes.get(0).getName(), is("Baz"));
    assertThat(classes.get(1).getName(), is("Bar"));
    assertThat(classes.get(2).getName(), is("Foo"));

    assertThat(classes.get(1).getDependencies(), contains("Baz"));
    assertThat(classes.get(2).getDependencies(), contains("Bar"));
  }
}
