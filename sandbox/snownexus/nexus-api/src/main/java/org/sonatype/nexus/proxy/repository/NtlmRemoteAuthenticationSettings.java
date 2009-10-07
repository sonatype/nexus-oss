package org.sonatype.nexus.proxy.repository;

public class NtlmRemoteAuthenticationSettings
    extends UsernamePasswordRemoteAuthenticationSettings
{
    private final String ntlmDomain;

    private final String ntlmHost;

    public NtlmRemoteAuthenticationSettings( String username, String password, String ntlmDomain )
    {
        this( username, password, ntlmDomain, null );
    }

    public NtlmRemoteAuthenticationSettings( String username, String password, String ntlmDomain, String ntlmHost )
    {
        super( username, password );

        this.ntlmDomain = ntlmDomain;

        this.ntlmHost = ntlmHost;
    }

    public String getNtlmDomain()
    {
        return ntlmDomain;
    }

    public String getNtlmHost()
    {
        return ntlmHost;
    }
}
