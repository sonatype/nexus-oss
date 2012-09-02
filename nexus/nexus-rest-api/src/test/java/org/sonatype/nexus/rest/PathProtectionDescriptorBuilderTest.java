/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link PathProtectionDescriptorBuilder}.
 */
public class PathProtectionDescriptorBuilderTest
    extends TestSupport
{

    @Test
    public void pathAndFilters()
    {
        PathProtectionDescriptor descriptor = new PathProtectionDescriptorBuilder()
            .path( "/foo" )
            .authcBasic()
            .filter( "perms", "foo" )
            .build();

        log( descriptor.getPathPattern() );
        assertEquals( "/foo", descriptor.getPathPattern() );

        log( descriptor.getFilterExpression() );
        assertEquals( "authcBasic,perms[foo]", descriptor.getFilterExpression() );
    }

    @Test
    public void simplePath()
    {
        PathProtectionDescriptor descriptor = new PathProtectionDescriptorBuilder()
            .path( "/foo" )
            .build();

        log( descriptor.getPathPattern() );
        assertEquals( "/foo", descriptor.getPathPattern() );

        log( descriptor.getFilterExpression() );
        assertEquals( null, descriptor.getFilterExpression() );
    }

    @Test( expected = IllegalStateException.class )
    public void pathMissing()
    {
        new PathProtectionDescriptorBuilder()
            .authcBasic()
            .build();
    }

    @Test
    public void singlePerm()
    {
        PathProtectionDescriptor descriptor = new PathProtectionDescriptorBuilder()
            .path( "/foo" )
            .authcBasic()
            .perms( "foo" )
            .build();

        log( descriptor.getPathPattern() );
        assertEquals( "/foo", descriptor.getPathPattern() );

        log( descriptor.getFilterExpression() );
        assertEquals( "authcBasic,perms[foo]", descriptor.getFilterExpression() );
    }

    @Test
    public void multiplePerms()
    {
        PathProtectionDescriptor descriptor = new PathProtectionDescriptorBuilder()
            .path( "/foo" )
            .authcBasic()
            .perms( "foo", "bar" )
            .build();

        log( descriptor.getPathPattern() );
        assertEquals( "/foo", descriptor.getPathPattern() );

        log( descriptor.getFilterExpression() );
        assertEquals( "authcBasic,perms[foo,bar]", descriptor.getFilterExpression() );
    }

}
