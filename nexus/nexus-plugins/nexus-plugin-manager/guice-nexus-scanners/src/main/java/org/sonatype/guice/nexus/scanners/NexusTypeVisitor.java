/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.nexus.scanners;

import java.lang.annotation.Annotation;
import java.net.URL;

import javax.inject.Named;

import org.codehaus.plexus.component.annotations.Component;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.bean.scanners.EmptyAnnotationVisitor;
import org.sonatype.guice.bean.scanners.EmptyClassVisitor;
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

    private NexusType nexusType;

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
