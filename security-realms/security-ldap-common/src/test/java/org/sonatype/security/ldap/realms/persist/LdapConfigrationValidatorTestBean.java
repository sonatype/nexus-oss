/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import java.io.File;

public class LdapConfigrationValidatorTestBean
{
    
    private File configFile;
    
    private int numberOfErrors;
    
    private int numberOfWarnings;

    public File getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile( File configFile )
    {
        this.configFile = configFile;
    }

    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    public void setNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors = numberOfErrors;
    }

    public int getNumberOfWarnings()
    {
        return numberOfWarnings;
    }

    public void setNumberOfWarnings( int numberOfWarnings )
    {
        this.numberOfWarnings = numberOfWarnings;
    }
    
    
    
}
