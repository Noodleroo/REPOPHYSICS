package com.noodleroo.repophysics.util;

import net.neoforged.fml.ModList;

public class RepoModCheck {
    public static final String LEVELS_ID = "repolevels";
    public static final String MONSTERS_ID = "repomonsters";
    public static final String VOICE_ID = "repovoice";

    public static boolean isLevelsInstalled() {
        return ModList.get().isLoaded(LEVELS_ID);
    }

    public static boolean isMonstersInstalled() {
        return ModList.get().isLoaded(MONSTERS_ID);
    }

    public static boolean isVoiceInstalled() {
        return ModList.get().isLoaded(VOICE_ID);
    }
}