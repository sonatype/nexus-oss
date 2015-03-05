/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.maven.internal.maven2;

import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.M2GavCalculator;
import org.sonatype.nexus.repository.maven.internal.PathParser;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Maven 2 path parser.
 *
 * @since 3.0
 */
@Named(Maven2Format.NAME)
@Singleton
public class Maven2PathParser
    implements PathParser
{
  // TODO: TokenParser not capable to do this (yet)
  // TODO: this is legacy, but here only to make it work
  // TODO: consider moving/updating/fleshing out it from nx2 codebase, or improve TokenParser
  private final M2GavCalculator m2GavCalculator = new M2GavCalculator();

  @Override
  public Map<String, String> parsePath(final String path) {
    final Gav gav = m2GavCalculator.pathToGav(path);
    if (gav == null) {
      return null;
    }
    Map<String, String> coordinates = Maps.newHashMap();
    coordinates.put(GROUP_ID, gav.getGroupId());
    coordinates.put(ARTIFACT_ID, gav.getArtifactId());
    coordinates.put(VERSION, gav.getVersion());
    coordinates.put(BASE_VERSION, gav.getBaseVersion());
    if (!Strings.isNullOrEmpty(gav.getClassifier())) {
      coordinates.put(CLASSIFIER, gav.getClassifier());
    }
    coordinates.put(EXTENSION, gav.getExtension());
    return coordinates;
  }

  @Override
  public String assemblePath(final Map<String, String> coordinates) {
    // TODO: currenrtly not used, might drop?
    throw new RuntimeException("not implemented");
  }
}
