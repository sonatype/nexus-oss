package org.sonatype.security.ldap.upgrade.cipher;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.ldap.realms.persist.PasswordHelper;


public class LegacyPasswordEncryptionTest
    extends PlexusTestCase
{

    public void testLegacyPassword() throws Exception
    {
        String legacyEncryptedPassword = "CP2WQrKyuB/fphz8c1eg5zaG";
        String legacyClearPassword = "S0natyp31";
        
        PasswordHelper passHelper = this.lookup( PasswordHelper.class );
        
        Assert.assertEquals( passHelper.decrypt( legacyEncryptedPassword ), legacyClearPassword );
    }

}
