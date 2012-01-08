/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities;

import java.util.Map;

/**
 * Validates capability properties.
 * 
 * The validators to be used are extracted as follows:<b/>
 * On create:<b/>
 * * Automatically created validators for all mandatory fields and fields supporting regexp validation<b/>
 * * Validators returned by {@link CapabilityDescriptor#validator()} method<b/>
 * * {@link CapabilityFactory}, if it implements {@link Validator}
 *
 * On update:<b/>
 * * Automatically created validators for all mandatory fields and fields supporting regexp validation<b/>
 * * Validators returned by {@link CapabilityDescriptor#validator(CapabilityIdentity)} method<b/>
 * * {@link Capability}, if it implements {@link Validator}
 *
 * @since 2.0
 */
public interface Validator
{

    /**
     * Validates capability properties before a capability is created/updated.
     *
     * @param properties capability properties that will be applied to capability
     * @return validation result
     */
    ValidationResult validate( Map<String, String> properties );

    /**
     * Describe when validation will pass.
     *
     * @return description
     */
    String explainValid();

    /**
     * Describe when validation will fail.
     *
     * @return description
     */
    String explainInvalid();

}
