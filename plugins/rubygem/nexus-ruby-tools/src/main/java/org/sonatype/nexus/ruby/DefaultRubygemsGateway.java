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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.osgi.OSGiScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.osgi.framework.FrameworkUtil;

public class DefaultRubygemsGateway
    extends ScriptWrapper
    implements RubygemsGateway
{
  private static ScriptingContainer newScriptingContainer() {
    ScriptingContainer container;
    try {
      container = new OSGiScriptingContainer(FrameworkUtil.getBundle(DefaultRubygemsGateway.class));
    }
    catch (Throwable e) {
      container = new ScriptingContainer();
    }
    // set the right classloader
    container.setClassLoader(DefaultRubygemsGateway.class.getClassLoader());

    return container;
  }

  public DefaultRubygemsGateway() {
    this(newScriptingContainer());
  }

  public DefaultRubygemsGateway(ScriptingContainer container) {
    super(container);
  }

  protected Object newScript() {
    IRubyObject nexusRubygemsClass = scriptingContainer.parse(PathType.CLASSPATH, "nexus/rubygems.rb").run();
    return scriptingContainer.callMethod(nexusRubygemsClass, "new", Object.class);
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

  @Override
  public Object spec(InputStream gem) {
    return callMethod("spec_get", gem, Object.class);
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream addSpec(Object spec, InputStream specsIndex, SpecsIndexType type) {
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
  public InputStream deleteSpec(Object spec, InputStream specsIndex) {
    return deleteSpec(spec, specsIndex, null);
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream deleteSpec(Object spec, InputStream specsIndex, InputStream releasesSpecs) {
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

  @Override
  public Map<String, InputStream> splitDependencies(InputStream deps) {
    @SuppressWarnings("unchecked")
    Map<String, List<Long>> map = (Map<String, List<Long>>) callMethod("split_dependencies",
        deps,
        Map.class);

    Map<String, InputStream> result = new HashMap<>();
    for (Map.Entry<String, List<Long>> entry : map.entrySet()) {
      result.put(entry.getKey(), new ByteArrayInputStream(entry.getValue()));
    }
    return result;
  }

  @Override
  public InputStream mergeDependencies(List<InputStream> deps) {
    return mergeDependencies(deps, false);
  }

  @Override
  public InputStream mergeDependencies(List<InputStream> deps, boolean unique) {
    Object[] args = new Object[deps.size() + 1];
    args[0] = unique;
    int index = 1;
    for (InputStream is : deps) {
      args[index++] = is;
    }
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("merge_dependencies",
        args,
        List.class);

    return array == null ? null : new ByteArrayInputStream(array);
  }

  @Override
  public InputStream createDependencies(List<InputStream> gemspecs) {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) (gemspecs.size() == 0 ?
        callMethod("create_dependencies",
            List.class) :
        callMethod("create_dependencies",
            gemspecs.toArray(),
            List.class));

    return array == null ? null : new ByteArrayInputStream(array);
  }

  @Override
  public String pom(InputStream specRz, boolean snapshot) {
    return callMethod("to_pom", new Object[]{specRz, snapshot}, String.class);
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
  public ByteArrayInputStream createGemspecRz(Object spec) {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("create_quick",
        new Object[]{spec},
        List.class);

    return new ByteArrayInputStream(array);
  }

  @Override
  public String filename(Object spec) {
    return scriptingContainer.callMethod(spec, "file_name", String.class);
  }

  @Override
  public String name(Object spec) {
    return scriptingContainer.callMethod(spec, "name", String.class);
  }
}
