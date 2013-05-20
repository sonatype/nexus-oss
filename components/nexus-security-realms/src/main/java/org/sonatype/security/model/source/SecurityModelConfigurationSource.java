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
package org.sonatype.security.model.source;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.configuration.source.ConfigurationSource;
import org.sonatype.security.model.Configuration;

/**
 * The Interface ApplicationConfigurationSource, responsible to fetch security configuration by some means. It also
 * stores one instance of Configuration object maintained thru life of the application. This component is also able to
 * persist security config.
 * 
 * @author cstamas
 */
public interface SecurityModelConfigurationSource
    extends ConfigurationSource<Configuration>
{

    InputStream getConfigurationAsStream()
        throws IOException;

    void backupConfiguration()
        throws IOException;

}
