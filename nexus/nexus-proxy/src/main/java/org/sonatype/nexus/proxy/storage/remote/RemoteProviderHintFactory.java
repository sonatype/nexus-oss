package org.sonatype.nexus.proxy.storage.remote;

/**
 * Component that drives the remote storage transport provider selection by telling the "hint" (former Plexus role
 * hint), of the RRS component to be used. It allows multiple way of configuration, either by setting the default
 * provider and even forceful overriding the hint (if configuration would say otherwise). This component is meant to
 * help smooth transition from Apache HttpClient3x RRS (for long time the one and only RRS implementation) to Ning's
 * AsyncHttpClient implementation.
 */
public interface RemoteProviderHintFactory
{
    /**
     * Returns the default provider role hint for provided remote URL.
     * 
     * @return The default provider role hint as a string.
     */
    String getDefaultRoleHint( final String remoteUrl )
        throws IllegalArgumentException;

    /**
     * Returns the provider role hint to be used, based on passed in remote URL and hint.
     * 
     * @return The provider role hint to be used, based on passed in remote URL and hint. If forceful override is in
     *         effect, it will return the forced, otherwise the passed in one (if it is valid, non-null, etc).
     */
    String getRoleHint( final String remoteUrl, final String hint )
        throws IllegalArgumentException;

    /**
     * Returns the default HTTP provider role hint.
     * 
     * @return The default HTTP provider role hint as a string.
     */
    String getDefaultHttpRoleHint();

    /**
     * Returns the HTTP provider role hint to be used, based on passed in hint.
     * 
     * @return The HTTP provider role hint to be used, based on passed in hint. If forceful override is in effect, it
     *         will return the forced, otherwise the passed in one (if it is valid, non-null, etc).
     */
    String getHttpRoleHint( final String hint );
}
