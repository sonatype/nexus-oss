/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.migration.nexus3343;

import java.io.File;
import java.net.URL;

public class Nexus3343DownloadBigFileIT
    extends AbstractBigFileIT
{

    @Override
    public File doTest()
        throws Exception
    {
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                + "/artifactory/main-local/nexus3343/released/1.0/released-1.0.bin" );
        return downloadFile( url, "target/downloads/nexus3343" );
    }

    @Override
    public File getSourceFile()
    {
        return new File( nexusWorkDir, "storage/main-local-releases/nexus3343/released/1.0/released-1.0.bin" );
    }

}
