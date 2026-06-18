package net.noodle.repophys.physics;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;

public class RepoSubLevelObserver implements SubLevelObserver {
    private final ServerLevel level;

    public RepoSubLevelObserver(ServerLevel level) {
        this.level = level;
    }

    @Override
    public void tick(SubLevelContainer subLevels) {
        // optional world tick logic
    }

    public void physicsTick(SubLevelPhysicsSystem physicsSystem) {
        ObjectGrabber.physicsTick(physicsSystem);
    }

    @Override
    public void onSubLevelAdded(SubLevel subLevel) {}

    @Override
    public void onSubLevelRemoved(SubLevel subLevel, SubLevelRemovalReason reason) {}
}



