/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.leveldb;

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

/**
 * A manager of databases that removes the burden of finding a proper place for, and closing them out (at Nexus
 * shutdown). Primarily, it is meant to ease of use of LevelDB in cases when database instances are "long living". Using
 * this manager all you have to use {@link #openManaged(String, Options)} method, keep and use the instance as you wish,
 * and it removes the burden of finding place/file to store the database files and to manage it's lifecycle, as all
 * "managed" databases will be closed once, when Nexus instance shuts down. Still, nothing prevents you to use the
 * "native" LevelDB way of creating a database that suits your needs, using the factory method {@link
 * org.iq80.leveldb.impl.Iq80DBFactory.open(File, Options)} (as plugin also exports whole LevelDB too). Databases
 * created in that way, will need to be manually shut down when no needed, and also, you will need to ensure to find a
 * proper location to store the database files.
 * <p>
 * A note about {@code dbKey}s, that are actually plain strings: in current implementation, those will be used to
 * calculate (if no explicit {@link File} passed in) the location of the database, and will end up in path. Hence, only
 * "file system safe" characters should be used for now (currently nothing enforces this!). Keys with prefix "nx-" are
 * reserved for Nexus internal use (this is not enforced eitherF).
 * 
 * @author cstamas
 * @see <a href="https://github.com/dain/leveldb">LevelDB in Java</a>
 * @since 2.7.0
 */
public interface LevelDbManager
{
    /**
     * Creates and opens a "managed" LevelDB instance for caller, keyed with {@code dbKey} parameter and using passed in
     * {@link Options}. Internally, LevelDB manager maintains a registry of databases, and will prevent creation of
     * another database using same key. Location of the database will be the default location in Nexus (in it's work
     * directory).
     * 
     * @param dbKey a DB key, never {@code null}.
     * @param options LevelDB options, never {@code null}.
     * @throws IllegalArgumentException if DB keyed with {@code dbKey} already exists.
     * @throws IOException see {@link org.iq80.leveldb.impl.Iq80DBFactory.open(File, Options)}.
     */
    DB openManaged( String dbKey, Options options )
        throws IOException, IllegalArgumentException;

    /**
     * Creates and opens a "managed" LevelDB instance for caller, keyed with {@code dbKey} parameter, stored at location
     * {@code file} parameter (it has to be non existent or existent directory), and using passed in {@link Options}.
     * Internally, LevelDB manager maintains a registry of databases, and will prevent creation of another database
     * using same key. Location of the database will be the default location in Nexus (in it's work directory).
     * 
     * @param dbKey a DB key, never {@code null}.
     * @param options LevelDB options, never {@code null}.
     * @throws IllegalArgumentException if DB keyed with {@code dbKey} already exists, or if passed in file exists and
     *             is not a directory.
     * @throws IOException see {@link org.iq80.leveldb.impl.Iq80DBFactory.open(File, Options)}.
     */
    DB openManaged( String dbKey, File file, Options options )
        throws IOException, IllegalArgumentException;

    /**
     * Returns the managed DB instance opened under key {@code dbKey}. If there is no DB under given key, {@code null}
     * is returned.
     * 
     * @param dbKey a DB key, never {@code null}.
     */
    DB getManaged( String dbKey );

    /**
     * Closes the managed DB instance opened under the key {@code dbKey}, and removes instance from internal structure
     * (close on it will not be attempted on Nexus shutdown). If there is no DB under given key, nothing happens, call
     * will return cleanly.
     * 
     * @param dbKey a DB key, never {@code null}.
     */
    void closeManaged( String dbKey );
}
