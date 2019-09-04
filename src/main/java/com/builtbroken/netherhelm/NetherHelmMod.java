package com.builtbroken.netherhelm;

import org.apache.logging.log4j.LogManager;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = NetherHelmMod.MODID)
@Mod(modid = NetherHelmMod.MODID, name = NetherHelmMod.NAME, version = NetherHelmMod.VERSION)
public class NetherHelmMod {
    
    public static final String MODID = "netherhelm";
    public static final String NAME = "Nether Helm";
    public static final String VERSION = "@VERSION@";
    
    public static Item nether_helm_component = new Item().setRegistryName(MODID, "nether_helm").setCreativeTab(CreativeTabs.TOOLS).setTranslationKey("netherhelm.nether_helm");
    
    @Config(modid = NetherHelmMod.MODID)
    public static class Configuration {
        @Config.RequiresMcRestart
        @Config.Comment("List of armor item IDs that are valid for crafting. Clients must have matching configs due to item registration!")
        public static String[] affected_armors = new String[] {"minecraft:iron_helmet", "minecraft:leather_helmet", "minecraft:golden_helmet", "minecraft:diamond_helmet"};
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(nether_helm_component);
        for(String armor : Configuration.affected_armors) {
            Item item = event.getRegistry().getValue(new ResourceLocation(armor));
            if(item != null) {
                if(item instanceof ItemArmor) {
                    if(!(item instanceof ItemMimicArmor)) {
                        event.getRegistry().register(new ItemMimicArmor((ItemArmor) item));
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
        if(event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().getEntityWorld().isRemote) {
            ItemStack newStack = event.getTo();
            if(newStack.getItem() instanceof ItemMimicArmor && event.getSlot() != EntityEquipmentSlot.MAINHAND && event.getSlot() != EntityEquipmentSlot.OFFHAND) {
                int dim = -1;
                if(newStack.hasTagCompound() && newStack.getTagCompound().hasKey("netherhelm:target_dimension", Constants.NBT.TAG_INT)) {
                    dim = newStack.getTagCompound().getInteger("netherhelm:target_dimension");
                }
                EntityPlayer player = (EntityPlayer) event.getEntityLiving();
                if(player.inventory.armorInventory.remove(newStack)) {
                    player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, newStack));
                }
                player.changeDimension(dim, new Teleporter(DimensionManager.getWorld(dim)) {
                    @Override
                    public void placeInPortal(Entity entityIn, float rotationYaw) {}
                    @Override
                    public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {return false;}
                    @Override
                    public boolean makePortal(Entity entityIn) {return false;}
                    @Override
                    public void removeStalePortalLocations(long worldTime) {}
                    @Override
                    public void placeEntity(World world, Entity entity, float yaw) {}
                });
            }
        }
    }

}