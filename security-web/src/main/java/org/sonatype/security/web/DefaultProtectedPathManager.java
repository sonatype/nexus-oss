package org.sonatype.security.web;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.codehaus.plexus.component.annotations.Component;

/**
 * The default implementation requires a PathMatchingFilterChainResolver, so the configuration can be passed to it.
 * 
 * @author Brian Demers
 *
 */
@Component( role = ProtectedPathManager.class )
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
