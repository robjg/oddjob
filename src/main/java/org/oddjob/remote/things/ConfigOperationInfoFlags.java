package org.oddjob.remote.things;

/**
 * Provide {@link ConfigOperationInfo} as flags.
 */
public class ConfigOperationInfoFlags implements ConfigOperationInfo {

    public static final int SUPPORTS_CONFIGURATION = 1;
    public static final int SUPPORTS_COPY = 2;
    public static final int SUPPORTS_CUT = 4;
    public static final int SUPPORTS_PASTE = 8;

    private final int supportsFlags;

    ConfigOperationInfoFlags(int supportsFlags) {
        this.supportsFlags = supportsFlags;
    }

    public static ConfigOperationInfoFlags from(int supportsFlags) {
        return new ConfigOperationInfoFlags(supportsFlags);
    }

    public int getSupportsFlags() {
        return supportsFlags;
    }

    public boolean isConfigurationSupported() {
        return (supportsFlags & SUPPORTS_CONFIGURATION) != 0;
    }

    public boolean isCopySupported() {
        return (supportsFlags & SUPPORTS_COPY) != 0;
    }

    public boolean isCutSupported() {
        return (supportsFlags & SUPPORTS_CUT) != 0;
    }

    public boolean isPasteSupported() {
        return (supportsFlags & SUPPORTS_PASTE) != 0;
    }

    public static class Builder {

        private int supportsFlags = 0;

        public ConfigOperationInfoFlags build() {
            return new ConfigOperationInfoFlags(supportsFlags);
        }

        public Builder withSupportsConfiguration(boolean supports) {
            if (supports) {
                supportsFlags = supportsFlags | SUPPORTS_CONFIGURATION;
            } else {
                supportsFlags = supportsFlags & ~SUPPORTS_CONFIGURATION;
            }
            return this;
        }

        public Builder withSupportsCopy(boolean supports) {
            if (supports) {
                supportsFlags = supportsFlags | SUPPORTS_COPY;
            } else {
                supportsFlags = supportsFlags & ~SUPPORTS_COPY;
            }
            return this;
        }

        public Builder withSupportsCut(boolean supports) {
            if (supports) {
                supportsFlags = supportsFlags | SUPPORTS_CUT;
            } else {
                supportsFlags = supportsFlags & ~SUPPORTS_CUT;
            }
            return this;
        }

        public Builder withSupportsPaste(boolean supports) {
            if (supports) {
                supportsFlags = supportsFlags | SUPPORTS_PASTE;
            } else {
                supportsFlags = supportsFlags & ~SUPPORTS_PASTE;
            }
            return this;
        }

    }
}
