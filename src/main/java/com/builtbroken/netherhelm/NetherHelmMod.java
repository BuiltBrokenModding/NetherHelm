package com.builtbroken.netherhelm;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = NetherHelmMod.MODID)
@Mod(modid = NetherHelmMod.MODID, name = NetherHelmMod.NAME, version = NetherHelmMod.VERSION)
public class NetherHelmMod {

    public static final String MODID = "netherhelm";
    public static final String NAME = "Nether Helm";
    public static final String VERSION = "@VERSION@";

    public static Item nether_helm_component = new Item().setRegistryName(MODID, "nether_helm_component").setCreativeTab(CreativeTabs.TOOLS).setTranslationKey("netherhelm.nether_helm_component");
    public static Map<ItemArmor, ItemMimicArmor> mimic_armors = new HashMap<ItemArmor, ItemMimicArmor>();

    @Config(modid = NetherHelmMod.MODID)
    public static class Configuration {
        @Config.RequiresMcRestart
        @Config.Comment("List of armor item IDs that are valid for crafting. Clients must have matching configs due to item registration, recommended for modpack use only!")
        public static String[] affected_armors = new String[] {"minecraft:iron_helmet", "minecraft:leather_helmet", "minecraft:golden_helmet", "minecraft:diamond_helmet"};
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        for(ItemMimicArmor armor : mimic_armors.values()) {
            GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, "nether_helm_to_mimic_" + armor.parent.getRegistryName().getPath()), armor.getRegistryName(), new ItemStack(armor), Ingredient.fromItem(armor.parent), Ingredient.fromItem(nether_helm_component));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(nether_helm_component);
        for(String armor : Configuration.affected_armors) {
            Item item = event.getRegistry().getValue(new ResourceLocation(armor));
            if(item != null) {
                if(item instanceof ItemArmor) {
                    if(!(item instanceof ItemMimicArmor)) {
                        ItemMimicArmor mimic = new ItemMimicArmor((ItemArmor) item);
                        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                            Supplier<Runnable> runMe = () -> () -> mimic.setTileEntityItemStackRenderer(new MimicArmorTEISR());
                            runMe.get().run();
                        }
                        mimic_armors.put(mimic.parent, mimic);
                        event.getRegistry().register(mimic);
                    }
                } else {
                    LogManager.getLogger().warn("There is a registry for " + armor + ", but it is not an equippable armor!");
                }
            } else {
                LogManager.getLogger().warn("Could not find registry for " + armor + ", skipping.");
            }
        }

    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if(event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().getEntityWorld().isRemote && event.getEntity().world != null) {
            ItemStack newStack = event.getTo();
            if(newStack.getItem() instanceof ItemMimicArmor && event.getSlot() != EntityEquipmentSlot.MAINHAND && event.getSlot() != EntityEquipmentSlot.OFFHAND) {
                int dim = -1;
                if(newStack.hasTagCompound() && newStack.getTagCompound().hasKey("netherhelm:target_dimension", Constants.NBT.TAG_INT)) {
                    dim = newStack.getTagCompound().getInteger("netherhelm:target_dimension");
                }
                EntityPlayer player = (EntityPlayer) event.getEntityLiving();
                if(dim != player.world.provider.getDimension()) {
                    WorldServer dest = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
                    if(dest != null) {
                        player.setItemStackToSlot(event.getSlot(), ItemStack.EMPTY);
                        player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, newStack));
                        BlockPos pos = getNetherPosition(dest, player);
                        player.changeDimension(dim, new Teleporter(dest) {
                            @Override
                            public void placeInPortal(Entity entityIn, float rotationYaw) {}
                            @Override
                            public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {return false;}
                            @Override
                            public boolean makePortal(Entity entityIn) {return false;}
                            @Override
                            public void removeStalePortalLocations(long worldTime) {}
                            @Override
                            public void placeEntity(World world, Entity entity, float yaw) {
                                player.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), entity.rotationYaw, entity.rotationPitch);
                                player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                            }
                        });
                        player.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), player.rotationYaw, player.rotationPitch);
                        player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                    }
                }
            }
        }
    }
    
    public static BlockPos getNetherPosition(World world, Entity entityIn) {
        // from makePortal in Teleporter
        double d0 = -1.0D;
        int j = MathHelper.floor(entityIn.posX);
        int k = MathHelper.floor(entityIn.posY);
        int l = MathHelper.floor(entityIn.posZ);
        int i1 = j;
        int j1 = k;
        int k1 = l;
        int i2 = new Random().nextInt(4);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j2 = j - 16; j2 <= j + 16; ++j2)
        {
            double d1 = (double)j2 + 0.5D - entityIn.posX;

            for (int l2 = l - 16; l2 <= l + 16; ++l2)
            {
                double d2 = (double)l2 + 0.5D - entityIn.posZ;
                label293:

                for (int j3 = world.getActualHeight() - 1; j3 >= 0; --j3)
                {
                    if (world.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3, l2)))
                    {
                        while (j3 > 0 && world.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3 - 1, l2)))
                        {
                            --j3;
                        }

                        for (int k3 = i2; k3 < i2 + 4; ++k3)
                        {
                            int l3 = k3 % 2;
                            int i4 = 1 - l3;

                            if (k3 % 4 >= 2)
                            {
                                l3 = -l3;
                                i4 = -i4;
                            }

                            for (int j4 = 0; j4 < 3; ++j4)
                            {
                                for (int k4 = 0; k4 < 4; ++k4)
                                {
                                    for (int l4 = -1; l4 < 4; ++l4)
                                    {
                                        int i5 = j2 + (k4 - 1) * l3 + j4 * i4;
                                        int j5 = j3 + l4;
                                        int k5 = l2 + (k4 - 1) * i4 - j4 * l3;
                                        blockpos$mutableblockpos.setPos(i5, j5, k5);

                                        if (l4 < 0 && !world.getBlockState(blockpos$mutableblockpos).getMaterial().isSolid() || l4 >= 0 && !world.isAirBlock(blockpos$mutableblockpos))
                                        {
                                            continue label293;
                                        }
                                    }
                                }
                            }

                            double d5 = (double)j3 + 0.5D - entityIn.posY;
                            double d7 = d1 * d1 + d5 * d5 + d2 * d2;

                            if (d0 < 0.0D || d7 < d0)
                            {
                                d0 = d7;
                                i1 = j2;
                                j1 = j3;
                                k1 = l2;
                            }
                        }
                    }
                }
            }
        }

        if (d0 < 0.0D)
        {
            for (int l5 = j - 16; l5 <= j + 16; ++l5)
            {
                double d3 = (double)l5 + 0.5D - entityIn.posX;

                for (int j6 = l - 16; j6 <= l + 16; ++j6)
                {
                    double d4 = (double)j6 + 0.5D - entityIn.posZ;
                    label231:

                    for (int i7 = world.getActualHeight() - 1; i7 >= 0; --i7)
                    {
                        if (world.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7, j6)))
                        {
                            while (i7 > 0 && world.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7 - 1, j6)))
                            {
                                --i7;
                            }

                            for (int k7 = i2; k7 < i2 + 2; ++k7)
                            {
                                int j8 = k7 % 2;
                                int j9 = 1 - j8;

                                for (int j10 = 0; j10 < 4; ++j10)
                                {
                                    for (int j11 = -1; j11 < 4; ++j11)
                                    {
                                        int j12 = l5 + (j10 - 1) * j8;
                                        int i13 = i7 + j11;
                                        int j13 = j6 + (j10 - 1) * j9;
                                        blockpos$mutableblockpos.setPos(j12, i13, j13);

                                        if (j11 < 0 && !world.getBlockState(blockpos$mutableblockpos).getMaterial().isSolid() || j11 >= 0 && !world.isAirBlock(blockpos$mutableblockpos))
                                        {
                                            continue label231;
                                        }
                                    }
                                }

                                double d6 = (double)i7 + 0.5D - entityIn.posY;
                                double d8 = d3 * d3 + d6 * d6 + d4 * d4;

                                if (d0 < 0.0D || d8 < d0)
                                {
                                    d0 = d8;
                                    i1 = l5;
                                    j1 = i7;
                                    k1 = j6;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new BlockPos(i1, j1, k1);
    }

}

