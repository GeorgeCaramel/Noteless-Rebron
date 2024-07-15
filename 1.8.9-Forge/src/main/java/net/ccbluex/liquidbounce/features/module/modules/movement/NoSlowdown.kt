/*
 * 我觉得 写这个的水平比你高
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.*
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.util.*

@ModuleInfo(name = "NoSlowdown", description = "NoSlow" ,category = ModuleCategory.MOVEMENT)
class NoSlowdown : Module() {

    private val swordMode = ListValue("SwordMode", arrayOf("None", "NCP", "AAC5", "SwitchItem","InvalidC09"), "None")

    private val blockMultiplier = FloatValue("BlockSpeed", 1f, 0.2F,1f)

    private val consumePacket = ListValue("ConsumeMode", arrayOf("None", "AAC5", "SwitchItem","DropItem","Pit"), "None")

    private val consumeMultiplier = FloatValue("ConsumeSpeed", 1f, 0.2F,1f)

    private val bowPacket = ListValue("BowMode", arrayOf("None", "AAC5", "SwitchItem"), "None")

    private val bowMultiplier = FloatValue("BowSpeed", 1f, 0.2F,1f)

    private val tagValue = ListValue("Tags", arrayOf("None","Mode","Raven","Custom"),"None")

    private var shouldDrop = false
    private var drop = false
    val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura

    override fun onEnable() {
        shouldDrop = false
    }

    override fun onDisable() {
        MSTimer().reset()
        LinkedList<net.minecraft.network.Packet<INetHandlerPlayClient>>().clear()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem ?: return
        val isUsingItem = usingItemFunc()

        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0)
            return

        if ((heldItem.item is ItemFood || heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk) && isUsingItem) {
            when (consumePacket.get().toLowerCase()) {

                "aac5" ->
                    PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f))

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C17PacketCustomPayload("Nothing", PacketBuffer(Unpooled.buffer())))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }

                else -> return
            }
        }

        if (heldItem.item is ItemBow && isUsingItem) {
            when (bowPacket.get().toLowerCase()) {
                "aac5" ->
                    PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f))

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C17PacketCustomPayload("Nothing", PacketBuffer(Unpooled.buffer())))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
            }
        }

        if (heldItem.item is ItemSword && isUsingItem) {
            when (swordMode.get().toLowerCase()) {
                "none" -> return

                "ncp" ->
                    when (event.eventState) {
                        EventState.PRE -> PacketUtils.sendPacketNoEvent(
                            C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
                        )

                        EventState.POST -> PacketUtils.sendPacketNoEvent(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f
                            )
                        )

                        else -> return
                    }

                "aac5" ->
                    if (event.eventState == EventState.POST) {
                        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f))
                    }

                "invalidc09" ->
                    when (event.eventState) {
                        EventState.PRE -> {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                            mc.netHandler.addToSendQueue(C17PacketCustomPayload("Nothing", PacketBuffer(Unpooled.buffer())))
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        }
                        EventState.POST -> {
                            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                        }
                    }

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C17PacketCustomPayload("Nothing", PacketBuffer(Unpooled.buffer())))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        val stack = mc.thePlayer.heldItem ?: return
        if (consumePacket.get() == "DropItem") {
            if (packet is C08PacketPlayerBlockPlacement && (((stack.item is ItemAppleGold && !(stack.item as ItemAppleGold).hasEffect(stack)) || stack.item is ItemFood && stack.item !is ItemAppleGold && mc.thePlayer.foodStats.foodLevel < 20.0F) && stack.stackSize >= 2)) {
                if (!shouldDrop) {
                    shouldDrop = true
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN,EnumFacing.DOWN))
                    drop = true
                }
            }
            if (packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM && drop && ((stack.item is ItemAppleGold && !(stack.item as ItemAppleGold).hasEffect(stack)) || stack.item is ItemFood && stack.item !is ItemAppleGold) && stack.stackSize >= 1) {
                shouldDrop = true
                event.cancelEvent()
                drop = false
            }
            if (packet is S2FPacketSetSlot) {
                val s2F: S2FPacketSetSlot = packet
                if (s2F.func_149174_e().item == mc.thePlayer.heldItem.item) {
                    mc.thePlayer.inventory.getCurrentItem().stackSize = s2F.func_149174_e().stackSize
                    event.cancelEvent()
                    shouldDrop = false
                }
            }
        }
        if (consumePacket.get() == "Pit") {
            if (packet is C08PacketPlayerBlockPlacement && ((stack.item is ItemAppleGold && !(stack.item as ItemAppleGold).hasEffect(stack)) || stack.item is ItemFood && stack.item !is ItemAppleGold && mc.thePlayer.foodStats.foodLevel < 20.0F)) {
                if (!shouldDrop) {
                    shouldDrop = true
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN,EnumFacing.DOWN))
                }
            }
            if (packet is S2FPacketSetSlot) {
                val s2F: S2FPacketSetSlot = packet
                if (s2F.func_149174_e().item == mc.thePlayer.heldItem.item) {
                    mc.thePlayer.inventory.getCurrentItem().stackSize = s2F.func_149174_e().stackSize
                    event.cancelEvent()
                    shouldDrop = false
                }
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        event.forward = getMultiplier(heldItem)
        event.strafe = getMultiplier(heldItem)
    }

    private fun getMultiplier(item: Item?) = when (item) {
        is ItemFood -> {
            if (consumePacket.get() != "DropItem" && consumePacket.get() != "Pit") {
                consumeMultiplier.get()
            } else if (consumePacket.get() == "DropItem") {
                if (((mc.thePlayer.heldItem.item is ItemAppleGold && !(mc.thePlayer.heldItem.item as ItemAppleGold).hasEffect(
                        mc.thePlayer.heldItem
                    )) || mc.thePlayer.heldItem.item is ItemFood && mc.thePlayer.heldItem.item !is ItemAppleGold) && mc.thePlayer.heldItem.stackSize >= 1 && drop) {
                    if (!shouldDrop) {
                        consumeMultiplier.get()
                    } else {
                        0.2F
                    }
                } else 0.2F
            } else {
                if (((mc.thePlayer.heldItem.item is ItemAppleGold && !(mc.thePlayer.heldItem.item as ItemAppleGold).hasEffect(
                        mc.thePlayer.heldItem
                    )) || mc.thePlayer.heldItem.item is ItemFood && mc.thePlayer.heldItem.item !is ItemAppleGold)
                ) {
                    if (!shouldDrop) {
                        consumeMultiplier.get()
                    } else {
                        0.2F
                    }
                } else 0.2F
            }
        }

        is ItemPotion, is ItemBucketMilk -> {
            if (consumePacket.get() != "DropItem" && consumePacket.get() != "Pit") {
                consumeMultiplier.get()
            } else 0.2F
        }

        is ItemSword -> blockMultiplier.get()

        is ItemBow -> bowMultiplier.get()

        else -> 0.2F
    }
    fun usingItemFunc() = mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && killAura.blockingStatus))

}