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
package org.sonatype.security.configuration.upgrade;

import org.sonatype.configuration.upgrade.ConfigurationUpgrader;
import org.sonatype.security.configuration.model.SecurityConfiguration;

/**
 * Defines a type for upgraders of security-configuration.xml.
 * This is only used if an old version is detected and needs to be upgraded
 * 
 * @author Steve Carlucci
 * @since 3.1
 */
public interface SecurityConfigurationUpgrader
    extends ConfigurationUpgrader<SecurityConfiguration>
{

}