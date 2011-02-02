package org.sonatype.nexus.proxy.storage.remote;


/**
 * This interface allows for configuration of the default Remote Repository Provider hint/name. <BR/>
 * NOTE: This class was added allow a smooth transition between an AHC based transport and the HttpClient one.
 */
public interface RemoteProviderHintFactory
{
    /**
     * @return The default HTTP provider name/hint as a string.
     */
    public String getDefaultRoleHint();
}
