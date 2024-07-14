/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.exploit.VersionPatcher;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockLadder.class)
@SideOnly(Side.CLIENT)
public abstract class MixinBlockLadder extends MixinBlock {

    @Shadow
    @Final
    public static PropertyDirection FACING;

    /**
     * @author CCBlueX
     * @reason Fuck u
     */

    @ModifyConstant(method = "setBlockBoundsBasedOnState", constant = @Constant(floatValue = 0.125F))
    private float ViaVersion_LadderBB(float constant) {
        VersionPatcher versionPatcher = (VersionPatcher) LiquidBounce.moduleManager.getModule(VersionPatcher.class);
        if (versionPatcher.getState() && VersionPatcher.LadderPatch.get())
            return 0.1875F;
        return 0.125F;
    }
}
