/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.capabilities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Configuration adapter for {@link YumRepositoryCapability}.
 *
 * @since 2.2
 */
public class YumRepositoryCapabilityConfiguration
{

    public static final String REPOSITORY_ID = "repository";

    private String repository;

    public YumRepositoryCapabilityConfiguration( final Map<String, String> properties )
    {
        checkNotNull( properties );
        this.repository = properties.get( REPOSITORY_ID );
    }

    public String repository()
    {
        return repository;
    }

    public YumRepositoryCapabilityConfiguration setRepository( final String repository )
    {
        this.repository = repository;
        return this;
    }

    public Map<String, String> asMap()
    {
        final Map<String, String> props = Maps.newHashMap();
        props.put( REPOSITORY_ID, repository );
        return props;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
            "repository=" + repository +
            '}';
    }
}
