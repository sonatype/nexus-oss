package org.sonatype.security.ldap.upgrade.cipher;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = PlexusCipher.class, hint = "test" )
public class TestPlexusCipher
    extends DefaultPlexusCipher
{

    public String getAlgorithm()
    {
        return super.algorithm;
    }

    public int getIterationCount()
    {
        return super.iterationCount;
    }
}
