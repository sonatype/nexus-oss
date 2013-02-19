/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.rest;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Builder for require.js contributions used to drive plugin UI.
 *
 * @see RequireJsContributor
 * @since 2.4
 */
public class RequireJsContributionBuilder
    extends UiSnippetBuilder<RequireJsContributor.RequireJsContribution>
{

    private String module;

    private List<String> dependencies = Lists.newLinkedList();

    public RequireJsContributionBuilder( final Object owner, final String groupId, final String artifactId )
    {
        super( owner, groupId, artifactId );
    }

    /**
     * Sets the main module name to use.
     */
    public RequireJsContributionBuilder module( String module )
    {
        this.module = module;
        return this;
    }

    /**
     * Adds a dependency.
     */
    public RequireJsContributionBuilder dependency( String dependency )
    {
        dependencies.add( dependency );
        return this;
    }

    /**
     * Adds the default location for a compressed plugin js file: /static/js/$artifactId-all.js
     */
    public RequireJsContributionBuilder defaultAggregateDependency()
    {
        return dependency( getDefaultPath( "js" ) );
    }

    @Override
    public RequireJsContributor.RequireJsContribution build()
    {
        return new RequireJsContributor.RequireJsContribution( module, dependencies );
    }
}
