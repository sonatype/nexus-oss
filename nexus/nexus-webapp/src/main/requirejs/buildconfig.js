/*
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
({
    // need to root the dir or it's relative to /tmp...
    //appDir: "${project.build.directory}/../src/main/webapp",
    appDir: "../../../src/main/webapp",
    baseUrl: "js",
    dir: "../../../target/generated-sources/yuicompressor",
    //dir: "${project.build.directory}/generated-sources/yuicompressor",
    optimize: 'none',
    modules: [
        {
            name: "nexus-init"
        }
    ]
})
