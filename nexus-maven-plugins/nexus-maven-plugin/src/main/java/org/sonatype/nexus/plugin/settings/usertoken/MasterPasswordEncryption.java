/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin.settings.usertoken;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

/**
 * Helper to deal with master-password encryption.
 *
 * @since 2.1
 */
@Component(role=MasterPasswordEncryption.class)
public class MasterPasswordEncryption
{
    @Requirement
    private SecDispatcher dispatcher;

    // NOTE: This is copied from MavenCli as there is no other component which exposes this functionality :-(

    public String encrypt(final String value) throws Exception {
        String configurationFile = ((DefaultSecDispatcher)dispatcher).getConfigurationFile(); // HACK: Need the impl not the intf

        if (configurationFile.startsWith("~")) {
            configurationFile = System.getProperty("user.home") + configurationFile.substring(1);
        }

        String file = System.getProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, configurationFile);

        String master = null;

        SettingsSecurity sec = SecUtil.read(file, true);
        if (sec != null) {
            master = sec.getMaster();
        }

        if (master == null) {
            throw new IllegalStateException("Master password is not set in the setting security file: " + file);
        }

        DefaultPlexusCipher cipher = new DefaultPlexusCipher();
        String masterPasswd = cipher.decryptDecorated(master, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION);
        return cipher.encryptAndDecorate(value, masterPasswd);
    }
}
