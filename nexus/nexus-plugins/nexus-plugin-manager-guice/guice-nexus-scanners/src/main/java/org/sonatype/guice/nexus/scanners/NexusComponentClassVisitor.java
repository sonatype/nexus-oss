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

import static org.sonatype.guice.plexus.scanners.AnnotatedPlexusComponentScanner.visitClassResource;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.plexus.scanners.EmptyAnnotationVisitor;
import org.sonatype.guice.plexus.scanners.EmptyClassVisitor;
import org.sonatype.guice.plexus.scanners.PlexusComponentClassVisitor;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

/**
 * {@link ClassVisitor} that collects Nexus components annotated with @{@link ExtensionPoint} or @{@link Managed}.
 */
final class NexusComponentClassVisitor
    extends PlexusComponentClassVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String EXTENSION_POINT_DESC = Type.getDescriptor( ExtensionPoint.class );

    static final String MANAGED_DESC = Type.getDescriptor( Managed.class );

    static final String SINGLETON_DESC = Type.getDescriptor( Singleton.class );

    static final String NAMED_DESC = Type.getDescriptor( Named.class );

    static final String REPOSITORY_TYPE_DESC = Type.getDescriptor( RepositoryType.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger( NexusComponentClassVisitor.class );

    private final Map<String, NexusComponentType> knownTypes = new HashMap<String, NexusComponentType>();

    private final ClassVisitor managedVisitor = new ManagedInterfaceVisitor();

    private final AnnotationVisitor namedVisitor = new NamedAnnotationVisitor();

    private final AnnotationVisitor repositoryTypeVisitor = new RepositoryTypeAnnotationVisitor();

    final List<RepositoryTypeDescriptor> repositoryTypes;

    final List<String> exportedClassNames;

    private boolean skipClass;

    NexusComponentType type;

    String className;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    NexusComponentClassVisitor( final ClassSpace space, final List<RepositoryTypeDescriptor> repositoryTypes,
                                final List<String> exportedClassNames )
    {
        super( space );

        this.repositoryTypes = repositoryTypes;
        this.exportedClassNames = exportedClassNames;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void visit( final int version, final int access, final String name, final String signature,
                       final String superName, final String[] interfaces )
    {
        type = NexusComponentType.UNKNOWN;

        className = name.replace( '/', '.' );
        if ( null != exportedClassNames )
        {
            exportedClassNames.add( className );
        }

        skipClass = true;
        super.visit( version, access, name, signature, superName, interfaces );
        if ( skipClass || interfaces.length == 0 )
        {
            return;
        }

        try
        {
            scanInterfaces( interfaces );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e.toString() );
        }

        if ( type.name().startsWith( "EXTENSION" ) )
        {
            setHint( className ); // default to implementation name as extension hint
        }

        if ( !type.isSingleton() )
        {
            setInstantiationStrategy( "per-lookup" );
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        if ( type.isComponent() && NAMED_DESC.equals( desc ) )
        {
            return namedVisitor;
        }
        if ( null != repositoryTypes && REPOSITORY_TYPE_DESC.equals( desc ) )
        {
            return repositoryTypeVisitor;
        }
        return super.visitAnnotation( desc, visible );
    }

    @Override
    public void setImplementation( final String implementation )
    {
        super.setImplementation( implementation );
        skipClass = false;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void scanInterfaces( final String... interfaces )
        throws IOException
    {
        // look for @ExtensionPoint / @Managed
        for ( final String i : interfaces )
        {
            // check cached results
            type = knownTypes.get( i );
            if ( null == type )
            {
                searchForComponentType( i );
            }
            if ( type.isComponent() )
            {
                // we now know the Plexus role
                setRole( i.replace( '/', '.' ) );
                break;
            }
        }
    }

    private void searchForComponentType( final String interfacePath )
        throws IOException
    {
        type = NexusComponentType.UNKNOWN;
        final URL url = space.getResource( interfacePath + ".class" );
        if ( null != url )
        {
            visitClassResource( url, managedVisitor );
        }
        else
        {
            logger.debug( "Missing interface: " + interfacePath );
        }
        knownTypes.put( interfacePath, type );
    }

    // ----------------------------------------------------------------------
    // Managed interface visitor
    // ----------------------------------------------------------------------

    final class ManagedInterfaceVisitor
        extends EmptyClassVisitor
    {
        private boolean singleton;

        @Override
        public void visit( final int version, final int access, final String name, final String signature,
                           final String superName, final String[] interfaces )
        {
            singleton = false;
        }

        @Override
        public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
        {
            if ( EXTENSION_POINT_DESC.equals( desc ) )
            {
                type = NexusComponentType.EXTENSION_POINT;
            }
            else if ( MANAGED_DESC.equals( desc ) )
            {
                type = NexusComponentType.MANAGED;
            }
            else if ( SINGLETON_DESC.equals( desc ) )
            {
                singleton = true;
            }
            return null;
        }

        @Override
        public void visitEnd()
        {
            if ( singleton )
            {
                type = type.toSingleton();
            }
        }
    }

    // ----------------------------------------------------------------------
    // Named annotation visitor
    // ----------------------------------------------------------------------

    final class NamedAnnotationVisitor
        extends EmptyAnnotationVisitor
    {
        @Override
        public void visit( final String name, final Object value )
        {
            setHint( (String) value );
        }
    }

    // ----------------------------------------------------------------------
    // RepositoryType annotation visitor
    // ----------------------------------------------------------------------

    final class RepositoryTypeAnnotationVisitor
        extends EmptyAnnotationVisitor
    {
        @Override
        public void visit( final String name, final Object value )
        {
            repositoryTypes.add( new RepositoryTypeDescriptor( className, (String) value ) );
        }
    }
}