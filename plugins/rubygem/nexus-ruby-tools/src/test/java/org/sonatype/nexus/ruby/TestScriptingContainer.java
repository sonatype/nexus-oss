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
package org.sonatype.nexus.ruby;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jruby.embed.ScriptingContainer;

class TestScriptingContainer
    extends ScriptingContainer
{
  public TestScriptingContainer() {
    this(null,
        new File("target/test-classes/rubygems").getAbsolutePath(),
        new File("target/test-classes/it/Gemfile").getAbsolutePath());
  }

  public TestScriptingContainer(String userHome, String rubygems, String gemfile) {
    Map<String, String> env = new HashMap<String, String>();

    env.put("GEM_HOME", rubygems);
    env.put("GEM_PATH", rubygems);

    if (gemfile != null) {
      env.put("BUNDLE_GEMFILE", gemfile);
    }

    if (userHome != null) {
      env.put("HOME", userHome); // gem push needs it to find .gem/credentials
    }

    env.put("PATH", ""); // bundler needs a PATH set
    env.put("DEBUG", "true");
    setEnvironment(env);
  }
}