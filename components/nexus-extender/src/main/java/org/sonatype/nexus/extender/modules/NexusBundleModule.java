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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.guice.AbstractInterceptorModule;
import org.sonatype.nexus.guice.NexusTypeBinder;

import com.google.common.base.Strings;
import com.google.inject.Key;
import com.google.inject.Module;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.BundleModule;
import org.eclipse.sisu.plexus.PlexusSpaceModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.eclipse.sisu.wire.ParameterKeys;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Nexus specific {@link BundleModule} that uses bundle imports to decide what to install.
 * 
 * @since 3.0
 */
public class NexusBundleModule
    extends BundleModule
{
  private static final ShiroAopModule shiroAopModule = new ShiroAopModule();

  private static final SecurityFilterModule securityFilterModule = new SecurityFilterModule();

  private static final InstrumentationModule instrumentationModule = new InstrumentationModule();

  private static final ValidationModule validationModule = new ValidationModule();

  private static final WebResourcesModule webResourcesModule = new WebResourcesModule();

  private static final RankingModule rankingModule = new RankingModule();

  private final List<AbstractInterceptorModule> interceptorModules;

  private final String imports;

  private final boolean hasPlexus;

  public NexusBundleModule(final Bundle bundle, final MutableBeanLocator locator) {
    super(bundle, locator);

    interceptorModules = new EntryListAdapter<>(locator.locate(Key.get(AbstractInterceptorModule.class)));
    imports = Strings.nullToEmpty(bundle.getHeaders().get(Constants.IMPORT_PACKAGE));
    hasPlexus = bundle.getResource("META-INF/plexus/components.xml") != null;
  }

  @Override
  protected List<Module> modules() {
    List<Module> modules = new ArrayList<>();

    maybeAddShiroAOP(modules);
    maybeAddSecurityFilter(modules);
    maybeAddInstrumentation(modules);
    maybeAddValidation(modules);
    maybeAddWebResources(modules);
    maybeAddInterceptors(modules);
    modules.addAll(super.modules());
    modules.add(rankingModule);

    return modules;
  }

  @Override
  protected Map<?, ?> getProperties() {
    return locator.locate(ParameterKeys.PROPERTIES).iterator().next().getValue();
  }

  @Override
  protected Module spaceModule() {
    if (hasPlexus) {
      return new PlexusSpaceModule(space, BeanScanning.GLOBAL_INDEX);
    }
    return new SpaceModule(space, BeanScanning.GLOBAL_INDEX).with(NexusTypeBinder.STRATEGY);
  }

  private void maybeAddShiroAOP(List<Module> modules) {
    if (imports.contains("org.apache.shiro.authz.annotation")) {
      modules.add(shiroAopModule);
    }
  }

  private void maybeAddSecurityFilter(List<Module> modules) {
    if (imports.contains("org.sonatype.nexus.web")) {
      modules.add(securityFilterModule);
    }
  }

  private void maybeAddInstrumentation(List<Module> modules) {
    if (imports.contains("com.codahale.metrics.annotation")) {
      modules.add(instrumentationModule);
    }
  }

  private void maybeAddValidation(List<Module> modules) {
    if (imports.contains("org.sonatype.nexus.validation")) {
      modules.add(validationModule);
    }
  }

  private void maybeAddWebResources(List<Module> modules) {
    if (space.getBundle().getEntry("static") != null) {
      modules.add(webResourcesModule);
    }
  }

  private void maybeAddInterceptors(List<Module> modules) {
    for (AbstractInterceptorModule aim : interceptorModules) {
      if (aim.appliesTo(space)) {
        modules.add(aim);
      }
    }
  }
}
