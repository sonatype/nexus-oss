package org.sonatype.nexus.proxy.repository.charger;

/**
 * Exception handler interface. In case callable throws any exception, and implementation of this interface is provided,
 * it will be asked to handle the exception. Since this is an interface, it is very nice to make your Callable
 * implementation to implement this interface too, and have the actual work, but also it's error handling in one place.
 * 
 * @author cstamas
 */
public interface ExceptionHandler
{
    /**
     * This method is invoked by any application level exception thrown by Callable being processed. If this method
     * returns true, the exception will also be considered as "handled" and will be ignored (the Callable throwing that
     * exception will not participate in the list of results).
     * 
     * @param ex
     * @return
     */
    boolean handle( Exception ex );
}
