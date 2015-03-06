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
package org.sonatype.nexus.repository.maven.internal;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.maven.internal.policy.RedeployPolicy;
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named
public class MavenFacetImpl
    extends FacetSupport
    implements MavenFacet
{
  public static final String CONFIG_KEY = "maven";

  private final Map<String, PathParser> pathParsers;

  private VersionPolicy versionPolicy;

  private RedeployPolicy redeployPolicy;

  private PathParser pathParser;

  @Inject
  public MavenFacetImpl(final Map<String, PathParser> pathParsers) {
    this.pathParsers = checkNotNull(pathParsers);
  }

  @Override
  protected void doConfigure() throws Exception {
    super.doConfigure();
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    this.versionPolicy = VersionPolicy.valueOf(attributes.require("repositoryPolicy", String.class));
    this.redeployPolicy = RedeployPolicy.valueOf(attributes.require("redeployPolicy", String.class));
    this.pathParser = checkNotNull(pathParsers.get(getRepository().getFormat().getValue()),
        "No pathParser found for %s", getRepository().getFormat().getValue());
  }

  @Override
  public VersionPolicy getVersionPolicy() {
    return versionPolicy;
  }

  @Override
  public RedeployPolicy getRedeployPolicy() {
    return redeployPolicy;
  }

  @Override
  public PathParser getPathParser() {
    return pathParser;
  }
}
