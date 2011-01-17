package org.sonatype.security.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    
    protected Map<String, String> pseudoChains = new LinkedHashMap<String, String>();
    

    public void addProtectedResource( String pathPattern, String filterExpression )
    {
        // Only save the pathPattern and filterExpression in the pseudoChains, does not put real filters into the real
        // chain.
        // We can not get the real filters because this method is invoked when the application is starting, when ShiroSecurityFilter
        // might not be located.
     
        if( this.filterChainManager != null )
        {
            this.filterChainManager.createChain( pathPattern, filterExpression );
        }
        else
        {
            this.pseudoChains.put( pathPattern, filterExpression );
        }
    }

    public void setFilterChainManager( FilterChainManager filterChainManager )
    {
        this.filterChainManager = filterChainManager;
        
        // lazy load: see https://issues.sonatype.org/browse/NEXUS-3111
        // which to me seems like a glassfish bug...
        for ( Entry<String, String> entry : this.pseudoChains.entrySet() )
        {
            this.filterChainManager.createChain( entry.getKey(), entry.getValue() );
        }
    }

}
