/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.lvo;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Usually Nexus exports /static/... URLs by default, but only for nexus-plugin packaging.
 * LVO is called plugin, but it's not (packaging=jar).
 */
@Component(role = NexusResourceBundle.class, hint = "LvoResourceBundle")
public class LvoResourceBundle
    extends AbstractNexusResourceBundle
{
  @Override
  public List<StaticResource> getContributedResouces() {
    List<StaticResource> result = new ArrayList<StaticResource>();

    // explicitely add /static/js resource
    result.add(new DefaultStaticResource(getClass().getResource("/static/js/nexus-lvo-plugin-all.js"),
        "/static/js/nexus-lvo-plugin-all.js", "application/x-javascript"));

    return result;
  }
}
