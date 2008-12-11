/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.upgrade;

import java.io.File;
import java.io.IOException;

/**
 * A component that upgrades configuration model from (model version) to (model version +1).
 * 
 * @author cstamas
 */
public interface Upgrader
{
    Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException;

    void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException;
}
