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

package org.sonatype.nexus.jmx.internal;

import java.lang.annotation.Annotation;
import java.util.Hashtable;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.nexus.jmx.MBean;
import org.sonatype.nexus.jmx.ObjectNameEntry;
import org.sonatype.nexus.jmx.SuppliedMBeanAttribute;
import org.sonatype.nexus.jmx.reflect.ManagedObject;
import org.sonatype.nexus.jmx.reflect.ReflectionMBeanBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.inject.Key;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.inject.BeanLocator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles registration of {@link ManagedObject} components.
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class ManagedObjectRegistrar
    extends ComponentSupport
{
  private final MBeanServer server;

  @Inject
  public ManagedObjectRegistrar(final BeanLocator beanLocator,
                                final MBeanServer server)
  {
    checkNotNull(beanLocator);
    this.server = checkNotNull(server);

    beanLocator.watch(Key.get(Object.class), new ManageObjectMediator(), this);
  }

  private class ManageObjectMediator
      implements Mediator<Annotation, Object, ManagedObjectRegistrar>
  {
    @Override
    public void add(final BeanEntry<Annotation, Object> entry, final ManagedObjectRegistrar watcher)
        throws Exception
    {
      ManagedObject descriptor = descriptor(entry);
      if (descriptor == null) {
        return;
      }

      try {
        ObjectName name = objectName(descriptor, entry);
        log.info("Registering: {} -> {}", name, entry);
        MBean mbean = mbean(descriptor, entry);
        server.registerMBean(mbean, name);
      }
      catch (Exception e) {
        log.warn("Failed to export: {}; ignoring", entry, e);
      }
    }

    @Override
    public void remove(final BeanEntry<Annotation, Object> entry, final ManagedObjectRegistrar watcher)
        throws Exception
    {
      ManagedObject descriptor = descriptor(entry);
      if (descriptor == null) {
        return;
      }

      try {
        ObjectName name = objectName(descriptor, entry);
        log.info("Un-registering: {} -> {}", name, entry);
        server.unregisterMBean(name);
      }
      catch (Exception e) {
        log.warn("Failed to un-export: {}; ignoring", entry, e);
      }
    }
  }

  @Nullable
  private ManagedObject descriptor(final BeanEntry<Annotation, Object> entry) {
    Class<?> type = entry.getImplementationClass();
    return type.getAnnotation(ManagedObject.class);
  }

  /**
   * Determine {@link ObjectName} for given {@link BeanEntry}.
   */
  private ObjectName objectName(final ManagedObject descriptor, final BeanEntry<Annotation, Object> entry) throws Exception {
    Class<?> type = entry.getImplementationClass();

    // default domain to package if missing
    String domain = descriptor.domain();
    if (Strings.emptyToNull(domain) == null) {
      domain = type.getPackage().getName();
    }

    // Hashtable is required by ancient JMX api
    Hashtable<String, String> entries = new Hashtable<>();

    // add custom object-name entries
    for (ObjectNameEntry kv : descriptor.entries()) {
      entries.put(kv.name(), kv.value());
    }

    // set object-name 'type'
    String otype = Strings.emptyToNull(descriptor.type());
    if (otype == null) {
      otype = type.getSimpleName();
    }
    entries.put("type", otype);

    // optionally set object-name 'name'
    String oname = Strings.emptyToNull(descriptor.name());
    if (oname == null) {
      // default to binding-name if present
      Named name = type.getAnnotation(Named.class);
      if (name != null) {
        oname = Strings.emptyToNull(name.value());
      }
    }
    if (oname != null) {
      entries.put("name", oname);
    }

    return new ObjectName(domain, entries);
  }

  /**
   * Construct mbean for given {@link BeanEntry} discovering its attributes and operations.
   */
  private MBean mbean(final ManagedObject descriptor, final BeanEntry<Annotation, Object> entry) throws Exception {
    Class<?> type = entry.getImplementationClass();

    ReflectionMBeanBuilder builder = new ReflectionMBeanBuilder(type);

    // attach manged target
    builder.target(new Supplier()
    {
      @Override
      public Object get() {
        return entry.getProvider().get();
      }
    });

    // allow custom description, or expose what sisu tells us
    String description = Strings.emptyToNull(descriptor.description());
    if (description == null) {
      description = entry.getDescription();
    }
    builder.description(description);

    // discover managed members
    builder.discover();

    // expose additinal information we have about the bean
    builder.attribute(new SuppliedMBeanAttribute.Builder()
            .name("sisu.rank")
            .description("Sisu bean-rank")
            .value(entry.getRank())
            .build()
    );
    builder.attribute(new SuppliedMBeanAttribute.Builder()
            .name("sisu.source")
            .description("Sisu bean-source")
            .value(entry.getSource())
            .build()
    );

    return builder.build();
  }
}
