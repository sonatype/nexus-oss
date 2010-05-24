package org.sonatype.security.realms.publickey;

import java.security.PublicKey;

public class MockPublicKey
    implements PublicKey
{
    private static final long serialVersionUID = -1748932588224311551L;

    private String content;

    public MockPublicKey( String content )
    {
        this.content = content;
    }

    public String getAlgorithm()
    {
        return "mock";
    }

    public byte[] getEncoded()
    {
        return content.getBytes();
    }

    public String getFormat()
    {
        return "ummm";
    }

}
