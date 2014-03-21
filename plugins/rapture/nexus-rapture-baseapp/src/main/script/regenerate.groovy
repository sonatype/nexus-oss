/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
//
// Regenerates the baseapp js/css/images for the current configuration/style.
//

println ''
println 'Prepare:'

File basedir = project.basedir
println "Base directory: $basedir"

File outdir = new File(basedir, 'src/main/resources/static/rapture').canonicalFile
println "Output directory: $outdir"

// Start out clean
ant.delete() {
  fileset(dir: outdir) {
    include(name: '**')
  }
}

ant.mkdir(dir: outdir)

// ensure we can execute Sencha CMD
ant.exec(executable: 'sencha', failonerror: true) {
  arg(line: 'which')
}

// generate files for production and testing
[ 'production', 'testing' ].each { flavor ->
  println ''
  println "Generating: $flavor"

  // run 'app build' for each flavor
  ant.exec(executable: 'sencha', dir: "$basedir/src/main/baseapp", failonerror: true) {
    arg(line: "app build $flavor")
  }
}

println ''
println 'Cleaning up:'

// remove some cruft that sencha cmd generates
ant.delete() {
  fileset(dir: outdir) {
    include(name: 'config.rb')
    include(name: 'index.html')
    include(name: 'resources/Readme.md')
  }
}

println ''
println 'Changes:'

ant.exec(executable: 'git') {
  arg(line: 'status -s .')
}

println ''
println 'Done'
println ''


