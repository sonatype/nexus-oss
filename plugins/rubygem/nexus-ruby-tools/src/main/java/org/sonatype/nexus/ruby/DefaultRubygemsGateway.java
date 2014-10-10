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

import java.io.InputStream;
import java.util.List;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

public class DefaultRubygemsGateway
    extends ScriptWrapper
    implements RubygemsGateway
{
  /**
   * Ctor that accepts prepared scripting container, as usually the container is or should be
   * managed (ie. by calling {@link ScriptingContainer#terminate()} when application is shut down.
   */
  public DefaultRubygemsGateway(ScriptingContainer container) {
    super(container);
  }

  protected Object newScript() {
    IRubyObject nexusRubygemsClass = scriptingContainer.parse(PathType.CLASSPATH, "nexus/rubygems.rb").run();
    return scriptingContainer.callMethod(nexusRubygemsClass, "new", Object.class);
  }

  @Override
  public DependencyHelper newDependencyHelper() {
    return callMethod("new_dependency_helper", DependencyHelper.class);
  }

  @Override
  public SpecsHelper newSpecsHelper() {
    return callMethod("new_specs_helper", SpecsHelper.class);
  }

  @Override
  public MergeSpecsHelper newMergeSpecsHelper() {
    return callMethod("new_merge_specs_helper", MergeSpecsHelper.class);
  }

  @Override
  public DependencyData dependencies(InputStream is, String name, long modified) {
    return new DependencyDataImpl(scriptingContainer,
        callMethod("dependencies", new Object[]{name, is}, Object.class),
        modified);
  }

  @Override
  public void recreateRubygemsIndex(String directory) {
    callMethod("recreate_rubygems_index", directory, Void.class);
  }

  @Override
  public void purgeBrokenDepencencyFiles(String directory) {
    callMethod("purge_broken_depencency_files", directory, Void.class);
  }

  @Override
  public void purgeBrokenGemspecFiles(String directory) {
    callMethod("purge_broken_gemspec_files", directory, Void.class);
  }

  @Override
  public GemspecHelper newGemspecHelper(InputStream gemspec) {
    return callMethod("new_gemspec_helper", gemspec, GemspecHelper.class);
  }

  @Override
  public GemspecHelper newGemspecHelperFromGem(InputStream gem) {
    return callMethod("new_gemspec_helper_from_gem", gem, GemspecHelper.class);
  }
}
