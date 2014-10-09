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
import java.util.Map;

public interface RubygemsGateway
{
  void recreateRubygemsIndex(String directory);

  void purgeBrokenDepencencyFiles(String directory);

  void purgeBrokenGemspecFiles(String directory);

  ByteArrayInputStream createGemspecRz(Object spec);

  InputStream emptyIndex();

  Object spec(InputStream gem);

  String pom(InputStream specRz, boolean snapshot);

  InputStream addSpec(Object spec, InputStream specsDump, SpecsIndexType type);

  InputStream deleteSpec(Object spec, InputStream specsDump);

  InputStream deleteSpec(Object spec, InputStream specsIndex, InputStream refSpecs);

  InputStream mergeSpecs(List<InputStream> streams, boolean latest);

  Map<String, InputStream> splitDependencies(InputStream bundlerResult);

  InputStream mergeDependencies(List<InputStream> deps);

  InputStream mergeDependencies(List<InputStream> deps, boolean unique);

  InputStream createDependencies(List<InputStream> gemspecs);

  String filename(Object spec);

  String name(Object spec);

  DependencyData dependencies(InputStream inputStream, String name, long modified);

  List<String> listAllVersions(String name, InputStream inputStream, long modified, boolean prerelease);
}
