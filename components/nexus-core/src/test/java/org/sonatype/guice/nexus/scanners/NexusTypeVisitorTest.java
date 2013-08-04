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

package org.sonatype.guice.nexus.scanners;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.codehaus.plexus.component.annotations.Component;
import org.junit.Assert;
import org.junit.Test;

public class NexusTypeVisitorTest
    extends TestSupport
{
  interface BadInterface
  {
  }

  @Deprecated
  interface HostInterface0
  {
  }

  @Singleton
  interface UserInterface0
  {
  }

  @ExtensionPoint
  interface HostInterface1
  {
  }

  interface SubHostInterface1
      extends HostInterface1
  {
  }

  @Managed
  interface UserInterface1
  {
  }

  interface SubUserInterface1
      extends UserInterface1
  {
  }

  @Singleton
  @ExtensionPoint
  interface HostInterface2
  {
  }

  @Managed
  @Singleton
  interface UserInterface2
  {
  }

  @Component(role = HostInterface0.class)
  static class BeanA
      implements HostInterface0
  {
  }

  static class BeanB
      implements HostInterface1
  {
  }

  static class BeanC
      implements HostInterface2
  {
  }

  @Component(role = UserInterface0.class, instantiationStrategy = "per-lookup")
  static class BeanD
      implements UserInterface0
  {
  }

  static class BeanE
      implements UserInterface1
  {
  }

  static class BeanF
      implements UserInterface2
  {
  }

  @Named("BeanA")
  static class NamedBeanA
      implements HostInterface1
  {
  }

  @Named("")
  static class NamedBeanB
      implements HostInterface2
  {
  }

  @Named("BeanC")
  static class NamedBeanC
      implements UserInterface1
  {
  }

  @Named("")
  static class NamedBeanD
      implements UserInterface2
  {
  }

  @SuppressWarnings("unused")
  @Component(role = SubHostInterface1.class)
  static class ComponentBeanA
      implements HostInterface1, SubHostInterface1
  {
  }

  @SuppressWarnings("unused")
  @Component(role = SubUserInterface1.class)
  static class ComponentBeanB
      implements UserInterface1, SubUserInterface1
  {
  }

  static class BadBean
      implements BadInterface
  {
  }

  static class TestListener
      implements NexusTypeListener
  {
    final Map<Component, DeferredClass<?>> components;

    public TestListener(final Map<Component, DeferredClass<?>> components) {
      this.components = components;
    }

    public void hear(final String clazz) {
    }

    public void hear(final RepositoryType repositoryType) {
    }

    public void hear(final Component component, final DeferredClass<?> implementation, final Object source) {
      components.put(component, implementation);
    }

    public void hear(final Annotation qualifier, final Class<?> qualifiedType, final Object source) {
    }
  }

  @Test
  public void testNexusTypeScanning()
      throws MalformedURLException
  {
    final URL[] testURLs = new URL[]{new File(util.getTargetDir(), "test-classes").toURI().toURL()};
    final ClassSpace space = new URLClassSpace(getClass().getClassLoader(), testURLs);

    final Map<Component, DeferredClass<?>> components = new HashMap<Component, DeferredClass<?>>();
    new ClassSpaceScanner(space).accept(new NexusTypeVisitor(new TestListener(components)));
    // Assert.assertEquals( 11, components.size() );

    // nexus-core: squash of modules causes to have more than 11
    // TODO: review this to somehow isolate this
    Assert.assertEquals(18, components.size());

    // non-extension so no automatic hinting...
    Assert.assertEquals(BeanA.class,
        components.get(new ComponentImpl(HostInterface0.class, Hints.DEFAULT_HINT, "singleton", "")).load());
    Assert.assertEquals(BeanB.class,
        components.get(new ComponentImpl(HostInterface1.class, BeanB.class.getName(), "per-lookup", "")).load());
    Assert.assertEquals(BeanC.class,
        components.get(new ComponentImpl(HostInterface2.class, BeanC.class.getName(), "singleton", "")).load());
    // non-component so API @Singleton ignored...
    Assert.assertEquals(BeanD.class,
        components.get(new ComponentImpl(UserInterface0.class, Hints.DEFAULT_HINT, "per-lookup", "")).load());
    Assert.assertEquals(BeanE.class,
        components.get(new ComponentImpl(UserInterface1.class, Hints.DEFAULT_HINT, "per-lookup", "")).load());
    final Class<?> duplicate =
        components.get(new ComponentImpl(UserInterface2.class, Hints.DEFAULT_HINT, "singleton", "")).load();

    // duplicate binding, first-one wins... result may vary on different machines/OS's
    Assert.assertTrue(duplicate.equals(BeanF.class) || duplicate.equals(NamedBeanD.class));

    Assert.assertEquals(NamedBeanA.class,
        components.get(new ComponentImpl(HostInterface1.class, "BeanA", "per-lookup", "")).load());
    Assert.assertEquals(NamedBeanB.class,
        components.get(new ComponentImpl(HostInterface2.class, Hints.DEFAULT_HINT, "singleton", "")).load());
    Assert.assertEquals(NamedBeanC.class,
        components.get(new ComponentImpl(UserInterface1.class, "BeanC", "per-lookup", "")).load());

    Assert.assertEquals(ComponentBeanA.class,
        components.get(new ComponentImpl(SubHostInterface1.class, ComponentBeanA.class.getName(),
            "per-lookup", "")).load());
    Assert.assertEquals(ComponentBeanB.class,
        components.get(new ComponentImpl(SubUserInterface1.class, Hints.DEFAULT_HINT, "per-lookup", "")).load());
  }
}
