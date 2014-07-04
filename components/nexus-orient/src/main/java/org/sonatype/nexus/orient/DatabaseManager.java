package org.sonatype.nexus.orient;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

/**
 * Orient database manager.
 *
 * @since 3.0
 */
public interface DatabaseManager
{
  ODatabaseDocumentTx connect(String name, boolean create);

  ODatabaseDocumentTx connect(String name);

  ODatabaseDocumentPool pool(String name);
}