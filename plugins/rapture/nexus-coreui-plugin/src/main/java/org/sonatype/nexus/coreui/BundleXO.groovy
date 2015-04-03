package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import org.sonatype.nexus.extdirect.DirectComponentSupport

import javax.inject.Named
import javax.inject.Singleton

/**
 * OSGI bundle.
 *
 * @since 3.0
 */
class BundleXO
{
  String id
  String state
  String symbolicName
  String location
  String version
}
