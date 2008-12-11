/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.jsecurity;

import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.nexus.NexusService;
import org.sonatype.nexus.configuration.NotifiableConfiguration;

public interface NexusSecurity
    extends ConfigurationManager, NexusService, NotifiableConfiguration
{
    void forgotPassword( String userId, String email )
        throws NoSuchUserException,
            NoSuchEmailException;

    void forgotUsername( String email )
        throws NoSuchEmailException;

    void resetPassword( String userId )
        throws NoSuchUserException;

    void changePassword( String userId, String oldPassword, String newPassword )
        throws NoSuchUserException,
            InvalidCredentialsException;

    void changePassword( String userId, String newPassword )
        throws NoSuchUserException;
}
