package net.noodle.repophys.blocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.blocks.debug_block.GrabPhysicsBlock;
import net.noodle.repophys.blocks.debug_block.GrabPhysicsBlockEntity;

import java.util.function.Supplier;

public class ModBlocks {
//    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Repophys.MODID);
//    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Repophys.MODID);
//    // Add the Block Entity Type registry
//    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Repophys.MODID);
//
//    public static final DeferredHolder<Block, GrabPhysicsBlock> GRAB_PHYSICS_BLOCK = registerBlock("grab_physics_block",
//            () -> new GrabPhysicsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
//
//    // Register the Block Entity Type mapping it to our block
//    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrabPhysicsBlockEntity>> GRAB_PHYSICS_BE_TYPE =
//            BLOCK_ENTITIES.register("grab_physics_be", () -> BlockEntityType.Builder.of(GrabPhysicsBlockEntity::new, GRAB_PHYSICS_BLOCK.get()).build(null));
//
//    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> blockSupplier) {
//        DeferredHolder<Block, T> block = BLOCKS.register(name, blockSupplier);
//        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
//        return block;
//    }
//
//    public static void register(IEventBus eventBus) {
//        BLOCKS.register(eventBus);
//        ITEMS.register(eventBus);
//        BLOCK_ENTITIES.register(eventBus); // Make sure this is added!
//    }
}