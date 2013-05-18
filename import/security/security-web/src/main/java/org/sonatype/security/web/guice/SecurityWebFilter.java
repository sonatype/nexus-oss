/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.web.guice;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.sonatype.security.SecuritySystem;

/**
 * Injected {@link ShiroFilter} that only applies when {@link SecuritySystem#isSecurityEnabled()} is {@code true}.
 */
@Singleton
public class SecurityWebFilter
    extends AbstractShiroFilter
{
    private final SecuritySystem securitySystem;

    @Inject
    protected SecurityWebFilter( SecuritySystem securitySystem, FilterChainResolver filterChainResolver )
    {
        this.securitySystem = securitySystem;
        this.setSecurityManager( (WebSecurityManager) securitySystem.getSecurityManager() );
        this.setFilterChainResolver( filterChainResolver );
    }

    @Override
    public boolean isEnabled()
    {
        return securitySystem.isSecurityEnabled();
    }
}
