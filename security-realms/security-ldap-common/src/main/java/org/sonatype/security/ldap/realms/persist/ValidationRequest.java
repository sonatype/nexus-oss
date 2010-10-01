/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import org.sonatype.security.ldap.realms.persist.model.Configuration;


/**
 * A request for validation, holding the configuration.
 * 
 * @author cstamas
 */
public class ValidationRequest
{
    /**
     * The configuration to validate.
     */
    private Configuration configuration;

    public ValidationRequest( Configuration configuration )
    {
        super();

        this.configuration = configuration;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Configuration configuration )
    {
        this.configuration = configuration;
    }
}
