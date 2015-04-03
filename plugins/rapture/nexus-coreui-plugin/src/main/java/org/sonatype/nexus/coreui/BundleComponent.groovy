package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.sonatype.nexus.extdirect.DirectComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * OSGI bundle component.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = "coreui_Bundle")
class BundleComponent
    extends DirectComponentSupport
{
  @Inject
  BundleContext bundleContext

  @DirectMethod
  List<BundleXO> read() {
    def result = []
    bundleContext.bundles.each { bundle ->
      result << new BundleXO(
          id: bundle.bundleId,
          state: state(bundle.state),
          symbolicName: bundle.symbolicName,
          version: bundle.version.toString(),
          location: bundle.location,
      )
    }
    return result
  }

  private String state(int value) {
    switch (value) {
      case Bundle.UNINSTALLED:
        return 'UNINSTALLED'
      case Bundle.INSTALLED:
        return 'INSTALLED'
      case Bundle.RESOLVED:
        return 'RESOLVED'
      case Bundle.STARTING:
        return 'STARTING'
      case Bundle.STOPPING:
        return 'STARTING'
      case Bundle.ACTIVE:
        return 'ACTIVE'
      default:
        return 'UNKNOWN'
    }
  }
}
