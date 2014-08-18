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
// Some whackyness here as we want to keep the Sencha CMD configuration using the target output directory
// so that dynamic watching works, but when regenerating, we don't want flavor generation to interact with each other
//

println ''
println 'Prepare:'

File basedir = project.basedir.canonicalFile
println "Base directory: $basedir"

File outdir = new File(basedir, 'src/main/resources/static/rapture')
println "Output directory: $outdir"

// ensure we can execute Sencha CMD
ant.exec(executable: 'sencha', failonerror: true) {
  arg(line: 'which')
}

// build flavors
def flavors = [
  'testing',
  'production'
]

// Reset the output directory
resetOutDir = {
  if (outdir.exists()) {
    ant.delete(dir: outdir)
  }
  ant.mkdir(dir: outdir)
}

// display a section banner
def banner = { message ->
  println ''
  println '-' * 79
  println message
}

// generate flavor files
flavors.each { flavor ->
  banner "Generating: $flavor"

  resetOutDir()

  // run 'app build' for the flavor
  ant.exec(executable: 'sencha', dir: "$basedir/src/main/baseapp", failonerror: true) {
    arg(line: "app build $flavor")
  }

  // Copy all flavor build outputs we care about
  def flavordir = new File(basedir, "target/flavors/$flavor")
  ant.mkdir(dir: flavordir)
  ant.copy(todir: flavordir) {
    fileset(dir: outdir) {
      include(name: 'baseapp-*.js')
      include(name: 'resources/baseapp-*.css')
      include(name: 'resources/images/**')
    }
  }
}

// Reset the output directory for installing flavor files
resetOutDir()

// install flavor files
flavors.each { flavor ->
  banner "Installing: $flavor"

  def flavordir = new File(basedir, "target/flavors/$flavor")
  ant.copy(todir: outdir) {
    fileset(dir: flavordir) {
      include(name: '**')
    }
  }
}

// Show all changed files to remind to check-in when ready
banner 'Changes:'
ant.exec(executable: 'git') {
  arg(value: 'add')
  arg(file: outdir)
}
ant.exec(executable: 'git') {
  arg(value: 'status')
  arg(value: '-s')
  arg(file: outdir)
}

println ''
println 'Done'
println ''


