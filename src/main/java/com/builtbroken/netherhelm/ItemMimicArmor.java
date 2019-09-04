package com.builtbroken.netherhelm;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemMimicArmor extends ItemArmor {
    
    public final ItemArmor parent;

    public ItemMimicArmor(ItemArmor parent) {
        super(parent.getArmorMaterial(), parent.renderIndex, parent.getEquipmentSlot());
        if(parent instanceof ItemMimicArmor) {
            throw new RuntimeException("Seriously?");
        }
        this.setTranslationKey(parent.getTranslationKey());
        this.setRegistryName(NetherHelmMod.MODID, "nether_helm_" + parent.getRegistryName().getPath());
        this.parent = parent;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return parent.getArmorTexture(stack, entity, slot, type);
    }

    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
            ModelBiped _default) {
        return parent.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }
    
    

}
