package org.sonatype.nexus.bundle.launcher.internal;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import com.sun.istack.internal.Nullable;
import java.io.File;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.gzip.GZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.bundle.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.ManagedNexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleLauncher;
import org.sonatype.nexus.bundle.launcher.NexusBundleService;
import org.sonatype.nexus.bundle.launcher.util.ArtifactResolver;
import org.sonatype.nexus.bundle.launcher.util.PortReservationService;

/**
 *
 * @author plynch
 */
@Named("default")
@Singleton
public class DefaultNexusBundleLauncher implements NexusBundleLauncher, NexusBundleService {

    private Logger logger = LoggerFactory.getLogger(DefaultNexusBundleLauncher.class);
    /**
     * Used to resolve bundles and bundle plugins
     */
    private final ArtifactResolver artifactResolver;
    /**
     * service for reserving free ports used by bundles
     */
    private final PortReservationService portReservationService;
    private final Provider<ZipUnArchiver> zipUnArchiverProvider;
    private final Provider<GZipUnArchiver> gzipUnArchiverProvider;
    /**
     * Directory where the service performs it's work
     */
    private final File serviceWorkDirectory;
    /**
     * Root Directory where bundle overlays are looked up
     */
    private final File overlaysSourceDirectory;

    @Inject
    public DefaultNexusBundleLauncher(final ArtifactResolver artifactResolver, final PortReservationService portReservationService, final Provider<ZipUnArchiver> zipUnArchiverProvider, final Provider<GZipUnArchiver> gzipUnArchiverProvider, @Named("${NexusBundleServce.serviceWorkDirectory:-target/nbs}") final File serviceWorkDirectory, @Named("${NexusBundleServce.overlaySourceDirectory:-target/overlays}") @Nullable final File overlaysSourceDirectory) {
        Preconditions.checkNotNull(artifactResolver);
        Preconditions.checkNotNull(portReservationService);
        Preconditions.checkNotNull(serviceWorkDirectory);
        Preconditions.checkNotNull(zipUnArchiverProvider);
        Preconditions.checkNotNull(gzipUnArchiverProvider);

        // required
        this.artifactResolver = artifactResolver;
        this.portReservationService = portReservationService;
        this.serviceWorkDirectory = serviceWorkDirectory;
        this.zipUnArchiverProvider = zipUnArchiverProvider;
        this.gzipUnArchiverProvider = gzipUnArchiverProvider;

        // optional
        this.overlaysSourceDirectory = overlaysSourceDirectory;

        logger.debug(serviceWorkDirectory.getAbsolutePath());
        logger.debug(overlaysSourceDirectory.getAbsolutePath());

        makeServiceDirectories();

    }

    /**
     * Make the required directories for the service to operate
     */
    protected final void makeServiceDirectories() {
        this.serviceWorkDirectory.mkdirs();
    }

    protected File resolveArtifact(final String artifactCoordinates) {
        File file = artifactResolver.resolve(artifactCoordinates);
        if (file == null) {
            throw new IllegalStateException("Bundle " + artifactCoordinates + " cannot be resolved to a file.");
        }
        if (!file.isFile()) {
            throw new IllegalStateException("Bundle " + artifactCoordinates + " is not a file. (" + file.getAbsolutePath() + ")");
        }
        return file;
    }

    protected void extract(File bundleFile, File toDir) {
        String extension = FileUtils.extension(bundleFile.getName());
        UnArchiver unArchiver;
        if ("zip".equals(extension)) {
            unArchiver = zipUnArchiverProvider.get();
        } else {
            unArchiver = gzipUnArchiverProvider.get();
        }
        unArchiver.setSourceFile(bundleFile);
        unArchiver.setDestDirectory(toDir);
        try {
            unArchiver.extract();
        } catch (Exception e) {
            throw new RuntimeException("Unable to unpack " + bundleFile, e);
        }
    }

    /**
     *
     * @param overlayRootDir the root of the overlay to copy onto the bundle root dir
     * @param bundleRootDir the dir where a bundle root lives
     */
    protected void overlay(File overlayRootDir, File bundleRootDir) {
    }

    // ================== NexusBundleLauncher =========================
    public ManagedNexusBundle start(final NexusBundleConfiguration bundleConfiguration) {
        File bundleFile = resolveArtifact(bundleConfiguration.getBundleArtifactCoordinates());
        return null;
    }

    @Override
    public ManagedNexusBundle start(NexusBundleConfiguration config, String groupName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop(ManagedNexusBundle managedNexusbundle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopAll(String groupName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
