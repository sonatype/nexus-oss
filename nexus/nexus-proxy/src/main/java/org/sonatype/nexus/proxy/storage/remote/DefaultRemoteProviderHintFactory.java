package org.sonatype.nexus.proxy.storage.remote;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

/**
 * This class allows for configuration of the default Remote Repository Provider hint/name. <BR/>
 * NOTE: This class was added allow a smooth transition between an AHC based transport and the HttpClient one.
 */
@Component( role = RemoteProviderHintFactory.class )
public class DefaultRemoteProviderHintFactory
    implements RemoteProviderHintFactory
{
    public final static String DEFAULT_HTTP_PROVIDER_KEY = "default.http.provider";

    /**
     * @return The default HTTP provider name/hint as a string.
     */
    public String getDefaultRoleHint()
    {
        return System.getProperty( DEFAULT_HTTP_PROVIDER_KEY, CommonsHttpClientRemoteStorage.PROVIDER_STRING );
    }
}
