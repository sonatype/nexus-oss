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
 * Builder for contributions used to activate plugin UI.
 *
 * @see UiContributor
 * @since 2.4
 */
public class UiContributionBuilder
    extends AbstractUiContributionBuilder<UiContributor.UiContribution>
{

    private String module;

    private List<String> dependencies = Lists.newLinkedList();

    public UiContributionBuilder( final Object owner, final String groupId, final String artifactId )
    {
        super( owner, groupId, artifactId );
    }

    /**
     * Sets the entry point to use.
     */
    public UiContributionBuilder boot( String module )
    {
        this.module = module;
        return this;
    }

    /**
     * Adds a dependency.
     */
    public UiContributionBuilder withDependency( String dependency )
    {
        dependencies.add( dependency );
        return this;
    }

    /**
     * Adds the default location for a compressed plugin js file: /static/js/$artifactId-all.js
     */
    public UiContributionBuilder withDefaultAggregateDependency()
    {
        return withDependency( getDefaultPath( "js" ) );
    }

    @Override
    public UiContributor.UiContribution build()
    {
        return new UiContributor.UiContribution( module, dependencies );
    }
}
