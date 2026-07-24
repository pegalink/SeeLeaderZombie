package net.robotic.seeleaderzombie.utils;

import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import java.util.Set;

public class GetCurrentVersion {
    private static final Set<String> SUPPORTED_VERSIONS = Set.of(
        "26.1",
        "26.1.2",
        "26.2"
    );
    private static final WorldVersion version = SharedConstants.getCurrentVersion();
    private static final String versionName = version.toString();

    public static boolean isTargetVersionStatic() {
        return SUPPORTED_VERSIONS.contains(versionName);
    }

    public boolean isTargetVersion() {
        return isTargetVersionStatic();
    }

    public String whichVersion() {
        return versionName;
    }
}
