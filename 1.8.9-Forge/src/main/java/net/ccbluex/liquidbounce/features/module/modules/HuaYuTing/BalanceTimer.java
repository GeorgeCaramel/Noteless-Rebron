// Decompiled with: CFR 0.152
// Class Version: 8
package net.ccbluex.liquidbounce.features.module.modules.HuaYuTing;

import java.util.concurrent.LinkedBlockingQueue;

import kotlin.jvm.internal.Intrinsics;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.event.WorldEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

@ModuleInfo(name="BalanceTimer", description="Changes the speed of the entire game.", category=ModuleCategory.HuaYuTing)
public class BalanceTimer extends Module {

    private final FloatValue speedValue = new FloatValue("Speed", 2.0f, 0.1f, 10.0f);

    private final BoolValue onMoveValue = new BoolValue("OnMove", true);

    private final BoolValue autoDisableValue = new BoolValue("AutoDisable", true);
    private boolean cancel;
    private int b;

    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue();

    public void onDisable() {
        this.b = 0;
        mc.timer.timerSpeed = 1.0f;
        this.cancel = false;
        while (!this.packets.isEmpty()) {
            try {
                mc.getNetHandler().getNetworkManager().sendPacket(this.packets.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventTarget
    public final void onPacket(PacketEvent e) {
        Packet p = e.getPacket();
        if (p instanceof C0FPacketConfirmTransaction && this.cancel) {
            this.packets.add(p);
            e.cancelEvent();
        }
        if (p instanceof C03PacketPlayer) {
            if (!((C03PacketPlayer)((Object)p)).isMoving() && !((C03PacketPlayer)((Object)p)).getRotating()) {
                e.cancelEvent();
                if (this.cancel) {
                    this.b += 50;
                }
            } else {
                this.b -= 50;
            }
            if (e.isCancelled()) {
                this.cancel = true;
            }
        }
    }

    @EventTarget
    public final void onUpdate(UpdateEvent event) {
        PacketUtils.sendPacketNoEvent(new C0FPacketConfirmTransaction(0, (short) 0,true));
        if (this.b < 0) {
            this.setState(false);
        }
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (mc.thePlayer.ticksExisted % 15 == 0) {
            ClientUtils.displayChatMessage(Intrinsics.stringPlus("Balance: ", this.b));
        }
        if ((MovementUtils.isMoving() || !((Boolean)this.onMoveValue.get()).booleanValue()) && this.b > 100) {
            mc.timer.timerSpeed = ((Number)this.speedValue.get()).floatValue();
            return;
        }
        mc.timer.timerSpeed = 1.0f;
    }

    @EventTarget
    public final void onWorld(WorldEvent event) {
        if (event.getWorldClient() != null) {
            return;
        }
        if (((Boolean)this.autoDisableValue.get()).booleanValue()) {
            this.setState(false);
        }
    }
}
