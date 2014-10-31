/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype
 * .com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License
 * Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are
 * trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.trials.ruby.shoes4

import org.sonatype.nexus.client.core.NexusClient
import org.sonatype.nexus.testsuite.trials.ruby.RubyScenarioSupport
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers

import static org.hamcrest.MatcherAssert.assertThat

/**
 * The "Shoes4 roundtrip" scenario for Rubygems.
 *
 * <ul>
 * <li>have Git on the path</li>
 * </ul>
 * TODO: This is unfinished, versions will change but now are hardwired!
 */
class Shoes4Scenario
    extends RubyScenarioSupport
{
  public Shoes4Scenario(String id, File workdir, NexusClient nexusClient) {
    super(id, workdir, nexusClient)
  }

  @Override
  public void perform() {
    // clone shoes4
    exec(['git', 'clone', 'git@github.com:shoes/shoes4.git'])
    // cd into it
    cd 'shoes4'
    // invoke bundle install
    bundle(['install'])
    // invoke rake build:all to have Gems built
    exec(['rake', 'build:all'])
    // gems are in pkg/
    cd 'pkg'
    // deploy them
    // TODO: version!
    nexus 'shoes-dsl-4.0.0.pre2.gem'
    nexus 'shoes-swt-4.0.0.pre2.gem'
    nexus 'shoes-4.0.0.pre2.gem'
    // install them
    gem(['install', 'shoes'])

    // TODO: version!
    assertThat(lastOutFile, FileMatchers.contains('Successfully installed shoes-4.0.0.pre2'))
  }
}
