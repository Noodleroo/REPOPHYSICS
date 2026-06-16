package net.noodle.repophys.util; // Adjust to match your package structure

public class PlayerPowerUps {

    // --- GRABBER MODIFIERS ---
    /** Increases the strength of the grabber. Default is 1.0 (100%). Upgrades add 0.25 (25%). */
    public static double strengthMultiplier = 1.0D;

    /** Increases the range of the grabber. Default is 1.0 (100%). Upgrades add 0.25 (25%). */
    public static double rangeMultiplier = 1.0D;


    // --- STAMINA MODIFIERS ---
    /** Increases maximum stamina. Default base is usually 100. */
    public static int extraMaxStamina = 0;

    /** Speed of stamina regen per second while using crouch rest (Not moving). */
    public static double crouchRestRegenBonusStatic = 1.0D;

    /** Speed of stamina regen per second while moving and crouching. */
    public static double crouchRestRegenBonusMoving = 0.5D;


    // --- MOVEMENT MODIFIERS ---
    /** Increases sprint speed. Default is 1.0 (100%). Upgrades add 0.20 (20%). */
    public static double sprintSpeedMultiplier = 1.0D;

    /** Increases max amount of midair jumps (Double jumps, triple jumps, etc.). */
    public static int extraJumpsCount = 0;


    // --- VITALITY MODIFIERS ---
    /** Increases maximum health. Default base in Minecraft is 20 (10 hearts). */
    public static float extraMaxHealth = 0.0F;
}