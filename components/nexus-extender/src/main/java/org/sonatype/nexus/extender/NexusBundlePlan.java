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
package org.sonatype.nexus.extender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.nexus.guice.AbstractInterceptorModule;
import org.sonatype.nexus.guice.NexusModules.PluginModule;
import org.sonatype.nexus.guice.NexusTypeBinder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import org.codehaus.plexus.context.Context;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.inject.BindingPublisher;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.InjectorPublisher;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.launch.BundlePlan;
import org.eclipse.sisu.plexus.DefaultPlexusBeanLocator;
import org.eclipse.sisu.plexus.PlexusAnnotatedBeanModule;
import org.eclipse.sisu.plexus.PlexusBeanConverter;
import org.eclipse.sisu.plexus.PlexusBeanLocator;
import org.eclipse.sisu.plexus.PlexusBeanModule;
import org.eclipse.sisu.plexus.PlexusBindingModule;
import org.eclipse.sisu.plexus.PlexusXmlBeanConverter;
import org.eclipse.sisu.plexus.PlexusXmlBeanModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.wire.EntryListAdapter;
import org.eclipse.sisu.wire.ParameterKeys;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;

/**
 * {@link BundlePlan} that configures bundles using the Nexus binding strategy.
 * 
 * @since 3.0
 */
public class NexusBundlePlan
    implements BundlePlan
{
  private final AtomicInteger pluginRank = new AtomicInteger(1);

  private final MutableBeanLocator beanLocator;

  private final BeanManager beanManager;

  private final Map<?, ?> variables;

  private final List<AbstractInterceptorModule> interceptorModules;

  public NexusBundlePlan(final MutableBeanLocator beanLocator) {
    this.beanLocator = beanLocator;

    beanManager = lookup(Key.get(BeanManager.class));
    variables = lookup(Key.get(Context.class)).getContextData(); // FIXME: change to ParameterKeys.PROPERTIES
    interceptorModules = new EntryListAdapter<>(beanLocator.locate(Key.get(AbstractInterceptorModule.class)));
  }

  public BindingPublisher prepare(final Bundle bundle) {

    // ignore anything that isn't a Nexus plugin
    if (bundle.getResource("META-INF/nexus/plugin.xml") == null) {
      return null;
    }

    final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

    // Scan for Plexus XML components
    final ClassSpace pluginSpace = new BundleClassSpace(bundle);
    beanModules.add(new PlexusXmlBeanModule(pluginSpace, variables));

    // Scan for annotated components
    beanModules.add(new PlexusAnnotatedBeanModule(pluginSpace, variables, BeanScanning.INDEX)
        .with(NexusTypeBinder.STRATEGY));

    // Assemble plugin components and resources
    final List<Module> modules = new ArrayList<Module>();
    modules.add(new PluginModule());
    modules.addAll(interceptorModules);
    modules.add(new PlexusBindingModule(beanManager, beanModules));
    modules.add(new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(MutableBeanLocator.class).toInstance(beanLocator);
        bind(RankingFunction.class).toInstance(new DefaultRankingFunction(pluginRank.incrementAndGet()));
        bind(PlexusBeanLocator.class).to(DefaultPlexusBeanLocator.class);
        bind(PlexusBeanConverter.class).to(PlexusXmlBeanConverter.class);
        bind(ParameterKeys.PROPERTIES).toInstance(variables);
      }
    });

    return new InjectorPublisher(Guice.createInjector(new WireModule(modules)));
  }

  private <T> T lookup(final Key<T> key) {
    return beanLocator.locate(key).iterator().next().getValue();
  }
}
