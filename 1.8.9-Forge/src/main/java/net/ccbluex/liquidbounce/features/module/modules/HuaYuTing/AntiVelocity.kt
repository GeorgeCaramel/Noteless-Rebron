/*
 * Shovel Client
 */
package net.ccbluex.liquidbounce.features.module.modules.HuaYuTing

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import java.util.*


@ModuleInfo(name = "AntiVelocity", description = "", category = ModuleCategory.HuaYuTing)
class AntiVelocity : Module() {

    /**
     * OPTIONS
     */
    val modeValue = ListValue("Mode", arrayOf("Default","Grim1.9+"), "Default")
    private val horizontalValue = FloatValue("Horizontal", 0F, 0F,1F).displayable { modeValue.get() == "Default" }
    private val verticalValue = FloatValue("Vertical", 0F, 0F,1F).displayable { modeValue.get() == "Default" }
    private val onlySprint = BoolValue("OnlySprint",false).displayable { modeValue.get() == "Grim1.9+" }
    private val combatTime = IntegerValue("CombatTime", 5, 4, 25).displayable { modeValue.get() == "Grim1.9+" }

    /**
     * VALUES
     */
    var attacked = false
    private var velocityInput = false
    val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb)
            return
        when (modeValue.get().toLowerCase()) {
            "grim1.9+" ->{
                if (onlySprint.get() && mc.thePlayer.serverSprintState && MovementUtils.isMoving() || !onlySprint.get()) {
                    if (velocityInput) {
                        if (attacked) {
                            mc.thePlayer.motionX *= 0.07776
                            mc.thePlayer.motionZ *= 0.07776
                            attacked = false
                        }
                    }
                    if (mc.thePlayer.hurtTime == 0) {
                        velocityInput = false
                    }
                }
            }
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer)
                return

            when (modeValue.get().toLowerCase(Locale.getDefault())) {
                "default" -> {
                    val horizontal = horizontalValue
                    val vertical = verticalValue
                    if (horizontal.get() == 0F && vertical.get() == 0F)
                        event.cancelEvent()
                    packet.motionX = (packet.getMotionX() * horizontal.get()).toInt()
                    packet.motionY = (packet.getMotionY() * vertical.get()).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal.get()).toInt()
                }
                "grim1.9+"-> {
                    velocityInput = true
                    val currentRotation = RotationUtils.serverRotation
                    val target = RaycastUtils.raycastEntity(
                        killAura.rangeValue.get().toDouble(),
                        currentRotation.yaw,
                        currentRotation.pitch
                    ) { true }
                    if ((target != null && target != mc.thePlayer) || killAura.target != null) {
                        for (i in 0 until combatTime.get()) {
                            if (mc.thePlayer.serverSprintState && MovementUtils.isMoving()) {
                                mc.netHandler.addToSendQueue(C0APacketAnimation())
                                if (target != null && target != mc.thePlayer)
                                    mc.netHandler.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                                else
                                    mc.netHandler.addToSendQueue(C02PacketUseEntity(killAura.target, C02PacketUseEntity.Action.ATTACK))
                            } else if (!onlySprint.get()) {
                                mc.netHandler.addToSendQueue(C0APacketAnimation())
                                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                                mc.thePlayer.isSprinting = false
                                if (target != null && target != mc.thePlayer)
                                    mc.netHandler.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                                else
                                    mc.netHandler.addToSendQueue(C02PacketUseEntity(killAura.target, C02PacketUseEntity.Action.ATTACK))
                            }
                        }
                        attacked = true
                        mc.thePlayer.isSprinting = true
                        mc.thePlayer.serverSprintState = true
                    }
                }
            }
        }
        if (packet is S27PacketExplosion) {
            if (modeValue.get().equals("Default",false)) {
                event.cancelEvent()
            }
        }
    }
}
