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

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

public class NexusTypeVisitorTest
    extends TestCase
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

    @Component( role = HostInterface0.class )
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

    @Component( role = UserInterface0.class, instantiationStrategy = "per-lookup" )
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

    @Named( "BeanA" )
    static class NamedBeanA
        implements HostInterface1
    {
    }

    @Named( "" )
    static class NamedBeanB
        implements HostInterface2
    {
    }

    @Named( "BeanC" )
    static class NamedBeanC
        implements UserInterface1
    {
    }

    @Named( "" )
    static class NamedBeanD
        implements UserInterface2
    {
    }

    @SuppressWarnings( "unused" )
    @Component( role = SubHostInterface1.class )
    static class ComponentBeanA
        implements HostInterface1, SubHostInterface1
    {
    }

    @SuppressWarnings( "unused" )
    @Component( role = SubUserInterface1.class )
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

        public TestListener( final Map<Component, DeferredClass<?>> components )
        {
            this.components = components;
        }

        public void hear( final String clazz )
        {
        }

        public void hear( final RepositoryType repositoryType )
        {
        }

        public void hear( final Component component, final DeferredClass<?> implementation, final Object source )
        {
            components.put( component, implementation );
        }

        public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
        {
            if ( Component.class == qualifier.annotationType() )
            {
                hear( (Component) qualifier, new LoadedClass<Object>( qualifiedType ), source );
            }
        }
    }

    public void testNexusTypeScanning()
        throws MalformedURLException
    {
        final URL[] testURLs = new URL[] { new File( "target/test-classes" ).toURI().toURL() };
        final ClassSpace space = new URLClassSpace( getClass().getClassLoader(), testURLs );

        final Map<Component, DeferredClass<?>> components = new HashMap<Component, DeferredClass<?>>();
        new ClassSpaceScanner( space ).accept( new NexusTypeVisitor( new TestListener( components ) ) );
        assertEquals( 11, components.size() );

        // non-extension so no automatic hinting...
        assertEquals( BeanA.class,
                      components.get( new ComponentImpl( HostInterface0.class, Hints.DEFAULT_HINT, "singleton", "" ) ).load() );
        assertEquals( BeanB.class,
                      components.get( new ComponentImpl( HostInterface1.class, BeanB.class.getName(), "per-lookup", "" ) ).load() );
        assertEquals( BeanC.class,
                      components.get( new ComponentImpl( HostInterface2.class, BeanC.class.getName(), "singleton", "" ) ).load() );
        // non-component so API @Singleton ignored...
        assertEquals( BeanD.class,
                      components.get( new ComponentImpl( UserInterface0.class, Hints.DEFAULT_HINT, "per-lookup", "" ) ).load() );
        assertEquals( BeanE.class,
                      components.get( new ComponentImpl( UserInterface1.class, Hints.DEFAULT_HINT, "per-lookup", "" ) ).load() );
        final Class<?> duplicate =
            components.get( new ComponentImpl( UserInterface2.class, Hints.DEFAULT_HINT, "singleton", "" ) ).load();

        // duplicate binding, first-one wins... result may vary on different machines/OS's
        assertTrue( duplicate.equals( BeanF.class ) || duplicate.equals( NamedBeanD.class ) );

        assertEquals( NamedBeanA.class,
                      components.get( new ComponentImpl( HostInterface1.class, "BeanA", "per-lookup", "" ) ).load() );
        assertEquals( NamedBeanB.class,
                      components.get( new ComponentImpl( HostInterface2.class, Hints.DEFAULT_HINT, "singleton", "" ) ).load() );
        assertEquals( NamedBeanC.class,
                      components.get( new ComponentImpl( UserInterface1.class, "BeanC", "per-lookup", "" ) ).load() );

        assertEquals( ComponentBeanA.class,
                      components.get( new ComponentImpl( SubHostInterface1.class, ComponentBeanA.class.getName(),
                                                         "per-lookup", "" ) ).load() );
        assertEquals( ComponentBeanB.class,
                      components.get( new ComponentImpl( SubUserInterface1.class, Hints.DEFAULT_HINT, "per-lookup", "" ) ).load() );
    }
}
