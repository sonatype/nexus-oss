package org.sonatype.nexus.plugins;

public enum PluginActivationRequest
{
    ACTIVATE,

    DEACTIVATE;

    public boolean isSucess( PluginActivationResult result )
    {
        return ( ACTIVATE.equals( this ) && PluginActivationResult.ACTIVATED.equals( result ) )
            || ( DEACTIVATE.equals( this ) && PluginActivationResult.DEACTIVATED.equals( result ) );
    }
}
