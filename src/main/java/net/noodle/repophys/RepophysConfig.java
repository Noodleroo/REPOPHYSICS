package net.noodle.repophys;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class RepophysConfig {

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Client {
        // Raycast Settings
        public final ModConfigSpec.DoubleValue reachDistance;
        public final ModConfigSpec.DoubleValue holdDistance;
        public final ModConfigSpec.DoubleValue maxHoldDistance;
        public final ModConfigSpec.DoubleValue tractorBeamSmoothness;

        // Custom Highlight Colors
        public final ModConfigSpec.DoubleValue tractorBoxR;
        public final ModConfigSpec.DoubleValue tractorBoxG;
        public final ModConfigSpec.DoubleValue tractorBoxB;
        public final ModConfigSpec.DoubleValue tractorBoxA;



        // Debug Toggles
        public final ModConfigSpec.BooleanValue enableChatLogging;
        public final ModConfigSpec.BooleanValue enableActionbarHeartbeat;

        public Client(ModConfigSpec.Builder builder) {
            builder.comment("Tractor Beam Physics & Mechanical Configurations").push("physics");

            reachDistance = builder
                    .comment("How far away (in blocks) you can scan and lock onto entities.")
                    .defineInRange("reachDistance", 6.0D, 1.0D, 64.0D);

            holdDistance = builder
                    .comment("The distance (in blocks) the grabbed entity floats in front of the player.")
                    .defineInRange("holdDistance", 2.5D, 0.5D, 16.0D);

            maxHoldDistance = builder
                    .comment("The threshold distance before the beam snaps and drops the object.")
                    .defineInRange("maxHoldDistance", 8.0D, 2.0D, 128.0D);

            tractorBeamSmoothness = builder
                    .comment(" The smoothing interpolation weight for rendering a gripped entity on the client. Lower = floatier/heavier trailing, Higher = immediate/tight crosshair snapping. Range: 0.01 to 1.0")
                    .defineInRange("tractorBeamSmoothness", 1.D, 0.01D, 1.0D);

            builder.pop();

            builder.comment("Visual Overlay Configuration (Color Channels range from 0.0 to 1.0)").push("visuals");

            tractorBoxR = builder.comment("Red color component").defineInRange("boxRed", 0.0D, 0.0D, 1.0D);
            tractorBoxG = builder.comment("Green color component").defineInRange("boxGreen", 0.8D, 0.0D, 1.0D);
            tractorBoxB = builder.comment("Blue color component").defineInRange("boxBlue", 1.0D, 0.0D, 1.0D);
            tractorBoxA = builder.comment("Alpha component (Opacity/Transparency)").defineInRange("boxAlpha", 1.0D, 0.0D, 1.0D);

            builder.pop();

            builder.comment("Developer Debug Indicators").push("debug");

            enableChatLogging = builder
                    .comment("Should the tractor beam broadcast target locking notices to chat?")
                    .define("enableChatLogging", true);

            enableActionbarHeartbeat = builder
                    .comment("Should the background tick loop post a pulsing 'Active' status update on the actionbar?")
                    .define("enableActionbarHeartbeat", true);

            builder.pop();
        }
    }
}