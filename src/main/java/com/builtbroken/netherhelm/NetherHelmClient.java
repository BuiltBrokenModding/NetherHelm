package com.builtbroken.netherhelm;

import org.apache.logging.log4j.LogManager;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = NetherHelmMod.MODID, value = Side.CLIENT)
public class NetherHelmClient {

    public static final ModelResourceLocation mrl = new ModelResourceLocation(NetherHelmMod.MODID + ":mimic_nether_helm");

    @SubscribeEvent
    public static void modelReg(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(NetherHelmMod.nether_helm_component, 0, new ModelResourceLocation(NetherHelmMod.nether_helm_component.getRegistryName(), "inventory"));
        for(ItemMimicArmor item : NetherHelmMod.mimic_armors.values()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for(ItemMimicArmor item : NetherHelmMod.mimic_armors.values()) {
            IBakedModel parent = event.getModelRegistry().getObject(new ModelResourceLocation(item.parent.getRegistryName(), "inventory"));
            if(parent != null) {
                event.getModelRegistry().putObject(mrl, new MimicModel(parent));
            } else {
                LogManager.getLogger().warn("Could not get valid model definition for armor " + item.parent.getRegistryName());
            }
        }

    }

}