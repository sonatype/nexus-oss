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
package org.sonatype.nexus.extender.modules;

import org.sonatype.nexus.web.SecurityFilter;
import org.sonatype.nexus.web.TemplateRenderer;
import org.sonatype.security.SecuritySystem;

import com.google.inject.AbstractModule;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;

/**
 * Miscellaneous plugin bindings (to be reviewed/culled).
 * 
 * @since 3.0
 */
public class MiscellaneousModule
    extends AbstractModule
{
  // handle some edge-cases for commonly used servlet-based components which need a bit more configuration
  // so that sisu/guice can find the correct bindings inside of plugins

  @Override
  protected void configure() {
    bind(SecurityFilter.class);

    requireBinding(SecuritySystem.class);
    requireBinding(FilterChainResolver.class);
    requireBinding(TemplateRenderer.class);
  }
}
