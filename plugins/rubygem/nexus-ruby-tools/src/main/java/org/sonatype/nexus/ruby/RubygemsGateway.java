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

import org.jruby.runtime.builtin.IRubyObject;

public interface RubygemsGateway
{
  void recreateRubygemsIndex(String directory);

  void purgeBrokenDepencencyFiles(String directory);

  void purgeBrokenGemspecFiles(String directory);

  InputStream emptyIndex();

  InputStream addSpec(IRubyObject spec, InputStream specsDump, SpecsIndexType type);

  InputStream deleteSpec(IRubyObject spec, InputStream specsDump);

  InputStream deleteSpec(IRubyObject spec, InputStream specsIndex, InputStream refSpecs);

  InputStream mergeSpecs(List<InputStream> streams, boolean latest);

  DependencyData dependencies(InputStream inputStream, String name, long modified);

  List<String> listAllVersions(String name, InputStream inputStream, long modified, boolean prerelease);

  /**
   * create a new instance of <code>GemspecHelper</code>
   * @param the stream to the rzipped marshalled Gem::Specification ruby-object
   * @return an empty GemspecHelper
   */
  GemspecHelper newGemspecHelper(InputStream gemspec);

  /**
   * create a new instance of <code>GemspecHelper</code>
   * @param the stream to the from which the gemspec gets extracted
   * @return an empty GemspecHelper
   */
  GemspecHelper newGemspecHelperFromGem(InputStream gem);

  /**
   * create a new instance of <code>DependencyHelper</code>
   * @return an empty DependencyHelper
   */
  DependencyHelper newDependencyHelper();
}
