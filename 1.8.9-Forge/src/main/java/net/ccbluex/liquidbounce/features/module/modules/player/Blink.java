/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.render.Breadcrumbs;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Blink", description = "Suspends all movement packets.", category = ModuleCategory.PLAYER)
public class Blink extends Module {

    private final LinkedBlockingQueue<Packet> packets = new LinkedBlockingQueue<>();
    private EntityOtherPlayerMP fakePlayer = null;
    private boolean disableLogger;
    private final LinkedList<double[]> positions = new LinkedList<>();

    private final BoolValue pulseValue = new BoolValue("Pulse", false);
    private final IntegerValue pulseDelayValue = new IntegerValue("PulseDelay", 1000, 500, 5000);

    private final MSTimer pulseTimer = new MSTimer();

    public boolean blinking;

    @Override
    public void onEnable() {
        if(mc.thePlayer == null)
            return;

        if (!pulseValue.get()) {
            fakePlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
            fakePlayer.clonePlayer(mc.thePlayer, true);
            fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
            fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
            mc.theWorld.addEntityToWorld(-1337, fakePlayer);
        }

        synchronized(positions) {
            positions.add(new double[] {mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (mc.thePlayer.getEyeHeight() / 2), mc.thePlayer.posZ});
            positions.add(new double[] {mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
        }

        pulseTimer.reset();
    }

    @Override
    public void onDisable() {
        if(mc.thePlayer == null)
            return;

        blink();
        blinking = false;
        if (fakePlayer != null) {
            mc.theWorld.removeEntityFromWorld(fakePlayer.getEntityId());
            fakePlayer = null;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (mc.thePlayer == null || disableLogger)
            return;

        if (packet instanceof C03PacketPlayer) // Cancel all movement stuff
            event.cancelEvent();

        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook ||
                packet instanceof C08PacketPlayerBlockPlacement ||
                packet instanceof C0APacketAnimation ||
                packet instanceof C0BPacketEntityAction || packet instanceof C02PacketUseEntity) {
            event.cancelEvent();

            packets.add(packet);
        }
        blinking = true;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        synchronized(positions) {
            positions.add(new double[] {mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
        }

        if(pulseValue.get() && pulseTimer.hasTimePassed(pulseDelayValue.get())) {
            blink();
            pulseTimer.reset();
        }
    }

    @Override
    public String getTag() {
        return String.valueOf(packets.size());
    }

    private void blink() {
        try {
            disableLogger = true;

            while (!packets.isEmpty()) {
                mc.getNetHandler().getNetworkManager().sendPacket(packets.take());
            }

            disableLogger = false;
        }catch(final Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }

        synchronized(positions) {
            positions.clear();
        }
    }
}
