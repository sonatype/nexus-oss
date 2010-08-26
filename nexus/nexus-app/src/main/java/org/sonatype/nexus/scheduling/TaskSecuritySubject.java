package org.sonatype.nexus.scheduling;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

public class TaskSecuritySubject implements Subject
{
    private static final String USER_ID = "Task-User";
    private PrincipalCollection principalCollection = new SimplePrincipalCollection( USER_ID, "" );
    
    public void checkPermission( String permission )
        throws AuthorizationException
    {
        // do nothing
    }

    public void checkPermission( Permission permission )
        throws AuthorizationException
    {
        // do nothing
    }

    public void checkPermissions( String... permissions )
        throws AuthorizationException
    {
        // do nothing   
    }

    public void checkPermissions( Collection<Permission> permissions )
        throws AuthorizationException
    {
        // do nothing     
    }

    public void checkRole( String roleIdentifier )
        throws AuthorizationException
    {
        // do nothing   
    }

    public void checkRoles( Collection<String> roleIdentifiers )
        throws AuthorizationException
    {
        // do nothing   
    }

    public Object getPrincipal()
    {
        return USER_ID;
    }

    public PrincipalCollection getPrincipals()
    {
        return this.principalCollection;
    }

    public Session getSession()
    {
        return null;
    }

    public Session getSession( boolean create )
    {
        return null;
    }

    public boolean hasAllRoles( Collection<String> roleIdentifiers )
    {
        return true;
    }

    public boolean hasRole( String roleIdentifier )
    {
        return true;
    }

    public boolean[] hasRoles( List<String> roleIdentifiers )
    {
        boolean[] results = new boolean[roleIdentifiers.size()];
        for ( int ii = 0; ii < results.length; ii++ )
        {
            results[ii] = true;
        }
        return results;
    }

    public boolean isAuthenticated()
    {
        return true;
    }

    public boolean isPermitted( String permission )
    {
        return true;
    }

    public boolean isPermitted( Permission permission )
    {
        return true;
    }

    public boolean[] isPermitted( String... permissions )
    {
        boolean[] results = new boolean[permissions.length];
        for ( int ii = 0; ii < results.length; ii++ )
        {
            results[ii] = true;
        }
        return results;
    }

    public boolean[] isPermitted( List<Permission> permissions )
    {
        boolean[] results = new boolean[permissions.size()];
        for ( int ii = 0; ii < results.length; ii++ )
        {
            results[ii] = true;
        }
        return results;
    }

    public boolean isPermittedAll( String... permissions )
    {
        return true;
    }

    public boolean isPermittedAll( Collection<Permission> permissions )
    {
        return true;
    }

    public void login( AuthenticationToken token )
        throws AuthenticationException
    {
        // do nothing
    }

    public void logout()
    {
       ThreadContext.unbindSubject();
    }

    public boolean isRemembered()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public <V> V execute( Callable<V> callable )
        throws ExecutionException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void execute( Runnable runnable )
    {
        // TODO Auto-generated method stub

    }

    public <V> Callable<V> associateWith( Callable<V> callable )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Runnable associateWith( Runnable runnable )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void runAs( PrincipalCollection principals )
        throws NullPointerException, IllegalStateException
    {
        // TODO Auto-generated method stub

    }

    public boolean isRunAs()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public PrincipalCollection getPreviousPrincipals()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public PrincipalCollection releaseRunAs()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
