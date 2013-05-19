/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.tools;

import org.sonatype.security.model.Configuration;

/**
 * A DynamicSecurityResource all for other components/plugins to contributes users/roles/privileges to the security model.
 * 
 * @author Brian Demers
 */
public interface DynamicSecurityResource
{
    /**
     * Gets the security configuration.
     * 
     * @return
     */
    Configuration getConfiguration();

    /**
     * Marks the Configuration dirty so it can be reloaded.
     * 
     * @return
     */
    boolean isDirty();
}
