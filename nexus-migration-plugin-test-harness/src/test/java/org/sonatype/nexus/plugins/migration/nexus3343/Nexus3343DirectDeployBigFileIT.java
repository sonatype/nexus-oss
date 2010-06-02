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

import org.junit.Assert;
import org.restlet.data.Status;

public class Nexus3343DirectDeployBigFileIT
    extends AbstractBigFileIT
{

    @Override
    public File doTest()
        throws Exception
    {

        int m = getDeployUtils().deployUsingGavWithRest( "main-local-releases", GAV, getSourceFile() );
        Assert.assertTrue( Status.isSuccess( m ) );

        return new File( nexusWorkDir, "storage/main-local-releases/nexus3343/released/1.0/released-1.0.bin" );
    }

    @Override
    public File getSourceFile()
    {
        return new File( getTestFile( "." ), "sourceFile.bin" );
    }
}
