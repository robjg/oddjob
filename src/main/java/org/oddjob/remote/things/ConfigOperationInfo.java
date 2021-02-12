package org.oddjob.remote.things;

/**
 * Provides information about what a operations an {@link ConfigPoint} supports.
 */
public interface ConfigOperationInfo {

    boolean isConfigurationSupported();

    boolean isCopySupported();

    boolean isCutSupported();

    boolean isPasteSupported();

    static ConfigOperationInfoFlags.Builder builder() {
        return new ConfigOperationInfoFlags.Builder();
    }
}
