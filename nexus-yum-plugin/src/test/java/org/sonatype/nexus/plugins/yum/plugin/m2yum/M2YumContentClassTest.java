/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
 package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

import com.sonatype.nexus.staging.api.AbstractStagingProfilePlexusResource;

public class M2YumContentClassTest {

  @Test
  public void shouldBeCompatibleToM2() throws Exception {
    ContentClass m2yum = new M2YumContentClass();
    assertThat(m2yum.isCompatible(new M2YumContentClass()), is(true));
    assertThat(m2yum.isCompatible(new Maven2ContentClass()), is(true));
  }

  @Test
  public void shouldDetectStagingRepoValidation() throws Exception {
    final AbstractStagingProfilePlexusResource resource = new AbstractStagingProfilePlexusResource();
    assertThat(resource.validateProfile(), is("maven2"));
  }
}
