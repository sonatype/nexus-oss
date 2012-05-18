/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nexus5057;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.*;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;
import org.testng.annotations.Test;

/**
 * <b>TODO:</b> Delete this java doc comment when accepted into version control.
 * Perhaps provide some docs on creating IT tests. I've noted down the steps
 * I've taken.
 * <p>
 * From what I can gather the <code>testRepositoryId</code> passed into the
 * super constructor specifies both the nexus configuration file to use (located
 * at resources\<i>testRepositoryId</i>\test-config\nexus.xml) and the
 * repository (located at resources\proxyRepo\<i>testRepositoryId</i>)
 * </p>
 * <p>
 * This test case is very similar to the p2r0X test cases.
 * </p>
 * <p>
 * Artifacts that need to be already deployed into the repository need to be
 * located at resources\<i>testRepositoryId</i>\artifacts\jars. Each artifact
 * lives in its own subdirectory and contains the jar to be deployed and a
 * pom.xml file which needs to a tweaked <code>distributionManagement</code>
 * section that specifies the content repository. e.g:
 *
 * <pre>
 *   {@code <distributionManagement>
 *     <repository>
 *       <id>nexus-test-harness-repo</id>
 *       <url>$}{{@code nexus-base-url}}{@code content/repositories/nexus5057</url>
 *     </repository>
 *   </distributionManagement>
 * }
 * </pre>
 *
 * </p>
 */
public class NEXUS5057EnsureEclipseBundleShapeIsSupportedIT extends
        AbstractNexusP2GeneratorIT {

    public NEXUS5057EnsureEclipseBundleShapeIsSupportedIT() {
        super("nexus5057");
    }

    /**
     * See <a
     * href="https://issues.sonatype.org/browse/NEXUS-5057">NEXUS-5057</a>.
     *
     * When a bundle is deployed that specifies
     * <code>Eclipse-BundleShape: dir</code> in the MANIFEST.MF then the
     * <code>*-p2Content.xml</code> file should include the correct instructions
     * for whether the content has been zipped.
     * <p>
     * <ul>
     * <li>repo location during test run =
     * target/nexus/nexus-work-dir/storage/nexus5057/</li>
     * <li>p2Artifact file =
     * test/nexus5057/1.0.0/nexus5057-1.0.0-p2Artifacts.xml</li>
     * <li>p2Content file = test/nexus5057/1.0.0/nexus5057-1.0.0-p2Content.xml</li>
     * </ul>
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        createP2MetadataGeneratorCapability();
        createP2RepositoryAggregatorCapability();

        deployArtifacts(getTestResourceAsFile("artifacts/jars"));

        // ensure link created
        final File file = downloadFile(new URL(getNexusTestRepoUrl()
                + "/.meta/p2/plugins/test.nexus5057_1.0.0.jar"), new File(
                "target/downloads/" + this.getClass().getSimpleName()
                        + "/test.nexus5057_1.0.0.jar").getCanonicalPath());
        assertThat(file, is(readable()));

        // ensure p2Content created
        final File p2Content = downloadP2ContentFor("test", "nexus5057",
                "1.0.0");
        assertThat("p2Content has been downloaded", p2Content,
                is(notNullValue()));
        assertThat("p2Content exists", p2Content.exists(), is(true));
        // ensure https://issues.sonatype.org/browse/NEXUS-5057 includes "zipped" instruction
        StringBuilder expected = new StringBuilder();
        expected.append("          <instruction key='zipped'>");
        expected.append(IOUtils.LINE_SEPARATOR);
        expected.append("            true");
        expected.append(IOUtils.LINE_SEPARATOR);
        expected.append("          </instruction>");
        assertThat(p2Content, contains(expected.toString()));

        // ensure repositories are valid
        final File installDir = new File("target/eclipse/nexus5057");

        installUsingP2(getNexusTestRepoUrl() + "/.meta/p2", "test.nexus5057",
                installDir.getCanonicalPath());

        // ensure that bundle is unzipped
        final File bundle = new File( installDir, "plugins/test.nexus5057_1.0.0.jar" );
        assertThat( bundle, not( exists() ) );

        final File bundleDir = new File(installDir,
                "plugins/test.nexus5057_1.0.0/nexus5057/placeholder.txt");
        assertThat( "bundle must be unzipped when installed", bundleDir, is(readable()));

    }

}
