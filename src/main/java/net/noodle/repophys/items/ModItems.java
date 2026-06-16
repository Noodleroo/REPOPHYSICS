package net.noodle.repophys.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.noodle.repophys.content.RepoStaffItem;

public class ModItems {
    // 1. Create the registry container matching your mod id
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("repophys");

    // 2. Register your custom PhysicsGrabberItem class!
    public static final DeferredItem<Item> PHYSICS_GRABBER = ITEMS.register("physics_grabber",
            () -> new PhysicsGrabberItem(new Item.Properties()));

    public static final DeferredItem<Item> DEBUG_SUPER_SHOVEL = ITEMS.register("debug_super_shovel",
            () -> new DebugSuperShovel(new Item.Properties()));


    public static final DeferredHolder<Item, RepoStaffItem> REPO_STAFF = ITEMS.register("repo_staff",
            () -> new RepoStaffItem(new Item.Properties())
    );

    // 3. Helper method to bind this registry to your main mod startup lifecycle
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}