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

import java.lang.annotation.Annotation;
import java.net.URL;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.bean.scanners.EmptyAnnotationVisitor;
import org.sonatype.guice.bean.scanners.EmptyClassVisitor;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;
import org.sonatype.guice.bean.scanners.asm.Opcodes;
import org.sonatype.guice.bean.scanners.asm.Type;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.guice.plexus.scanners.PlexusTypeVisitor;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ClassSpaceVisitor} that looks for @{@link ExtensionPoint}, @ {@link RepositoryType}, or @{@link Managed}.
 */
public final class NexusTypeVisitor
    extends EmptyClassVisitor
    implements ClassSpaceVisitor
{
  private static final Logger log = LoggerFactory.getLogger(NexusTypeVisitor.class);

  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  static final String COMPONENT_DESC = Type.getDescriptor(Component.class);

  static final String NAMED_DESC = Type.getDescriptor(Named.class);

  static final String SINGLETON_DESC = Type.getDescriptor(Singleton.class);

  static final String EXTENSION_POINT_DESC = Type.getDescriptor(ExtensionPoint.class);

  static final String MANAGED_DESC = Type.getDescriptor(Managed.class);

  static final String LEGACY_SINGLETON_DESC = Type.getDescriptor(com.google.inject.Singleton.class);

  static final String LEGACY_NAMED_DESC = Type.getDescriptor(com.google.inject.name.Named.class);

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final NamedHintAnnotationVisitor namedHintVisitor = new NamedHintAnnotationVisitor();

  private final NexusTypeCache nexusTypeCache = new NexusTypeCache();

  private final NexusTypeListener nexusTypeListener;

  private final PlexusTypeVisitor plexusTypeVisitor;

  private ClassSpace space;

  private URL source;

  private String clazz;

  private NexusType nexusType = MarkedNexusTypes.UNKNOWN;

  private boolean sawComponent;

  private boolean sawNamed;

  private boolean sawSingleton;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public NexusTypeVisitor(final NexusTypeListener listener) {
    nexusTypeListener = listener;
    plexusTypeVisitor = new PlexusTypeVisitor(listener);
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public void visit(final ClassSpace _space) {
    space = _space;
    plexusTypeVisitor.visit(_space);
  }

  public ClassVisitor visitClass(final URL url) {
    source = url;
    nexusType = MarkedNexusTypes.UNKNOWN;
    plexusTypeVisitor.visitClass(url);
    return this;
  }

  @Override
  public void visit(final int version,
                    final int access,
                    final String name,
                    final String signature,
                    final String superName,
                    final String[] interfaces)
  {
    clazz = name.replace('/', '.');
    nexusTypeListener.hear(clazz);

    if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC)) == 0) {
      scanForNexusMarkers(clazz, interfaces);
    }
    plexusTypeVisitor.visit(version, access, name, signature, superName, interfaces);
  }

  private void warn(final String message, final Object... args) {
    log.warn(message, args);
    log.debug("Source: {}", source);
  }

  private void debug(final String message, final Object... args) {
    log.debug(message, args);
    log.debug("Source: {}", source);
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    // Remember if we saw @Component, @Named or @Singleton for legacy warning detection
    if (COMPONENT_DESC.equals(desc)) {
      sawComponent = true;
    }
    else if (NAMED_DESC.equals(desc)) {
      sawNamed = true;
    }
    else if (SINGLETON_DESC.equals(desc)) {
      sawSingleton = true;
    }

    // If we detected a class, then ...
    if (clazz != null) {
      // Complain if we see legacy annotations
      if (EXTENSION_POINT_DESC.equals(desc)) {
        // TODO: Flip this to complain() when aggressively killing legacy component annotations
        debug("Found legacy @{} annotation: {}", ExtensionPoint.class.getName(), clazz);
      }
      else if (MANAGED_DESC.equals(desc)) {
        // TODO: Flip this to complain() when aggressively killing legacy component annotations
        debug("Found legacy @{} annotation: {}", Managed.class.getName(), clazz);
      }
      else if (LEGACY_SINGLETON_DESC.equals(desc)) {
        warn("Found legacy @{} annotation: {}; replace with @{}",
            com.google.inject.Singleton.class.getName(), clazz, Singleton.class.getName());
      }
      else if (LEGACY_NAMED_DESC.equals(desc)) {
        warn("Found legacy @{} annotation: {}; replace with @{}",
            com.google.inject.name.Named.class.getName(), Named.class.getName(), clazz);
      }
    }

    final AnnotationVisitor annotationVisitor = plexusTypeVisitor.visitAnnotation(desc, visible);
    return nexusType.isComponent() && NAMED_DESC.equals(desc) ? namedHintVisitor : annotationVisitor;
  }

  @Override
  public void visitEnd() {
    final Annotation details = nexusType.details();
    if (details instanceof RepositoryType) {
      nexusTypeListener.hear((RepositoryType) details);
    }
    plexusTypeVisitor.visitEnd();

    // If we detected a class, then ...
    if (clazz != null) {
      // If not a legacy Plexus component, check if we have "magic" in play...
      if (!sawComponent) {
        // Complain if we found a JSR-330 component relying on legacy @ExtensionPoint or @Managed semantics
        if (nexusType == MarkedNexusTypes.EXTENSION_POINT && !sawNamed) {
          warn("Found legacy component relying on @{} magic to automatically imply @{}: {}",
              ExtensionPoint.class.getName(), Named.class.getName(), clazz);
        }
        else if (nexusType == MarkedNexusTypes.EXTENSION_POINT_SINGLETON && !(sawNamed && sawSingleton)) {
          warn("Found legacy component relying on @{} magic to automatically imply @{} @{}: {}",
              ExtensionPoint.class.getName(), Named.class.getName(), Singleton.class.getName(), clazz);
        }
        else if (nexusType == MarkedNexusTypes.MANAGED && !sawNamed) {
          warn("Found legacy component relying on @{} magic to automatically imply @{}: {}",
              Managed.class.getName(), Named.class.getName(), clazz);
        }
        else if (nexusType == MarkedNexusTypes.MANAGED_SINGLETON && !(sawNamed && sawSingleton)) {
          warn("Found legacy component relying on @{} magic to automatically imply @{} @{}: {}",
              Managed.class.getName(), Named.class.getName(), Singleton.class.getName(), clazz);
        }
      }
      else {
        // TODO: Flip this to complain() when aggressively killing plexus components
        debug("Found legacy plexus component: {}", clazz);
      }
    }

    // reset state
    source = null;
    clazz = null;
    sawComponent = false;
    sawNamed = false;
    sawSingleton = false;
  }

  // ----------------------------------------------------------------------
  // Implementation methods
  // ----------------------------------------------------------------------

  private void scanForNexusMarkers(final String clazz, final String[] interfaces) {
    for (final String i : interfaces) {
      nexusType = nexusTypeCache.nexusType(space, i);
      if (nexusType.isComponent()) {
        final AnnotationVisitor componentVisitor = getComponentVisitor();
        componentVisitor.visit("role", Type.getObjectType(i));
        if (nexusType != MarkedNexusTypes.MANAGED && nexusType != MarkedNexusTypes.MANAGED_SINGLETON) {
          componentVisitor.visit("hint", clazz);
        }
        if (!nexusType.isSingleton()) {
          componentVisitor.visit("instantiationStrategy", Strategies.PER_LOOKUP);
        }
        break;
      }
    }
  }

  AnnotationVisitor getComponentVisitor() {
    return plexusTypeVisitor.visitAnnotation(COMPONENT_DESC, true);
  }

  // ----------------------------------------------------------------------
  // Named annotation scanner
  // ----------------------------------------------------------------------

  final class NamedHintAnnotationVisitor
      extends EmptyAnnotationVisitor
  {
    @Override
    public void visit(final String name, final Object value) {
      getComponentVisitor().visit("hint", value);
    }
  }
}
