package org.sonatype.security.web;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

/**
 * The default implementation requires a FilterChainManager, so the configuration can be passed to it.
 * 
 * @author Brian Demers
 *
 */
@Singleton
@Typed( value = ProtectedPathManager.class )
@Named( value = "default" )
public class DefaultProtectedPathManager
    implements ProtectedPathManager, FilterChainManagerAware
{
    private FilterChainManager filterChainManager;
    
    public void addProtectedResource( String pathPattern, String filterExpression )
    {
        this.filterChainManager.createChain( pathPattern, filterExpression );
    }

    public void setFilterChainManager( FilterChainManager filterChainManager )
    {
        this.filterChainManager = filterChainManager;
    }

}
