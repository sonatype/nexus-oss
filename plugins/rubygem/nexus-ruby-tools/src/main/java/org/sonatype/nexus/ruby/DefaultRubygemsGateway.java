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
  public DependencyData dependencies(InputStream is, String name, long modified) {
    return new DependencyDataImpl(scriptingContainer,
        callMethod("dependencies", new Object[]{name, is}, Object.class),
        modified);
 }

  @Override
  public InputStream emptyIndex() {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("empty_specs", List.class);

    return new ByteArrayInputStream(array);
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream addSpec(IRubyObject spec, InputStream specsIndex, SpecsIndexType type) {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("add_spec",
        new Object[]{
            spec,
            specsIndex,
            type.name().toLowerCase()
        },
        List.class);

    return array == null ? null : new ByteArrayInputStream(array);
  }

  @Override
  public InputStream deleteSpec(IRubyObject spec, InputStream specsIndex) {
    return deleteSpec(spec, specsIndex, null);
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream deleteSpec(IRubyObject spec, InputStream specsIndex, InputStream releasesSpecs) {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("delete_spec",
        new Object[]{
            spec,
            specsIndex,
            releasesSpecs
        },
        List.class);

    return array == null ? null : new ByteArrayInputStream(array);
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream mergeSpecs(List<InputStream> streams, boolean latest) {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("merge_specs",
        new Object[]{
            streams,
            latest
        },
        List.class);

    return array == null ? null : new ByteArrayInputStream(array);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized List<String> listAllVersions(String name,
                                                   InputStream inputStream,
                                                   long modified,
                                                   boolean prerelease) {
    return (List<String>) callMethod("list_all_versions",
        new Object[]{
            name,
            inputStream,
            modified,
            prerelease
        },
        List.class);
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
