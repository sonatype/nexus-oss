package org.sonatype.security.web;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = PlexusPathMatchingFilterChainResolver.class )
public class PlexusPathMatchingFilterChainResolver
    implements FilterChainResolver
{

    private PathMatchingFilterChainResolver originalFilterChainResolver;

    public FilterChain getChain( ServletRequest request, ServletResponse response, FilterChain originalChain )
    {
        return originalFilterChainResolver.getChain( request, response, originalChain );
    }

    public void setOriginalFilterChainResolver( PathMatchingFilterChainResolver originalFilterChainResolver )
    {
        this.originalFilterChainResolver = originalFilterChainResolver;
    }
    
    public void addProtectedResource( String pathPattern, String filterExpression )
    {
        this.originalFilterChainResolver.getFilterChainManager().createChain( pathPattern, filterExpression );
    }

}
