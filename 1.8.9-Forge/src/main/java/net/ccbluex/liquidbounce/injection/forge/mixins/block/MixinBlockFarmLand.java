
package net.ccbluex.liquidbounce.injection.forge.mixins.block;


import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.exploit.VersionPatcher;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockFarmland.class)
public class MixinBlockFarmLand {
    /**
     * @author 1
     * @reason 1
     */
    @Overwrite
    public AxisAlignedBB getCollisionBoundingBox(World p_getCollisionBoundingBox_1_, BlockPos p_getCollisionBoundingBox_2_, IBlockState p_getCollisionBoundingBox_3_) {
        VersionPatcher versionPatcher = (VersionPatcher) LiquidBounce.moduleManager.getModule(VersionPatcher.class);
        if(versionPatcher.getState() && VersionPatcher.FarmLandPatch.get()) {
            return new AxisAlignedBB((double) p_getCollisionBoundingBox_2_.getX(), (double) p_getCollisionBoundingBox_2_.getY(), (double) p_getCollisionBoundingBox_2_.getZ(), (double) (p_getCollisionBoundingBox_2_.getX() + 1), (double) (p_getCollisionBoundingBox_2_.getY() + 0.9375), (double) (p_getCollisionBoundingBox_2_.getZ() + 1));
        }else {
            return new AxisAlignedBB((double) p_getCollisionBoundingBox_2_.getX(), (double) p_getCollisionBoundingBox_2_.getY(), (double) p_getCollisionBoundingBox_2_.getZ(), (double) (p_getCollisionBoundingBox_2_.getX() + 1), (double) (p_getCollisionBoundingBox_2_.getY() + 1), (double) (p_getCollisionBoundingBox_2_.getZ() + 1));
        }
    }
}
