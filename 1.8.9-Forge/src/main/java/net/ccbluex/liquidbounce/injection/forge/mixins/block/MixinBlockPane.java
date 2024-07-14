
package net.ccbluex.liquidbounce.injection.forge.mixins.block;


import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.exploit.VersionPatcher;
import net.minecraft.block.BlockPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(BlockPane.class)
public abstract class MixinBlockPane extends MixinBlock {

    @Shadow
    @Final
    public abstract boolean canPaneConnectTo(IBlockAccess p_canPaneConnectTo_1_, BlockPos p_canPaneConnectTo_2_, EnumFacing p_canPaneConnectTo_3_);


    /**
     * @author 1
     * @reason 1
     */
    @Overwrite
    public void addCollisionBoxesToList(World p_addCollisionBoxesToList_1_, BlockPos p_addCollisionBoxesToList_2_, IBlockState p_addCollisionBoxesToList_3_, AxisAlignedBB p_addCollisionBoxesToList_4_, List<AxisAlignedBB> p_addCollisionBoxesToList_5_, Entity p_addCollisionBoxesToList_6_) {
        VersionPatcher versionPatcher = (VersionPatcher) LiquidBounce.moduleManager.getModule(VersionPatcher.class);
        if(!VersionPatcher.GlassPanePatch.get() || !versionPatcher.getState()) {
            boolean flag = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.NORTH);
            boolean flag1 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.SOUTH);
            boolean flag2 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.WEST);
            boolean flag3 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.EAST);
            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    this.setBlockBounds(0.0F, 0.0F, 0.4375F, 0.5F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                } else if (flag3) {
                    this.setBlockBounds(0.5F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                }
            } else {
                this.setBlockBounds(0.0F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
            }

            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 0.5F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                } else if (flag1) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.5F, 0.5625F, 1.0F, 1.0F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                }
            } else {
                this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 1.0F);
                super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
            }
        } else {
            boolean flag = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.NORTH);
            boolean flag1 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.SOUTH);
            boolean flag2 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.WEST);
            boolean flag3 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.EAST);
            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    this.setBlockBounds(0.0F, 0.0F, 0.4375F, 0.5625F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                } else if (flag3) {
                    this.setBlockBounds(0.5625F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                }
            } else {
                this.setBlockBounds(0.0F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
            }

            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                } else if (flag1) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.4375F, 0.5625F, 1.0F, 1.0F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                }
            } else {
                this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 1.0F);
                super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
            }
        }
    }
}
