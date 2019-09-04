package com.builtbroken.netherhelm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MimicArmorTEISR extends TileEntityItemStackRenderer {

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public static TransformType transform = TransformType.NONE;

    @Override
    public void renderByItem(ItemStack itemStackIn, float partialTicks) {
        if(itemStackIn.getItem() instanceof ItemMimicArmor) {
            ItemMimicArmor item = (ItemMimicArmor) itemStackIn.getItem();
            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(item.parentStack, Minecraft.getMinecraft().player.world, Minecraft.getMinecraft().player);
            Minecraft.getMinecraft().getRenderItem().renderModel(model, -1, item.parentStack);
            if(Minecraft.getSystemTime() % 5000 < 300) {
                GlStateManager.depthMask(false);
                GlStateManager.depthFunc(514);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
                Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);
                GlStateManager.matrixMode(5890);
                GlStateManager.pushMatrix();
                GlStateManager.scale(8.0F, 8.0F, 8.0F);
                float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
                GlStateManager.translate(f, 0.0F, 0.0F);
                GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
                Minecraft.getMinecraft().getRenderItem().renderModel(model, -0x40c7cc, ItemStack.EMPTY);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(8.0F, 8.0F, 8.0F);
                float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
                GlStateManager.translate(-f1, 0.0F, 0.0F);
                GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
                Minecraft.getMinecraft().getRenderItem().renderModel(model, -0x40c7cc, ItemStack.EMPTY);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(515);
                GlStateManager.depthMask(true);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            }
        }
    }

}
