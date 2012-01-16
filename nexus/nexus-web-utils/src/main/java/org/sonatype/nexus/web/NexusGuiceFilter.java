/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.servlet.FilterPipeline;
import com.google.inject.servlet.GuiceFilter;

public final class NexusGuiceFilter
    extends GuiceFilter
{
    @Inject
    static List<FilterPipeline> pipelines = Collections.emptyList();

    public NexusGuiceFilter()
    {
        super( new MultiFilterPipeline() );
    }

    static final class MultiFilterPipeline
        implements FilterPipeline
    {
        public void initPipeline( ServletContext context )
        {
            // pipelines support lazy initialization
        }

        public void dispatch( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException
        {
            new MultiFilterChain( chain ).doFilter( request, response );
        }

        public void destroyPipeline()
        {
            for ( final FilterPipeline p : pipelines )
            {
                p.destroyPipeline();
            }
        }
    }

    static final class MultiFilterChain
        implements FilterChain
    {
        private final Iterator<FilterPipeline> itr;

        private final FilterChain defaultChain;

        MultiFilterChain( final FilterChain chain )
        {
            itr = pipelines.iterator();
            defaultChain = chain;
        }

        public void doFilter( final ServletRequest request, final ServletResponse response )
            throws IOException, ServletException
        {
            if ( itr.hasNext() )
            {
                itr.next().dispatch( request, response, this );
            }
            else
            {
                defaultChain.doFilter( request, response );
            }
        }
    }
}
