package org.sonatype.nexus.geb.traits


trait ExtJS
{
  def idsForCQ(String cq) {
    return js.exec(cq, '''return Ext.pluck(Ext.ComponentQuery.query(arguments[0]), 'id')''')
  }
}
