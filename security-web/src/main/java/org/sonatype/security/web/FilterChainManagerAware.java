package org.sonatype.security.web;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

/**
 * This class marks the ability to have a FilterChainManager set (if it cannot be injected).
 * @author Brian Demers
 *
 */
public interface FilterChainManagerAware
{
    /**
     * Sets the PathMatchingFilterChainResolver.
     * @param originalFilterChainResolver
     */
    public void setFilterChainManager( FilterChainManager filterChainManager );
}
