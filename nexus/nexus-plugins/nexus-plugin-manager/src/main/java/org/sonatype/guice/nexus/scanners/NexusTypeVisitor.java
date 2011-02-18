/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.guice.nexus.scanners;

import java.lang.annotation.Annotation;
import java.net.URL;

import javax.inject.Named;

import org.codehaus.plexus.component.annotations.Component;
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

/**
 * {@link ClassSpaceVisitor} that looks for @{@link ExtensionPoint}, @ {@link RepositoryType}, or @{@link Managed}.
 */
public final class NexusTypeVisitor
    extends EmptyClassVisitor
    implements ClassSpaceVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String COMPONENT_DESC = Type.getDescriptor( Component.class );

    static final String NAMED_DESC = Type.getDescriptor( Named.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final NamedHintAnnotationVisitor namedHintVisitor = new NamedHintAnnotationVisitor();

    private final NexusTypeCache nexusTypeCache = new NexusTypeCache();

    private final NexusTypeListener nexusTypeListener;

    private final PlexusTypeVisitor plexusTypeVisitor;

    private ClassSpace space;

    private NexusType nexusType = MarkedNexusTypes.UNKNOWN;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public NexusTypeVisitor( final NexusTypeListener listener )
    {
        nexusTypeListener = listener;
        plexusTypeVisitor = new PlexusTypeVisitor( listener );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void visit( final ClassSpace _space )
    {
        space = _space;
        plexusTypeVisitor.visit( _space );
    }

    public ClassVisitor visitClass( final URL url )
    {
        nexusType = MarkedNexusTypes.UNKNOWN;
        plexusTypeVisitor.visitClass( url );
        return this;
    }

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        final String clazz = name.replace( '/', '.' );
        nexusTypeListener.hear( clazz );

        if ( ( access & ( Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_SYNTHETIC ) ) == 0 )
        {
            scanForNexusMarkers( clazz, interfaces );
        }
        plexusTypeVisitor.visit( version, access, name, signature, superName, interfaces );
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        final AnnotationVisitor annotationVisitor = plexusTypeVisitor.visitAnnotation( desc, visible );
        return nexusType.isComponent() && NAMED_DESC.equals( desc ) ? namedHintVisitor : annotationVisitor;
    }

    @Override
    public void visitEnd()
    {
        final Annotation details = nexusType.details();
        if ( details instanceof RepositoryType )
        {
            nexusTypeListener.hear( (RepositoryType) details );
        }
        plexusTypeVisitor.visitEnd();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void scanForNexusMarkers( final String clazz, final String[] interfaces )
    {
        for ( final String i : interfaces )
        {
            nexusType = nexusTypeCache.nexusType( space, i );
            if ( nexusType.isComponent() )
            {
                final AnnotationVisitor componentVisitor = getComponentVisitor();
                componentVisitor.visit( "role", Type.getObjectType( i ) );
                if ( nexusType != MarkedNexusTypes.MANAGED && nexusType != MarkedNexusTypes.MANAGED_SINGLETON )
                {
                    componentVisitor.visit( "hint", clazz );
                }
                if ( !nexusType.isSingleton() )
                {
                    componentVisitor.visit( "instantiationStrategy", Strategies.PER_LOOKUP );
                }
                break;
            }
        }
    }

    AnnotationVisitor getComponentVisitor()
    {
        return plexusTypeVisitor.visitAnnotation( COMPONENT_DESC, true );
    }

    // ----------------------------------------------------------------------
    // Named annotation scanner
    // ----------------------------------------------------------------------

    final class NamedHintAnnotationVisitor
        extends EmptyAnnotationVisitor
    {
        @Override
        public void visit( final String name, final Object value )
        {
            getComponentVisitor().visit( "hint", value );
        }
    }
}
