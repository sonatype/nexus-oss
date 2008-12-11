/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.configuration.security.validator;

import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * The validator used to validate current configuration in boot-up sequence.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationValidator extends ConfigurationValidator
{
    String ROLE = SecurityConfigurationValidator.class.getName();
    
    /**
     * Validate a user configuration
     * 
     * @param ctx
     * @param user
     * @return
     */
    ValidationResponse validateUser( SecurityValidationContext ctx, CUser user, boolean update );
    
    /**
     * Validate a role configuration
     * 
     * @param ctx
     * @param role
     * @return
     */
    ValidationResponse validateRole( SecurityValidationContext ctx, CRole role, boolean update );
    
    /**
     * Validate a repository target privilege configuration
     * 
     * @param ctx
     * @param privilege
     * @return
     */
    ValidationResponse validateRepoTargetPrivilege( SecurityValidationContext ctx, CRepoTargetPrivilege privilege, boolean update );
    
    /**
     * Validate an application privilege configuration
     * 
     * @param ctx
     * @param privilege
     * @return
     */
    ValidationResponse validateApplicationPrivilege( SecurityValidationContext ctx, CApplicationPrivilege privilege, boolean update );    
}
