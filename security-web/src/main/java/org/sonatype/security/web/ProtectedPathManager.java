package org.sonatype.security.web;


/**
 *  This component will manage how paths are dynamically added to the security infrastructure. 
 *  
 * @author Brian Demers
 *
 */
public interface ProtectedPathManager
{
    /**
     * Adds a protected resource for the <codepathPattern</code>, and configures it with the<code>filterExpression</code>.
     * @param pathPattern the pattern of the path to protect (i.e. ant pattern)
     * @param filterExpression the configuration used for the filter protecting this pattern.
     */
    public void addProtectedResource( String pathPattern, String filterExpression );
    
    //TODO: this may require some shiro changes if we need this in the future.
//    /**
//     * Removes a protected path
//     * @param pathPattern path to be removed
//     */
//    public void removeProtectedResource( String pathPattern );
}
