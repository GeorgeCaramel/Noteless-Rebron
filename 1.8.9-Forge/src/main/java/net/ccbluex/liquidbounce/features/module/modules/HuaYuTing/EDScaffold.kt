/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.skidbyjuzibujiji

//import net.ccbluex.liquidbounce.utils.RotationUtils.*
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
//import net.ccbluex.liquidbounce.features.module.modules.trash.GrimScaffold.Companion.getVec3
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextDouble
import net.ccbluex.liquidbounce.utils.render.ColorManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.utils.timer.TimerUtil
import net.minecraft.block.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityPig
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.potion.Potion
import net.minecraft.stats.StatList
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.*


@ModuleInfo(name = "EDScaffold", description = "蒋介石私人scaffold",category = ModuleCategory.HuaYuTing)
class EDScaffold : Module() {
    private val towerEnabled = BoolValue("EnableTower", false)
    private val towerModeValue = ListValue(
        "TowerMode", arrayOf(
            "Jump",
            "Motion",
            "StableMotion",
            "ConstantMotion",
            "MotionTP",
            "Packet",
            "Teleport",
            "AAC3.3.9",
            "AAC3.6.4",
            "Verus",
            "Hypixel"
        ), "Motion"
    ) .displayable{ towerEnabled.get() }
    private val noMoveOnlyValue = BoolValue("NoMove", true) .displayable{ towerEnabled.get() }
    private val towerTimerValue = FloatValue("TowerTimer", 1f, 0.1f, 10f) .displayable{ towerEnabled.get() }

    // Jump mode
    private val jumpMotionValue = FloatValue("JumpMotion", 0.42f, 0.3681289f, 0.79f) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("Jump", ignoreCase = true)
    }
    private val jumpDelayValue = IntegerValue("JumpDelay", 0, 0, 20) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("Jump", ignoreCase = true)
    }

    // StableMotion
    private val stableMotionValue = FloatValue("StableMotion", 0.41982f, 0.1f, 1f) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true)
    }
    private val stableFakeJumpValue = BoolValue("StableFakeJump", false) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true)
    }
    private val stableStopValue = BoolValue("StableStop", false) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true)
    }
    private val stableStopDelayValue = IntegerValue("StableStopDelay", 1500, 0, 5000) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true) && stableStopValue.get()
    }

    // ConstantMotion
    private val constantMotionValue = FloatValue("ConstantMotion", 0.42f, 0.1f, 1f) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("ConstantMotion", ignoreCase = true)
    }
    private val constantMotionJumpGroundValue = FloatValue("ConstantMotionJumpGround", 0.79f, 0.76f, 1f) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("ConstantMotion", ignoreCase = true)
    }

    // Teleport
    private val teleportHeightValue = FloatValue("TeleportHeight", 1.15f, 0.1f, 5f) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }
    private val teleportDelayValue = IntegerValue("TeleportDelay", 0, 0, 20) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }
    private val teleportGroundValue = BoolValue("TeleportGround", true) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }
    private val teleportNoMotionValue = BoolValue("TeleportNoMotion", false) .displayable{
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }

    /**
     * OPTIONS (Scaffold)
     */
    // Mode
    val modeValue = ListValue("Mode", arrayOf("Normal", "Expand", "OffGround"), "Normal")
    private val strafefix = BoolValue("StrafeFix",false)
    private val  TestStrafeFix = BoolValue("TestStrafeFix",false)

    // Delay
    private val placeableDelay = BoolValue("PlaceableDelay", false)
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) set(i)
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) set(i)
        }
    }

    // idfk what is this
    private val smartDelay = BoolValue("SmartDelay", true)

    private val search = BoolValue("LBSearch",false)

    // AutoBlock
    private val autoBlockMode = ListValue("AutoBlock", arrayOf("Spoof", "Switch","LiteSpoof","Off"), "Spoof")
    private val stayAutoBlock = BoolValue("StayAutoBlock", false) .displayable{
        !autoBlockMode.get().equals("off", ignoreCase = true)
    }
    private val sortByHighestAmount = BoolValue("SortByHighestAmount", false) .displayable{
        !autoBlockMode.get().equals("off", ignoreCase = true)
    }

    //make sprint compatible with tower.add sprint tricks
    val sprintModeValue =
        ListValue("SprintMode", arrayOf("Same", "Ground", "Air","NoPacket", "PlaceOff", "PlaceOn", "FallDownOff","GrimOff", "Off","Smart"), "Off")

    // Basic stuff
    private val swingValue = BoolValue("Swing", true)
    private val downValue = BoolValue("Down", false)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post", "Legit","Tick","Packet"), "Post")

    // Eagle
    private val eagleValue = BoolValue("Eagle", false)
    private val eagleSilentValue = BoolValue("EagleSilent", false) .displayable{ eagleValue.get() }
    private val eagledelay = BoolValue("EagleDelay",false).displayable{ eagleValue.get() }
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10) .displayable{ eagleValue.get() }
    private val eagleEdgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2f, 0f, 0.5f) .displayable{ eagleValue.get() }

    // Expand
    private val omniDirectionalExpand = BoolValue("OmniDirectionalExpand", true) .displayable{
        modeValue.get().equals("expand", ignoreCase = true)
    }
    private val expandLengthValue = IntegerValue("ExpandLength", 5, 1, 6) .displayable{
        modeValue.get().equals("expand", ignoreCase = true)
    }

    // Rotations
    private val rotationsValue = BoolValue("Rotations", true)
    private val noHitCheckValue = BoolValue("NoHitCheck", false) .displayable{ rotationsValue.get() }
    private val keepRotation = BoolValue("KeepRotation", false) .displayable{ rotationsValue.get() }

    private val fastrotate = BoolValue("FastRotate",false).displayable{ !keepRotation.get() }
    private val blockstorotate = IntegerValue("BlocksToRotate", 0, 0, 10) .displayable{ !keepRotation.get() }
    private val rotateEdgeDistanceValue = FloatValue("RotateEdgeDistance", 0.2f, 0f, 0.5f) .displayable{ !keepRotation.get() }
    private val whenspeedrotate = FloatValue("SpeedRotateEdgeDistance", 0f, 0f, 0.1f) .displayable{ !keepRotation.get() }
    private val CancelRotateMode = ListValue("CancelRotateModes", arrayOf("All","Ground","Air"),"All").displayable{ !keepRotation.get() }
    private val rotateTiming = ListValue("RotateTiming", arrayOf("DelayPost","Post","All"),"All").displayable{ !keepRotation.get() }
    private val PostKeepRotateTick = IntegerValue("KeepRotateTick",1,1,5).displayable{ !keepRotation.get()  && rotateTiming.equals("DelayPost")}
    private val PostStopRotateTick = IntegerValue("StopRotateTick",0,1,5).displayable{ !keepRotation.get()  && rotateTiming.equals("DelayPost")}
    private val stabilizedRotation = BoolValue("StabilizedRotation", false) .displayable{ rotationsValue.get() && (rotationModeValue.equals("Normal") ||rotationModeValue.equals("GrimTest") ) }
    private val grimLock = BoolValue("GrimLock", true).displayable{ rotationsValue.get() && rotationModeValue.equals("GrimTest") }
    private val rotationModeValue = ListValue(
        "RotationMode",
        arrayOf("Normal",
            "Spin",
            "Custom",
            "Novoline",
            "Intave",
            "GrimTest",
            "Rise",
            "Risesec",
            "NormalPredict",
            "Crazy",
            "Telly",
            "IDK",
            "IDK2",
            "Augustus",
            "Test",
            "UnfairPitch"),
        "Normal") // searching reason
    private val maxTurnSpeed: FloatValue =
        object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f) {
            override fun onChanged(oldValue: Float, newValue: Float) {
                val i = minTurnSpeed.get()
                if (i > newValue) set(i)
            }
        }
    private val minTurnSpeed: FloatValue =
        object : FloatValue("MinTurnSpeed", 180f, 0f, 180f) {
            override fun onChanged(oldValue: Float, newValue: Float) {
                val i = maxTurnSpeed.get()
                if (i < newValue) set(i)
            }
        }
    private val staticPitch = BoolValue("StaticPitch",true).displayable { rotationsValue.get() && rotationModeValue.get() == "Test" }
    val yawSpeed = FloatValue("YawSpeed", 40.0f, 0.0f, 180.0f).displayable{ rotationsValue.get() && rotationModeValue.get() == "Augustus" }
    val yawOffset = FloatValue("YawOffSet", -180f, -200f, 200f).displayable{ rotationsValue.get() && rotationModeValue.get() == "Augustus" }
    private val customYawValue = FloatValue("Custom-Yaw", 180f, -180f, 180f) .displayable{
        rotationModeValue.get().equals("custom", ignoreCase = true)
    }
    private val customPitchValue = FloatValue("Custom-Pitch", 82f, -90f, 90f) .displayable{
        rotationModeValue.get().equals("custom", ignoreCase = true)
                ||
                rotationModeValue.get().equals("better", ignoreCase = true)
    }
    private val speenSpeedValue = FloatValue("Spin-Speed", 5f, -90f, 90f) .displayable{
        rotationModeValue.get().equals("spin", ignoreCase = true)
    }
    private val speenPitchValue = FloatValue("Spin-Pitch", 90f, -90f, 90f) .displayable{
        rotationModeValue.get().equals("spin", ignoreCase = true)
    }

    private val placeConditionValue =
        ListValue("Place-Condition", arrayOf("Air", "FallDown", "NegativeMotion", "Always"), "Always")
    private val rotationStrafeValue = BoolValue("RotationStrafe", false)
    private val speedPotSlow = BoolValue("SpeedPotDetect", true)

    // Zitter
    private val zitterValue = BoolValue("Zitter", false) .displayable{ !isTowerOnly }
    private val zitterModeValue =
        ListValue("ZitterMode", arrayOf("Teleport", "Smooth"), "Teleport") .displayable{ !isTowerOnly && zitterValue.get() }
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f) .displayable{
        !isTowerOnly && zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)
    }
    private val zitterStrength = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f) .displayable{
        !isTowerOnly && zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)
    }
    private val zitterDelay = IntegerValue("ZitterDelay", 100, 0, 500) .displayable{
        !isTowerOnly && zitterValue.get() && zitterModeValue.get().equals("smooth", ignoreCase = true)
    }

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f) .displayable{ !isTowerOnly }
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)
    private val xzMultiplier = FloatValue("XZ-Multiplier", 1f, 0f, 4f)
    private val customSpeedValue = BoolValue("CustomSpeed", false)
    private val customMoveSpeedValue = FloatValue("CustomMoveSpeed", 0.3f, 0f, 5f) .displayable{ customSpeedValue.get() }

    // Safety

    private val sameYValue = BoolValue("SameY", false) .displayable{ !towerEnabled.get() }
    private val sameYOnlyMove = BoolValue("SameY-OnlyMove",false).displayable{sameYValue.get()}
    private val telly = BoolValue("Telly",false)
    private val autoJumpValue = BoolValue("AutoJump", false)
    private val blockstoJump = IntegerValue("BlocksToJump", 0, 0, 10) .displayable{ autoJumpValue.get() }
    private val jumpEdgeDistanceValue = FloatValue("JumpEdgeDistance", 0.2f, 0f, 0.5f) .displayable{ autoJumpValue.get() }

    private val smartSpeedValue = BoolValue("SmartSpeed", false)
    private val safeWalkValue = BoolValue("SafeWalk", true)
    private val airSafeValue = BoolValue("AirSafe", false) .displayable{ safeWalkValue.get() }
    private val safeWalkMoveValue = BoolValue("SafeWalkOnlySlow",false).displayable{ safeWalkValue.get() }
    private val safeWalkMoveSpeedValue =  FloatValue("SafeWalkOnlySlowValue",0.01f,0.01f,0.2f).displayable{ safeWalkValue.get() && safeWalkMoveValue.get() }
    private val autoDisableSpeedValue = BoolValue("AutoDisable-Speed", true)
    private val hitableCheckValue = ListValue("HitableCheck", arrayOf("Simple", "Strict","OFF"), "Simple")
    private val hitableCheckOnGround = BoolValue("HitableCheckOnGround",false)
    private val hitableCheckOnGroundValue = ListValue("HitableCheckOnGroundMode", arrayOf("Simple", "Strict","OFF"), "Simple")
    private val invalidPlaceFacingMode = ListValue("WhenPlaceFacingInvalid", arrayOf("CancelIt", "FixIt", "IgnoreIt"), "FixIt")

    // bypass

    private val extraClickValue = ListValue("ExtraClick", arrayOf("EmptyC08", "AfterPlace", "RayTrace", "OFF"), "OFF")
    private val extraClickMaxDelayValue: IntegerValue = object : IntegerValue("ExtraClickMaxDelay", 100, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMinDelayValue.get()
            if (i > newValue) set(i)
        }
    }.displayable { !extraClickValue.equals("OFF") } as IntegerValue
    private val extraClickMinDelayValue: IntegerValue = object : IntegerValue("ExtraClickMinDelay", 50, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMaxDelayValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !extraClickValue.equals("OFF") } as IntegerValue
    // Visuals
    private val modeDisplay = BoolValue("ModeDisplay", true)
    private val markValue = BoolValue("Mark", false)
    private val redValue = IntegerValue("Red", 0, 0, 255) .displayable{ markValue.get() }
    private val greenValue = IntegerValue("Green", 120, 0, 255) .displayable{ markValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255) .displayable{ markValue.get() }
    private val alphaValue = IntegerValue("Alpha", 120, 0, 255) .displayable{ markValue.get() }
    private val circleValue =BoolValue("Circle",false)
    private val circlerenderModeValue = ListValue("RenderMode", arrayOf("Circle", "Polygon", "None"), "Polygon").displayable{circleValue.get()}
    private val lineWidthValue = FloatValue("LineWidth", 1f, 1f, 10f). displayable{!circlerenderModeValue.equals("None") && circleValue.get()}
    private val radiusValue = FloatValue("Radius", 0.5f, 0.1f, 5.0f).displayable{circleValue.get()}
    private val sadasd = IntegerValue("DevDelay",0,0,15)

    /**
     * MODULE
     */
    private var lockRotationTimer = TickTimer()
    // Target block
    private var targetPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0
    private var faceBlock = false

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var lockRotation2: Rotation? = null
    private val currRotation
        get() = RotationUtils.targetRotation ?: mc.thePlayer.rotation

    val Entity.rotation: Rotation
        get() = Rotation(rotationYaw, rotationPitch)

    // Auto block slot
    var slot = 0
    private var lastSlot = 0

    // Zitter Smooth
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val towerDelayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay: Long = 0
    private val clickTimer = MSTimer()
    private var clickDelay: Long = 0

    private var lastPlaceBlock: BlockPos? = null
    private var afterPlaceC08: C08PacketPlayerBlockPlacement? = null
    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false
    var candelayeagle = false
    var eagledelaytick = 0

    //Rotate
    private var placedBlocksWithoutRotate = 0
    //jump
    private var placedBlocksWithoutJump = 0
    // Down
    private var shouldGoDown = false
    private var alpha = 0f
    private var towerStatus = false

    private var tellyPlaceTicks = 0

    // Render thingy
    private var progress = 0f
    private var spinYaw = 0f
    private var lastMS = 0L

    // Mode stuff
    private val timer = TickTimer()
    private var jumpGround = 0.0
    private var verusState = 0
    private var verusJumped = false
    private var offGroundTicks = 0
    private var onGroundTicks = 0

    private var rots = Rotation(0f, 0f)
    private var objectPosition: MovingObjectPosition? = null
    private var blockPos: BlockPos? = null
    private var start = true
    private var xyz = DoubleArray(3)
    private val hashMap = HashMap<Float, MovingObjectPosition>()
    private val startTimeHelper = MSTimer()

    private val isTowerOnly: Boolean
        get() = towerEnabled.get()

    private val targetBlock: Vec3? = null
    private val blockFace: BlockPos? = null
    private var targetYaw = 0f
    var targetPitch = 0f
    var oldTargetPitch = 0f
    var oldTargetYaw = 0f

    private var changerotate = false
    private var CanCancelRotate = false

    private var rotatetick =0;
    private var stoprotatetick  = 0
    private var canrotate = true

    private fun towerActivation(): Boolean {
        return towerEnabled.get() && mc.gameSettings.keyBindJump.isKeyDown
    }
    private fun tryfastrotate(){
        if(fastrotate.get()){
            lockRotation = Rotation(mc.thePlayer.rotationYaw-180f,80f)
        }
    }
    private var cantellyrotate = false
    private var tellydelay = 0
    private var isPre1 = false
    private var doSpoof = false

    /**
     * Enable module
     */
    override fun onEnable() {
        tellyPlaceTicks = 0
        if (mc.thePlayer == null) return
        doSpoof = false
        progress = 0f
        spinYaw = 0f
        clickTimer.reset()
        launchY = mc.thePlayer.posY.toInt()
        lastSlot = mc.thePlayer.inventory.currentItem
        clickDelay = TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
        slot = mc.thePlayer.inventory.currentItem
        canrotate = true
        candelayeagle = false
        tryfastrotate()
        if (autoDisableSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state
        ) {
            LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state = false
            LiquidBounce.hud.addNotification(
                Notification(
                    "Speed",
                    "Speed is disabled to prevent flags/errors.",
                    NotifyType.WARNING,
                    1500,
                    500
                )
            )
        }
        if(mc.thePlayer.isPotionActive(Potion.moveSpeed)){
            LiquidBounce.hud.addNotification(
                Notification(
                    "Scaffold",
                    "Get Speed potion effect.",
                    NotifyType.WARNING,
                    1000,
                    300
                )
            )
        }
        rots.pitch = (mc.thePlayer.rotationPitch)
        rots.yaw = (mc.thePlayer.rotationYaw)
        objectPosition = null
        blockPos = null
        start = true
        this.startTimeHelper.reset()
        faceBlock = false
        lastMS = System.currentTimeMillis()
        changerotate = false
    }

    //Send jump packets, bypasses Hypixel.
    private fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    /**
     * Move player
     */
    private fun move(event: MotionEvent) {
        mc.thePlayer.cameraYaw = 0f
        mc.thePlayer.cameraPitch = 0f
        if (noMoveOnlyValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
            mc.thePlayer.jumpMovementFactor = 0f
        }
        when (towerModeValue.get().toLowerCase(Locale.getDefault())) {
            "jump" -> if (mc.thePlayer.onGround && timer.hasTimePassed(jumpDelayValue.get())) {
                fakeJump()
                mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
                timer.reset()
            }

            "motion" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.42
            } else if (mc.thePlayer.motionY < 0.1) mc.thePlayer.motionY = -0.3

            "motiontp" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.42
            } else if (mc.thePlayer.motionY < 0.23) mc.thePlayer.setPosition(
                mc.thePlayer.posX, mc.thePlayer.posY.toInt()
                    .toDouble(), mc.thePlayer.posZ
            )

            "packet" -> if (mc.thePlayer.onGround && timer.hasTimePassed(2)) {
                fakeJump()
                mc.netHandler.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.76, mc.thePlayer.posZ, false
                    )
                )
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.08, mc.thePlayer.posZ)
                timer.reset()
            }

            "teleport" -> {
                if (teleportNoMotionValue.get()) mc.thePlayer.motionY = 0.0
                if ((mc.thePlayer.onGround || !teleportGroundValue.get()) && timer.hasTimePassed(teleportDelayValue.get())) {
                    fakeJump()
                    mc.thePlayer.setPositionAndUpdate(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + teleportHeightValue.get(),
                        mc.thePlayer.posZ
                    )
                    timer.reset()
                }
            }

            "stablemotion" -> {
                if (stableFakeJumpValue.get()) fakeJump()
                mc.thePlayer.motionY = stableMotionValue.get().toDouble()
                if (stableStopValue.get() && towerDelayTimer.hasTimePassed(stableStopDelayValue.get().toLong())) {
                    mc.thePlayer.motionY = -0.28
                    towerDelayTimer.reset()
                }
            }

            "constantmotion" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    jumpGround = mc.thePlayer.posY
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                }
                if (mc.thePlayer.posY > jumpGround + constantMotionJumpGroundValue.get()) {
                    fakeJump()
                    mc.thePlayer.setPosition(
                        mc.thePlayer.posX, mc.thePlayer.posY.toInt()
                            .toDouble(), mc.thePlayer.posZ
                    )
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                    jumpGround = mc.thePlayer.posY
                }
            }

            "aac3.3.9" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (mc.thePlayer.motionY < 0) {
                    mc.thePlayer.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
                if (mc.thePlayer.ticksExisted % 4 == 1) {
                    mc.thePlayer.motionY = 0.4195464
                    mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                    mc.thePlayer.motionY = -0.5
                    mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                }
            }

            "aac3.6.4" -> if (mc.thePlayer.ticksExisted % 4 == 1) {
                mc.thePlayer.motionY = 0.4195464
                mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
            } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                mc.thePlayer.motionY = -0.5
                mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
            }

            "verus" -> {
                mc.thePlayer.setPosition(mc.thePlayer.posX, (mc.thePlayer.posY * 2).roundToInt().toDouble() / 2, mc.thePlayer.posZ)
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    mc.thePlayer.motionY = 0.5
                    mc.timer.timerSpeed = 0.8f
                    doSpoof = false
                }else{
                    mc.timer.timerSpeed = 1.33f
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.onGround = true
                    doSpoof = true
                }
            }

            "hypixel" -> {
                hypixelTower()
            }
        }
    }

    private fun hypixelTower() {
        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.76, 0.0))
                .isNotEmpty() && mc.theWorld.getCollidingBoundingBoxes(
                mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.75, 0.0)
            ).isEmpty() && mc.thePlayer.motionY > 0.23 && mc.thePlayer.motionY < 0.25
        ) {
            mc.thePlayer.motionY = mc.thePlayer.posY.roundToInt() - mc.thePlayer.posY
        }
        if (mc.theWorld.getCollidingBoundingBoxes(
                mc.thePlayer,
                mc.thePlayer.entityBoundingBox.offset(0.0, -0.0001, 0.0)
            ).isNotEmpty()
        ) {
            mc.thePlayer.motionY = 0.41999998688698
        } else if (mc.thePlayer.posY >= mc.thePlayer.posY.roundToInt() - 0.0001 && mc.thePlayer.posY <= mc.thePlayer.posY.roundToInt() + 0.0001 && !Keyboard.isKeyDown(
                mc.gameSettings.keyBindSneak.keyCode
            )
        ) {
            mc.thePlayer.motionY = 0.0
        }
    }
    var changetick = 0
    private class BlockData2(position: BlockPos, facing: EnumFacing) {
        val position: BlockPos
        val facing: EnumFacing
        val hitVec: Vec3?

        init {
            this.position = position
            this.facing = facing
            hitVec = gethitVec()
        }

        private fun gethitVec(): Vec3? {
            val directionVec = facing.directionVec
            var x = directionVec.x.toDouble() * 0.5
            var z = directionVec.z.toDouble() * 0.5
            if (facing.axisDirection == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x
                z = -z
            }
            val hitVec = Vec3(position).addVector(x + z, directionVec.y.toDouble() * 0.5, x + z)
            val src = mc.thePlayer.getPositionEyes(1.0f)
            val obj = mc.theWorld.rayTraceBlocks(src, hitVec, false, false, true)
            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return null
            }
            if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
                obj.hitVec = obj.hitVec.addVector(0.0, -0.2, 0.0)
            }
            return obj.hitVec
        }
    }
    private fun isPosValid(pos: BlockPos): Boolean {
        val block = mc.theWorld.getBlockState(pos).block
        return ((block.material.isSolid || !block.isTranslucent || block.isVisuallyOpaque
                || block is BlockLadder || block is BlockCarpet || block is BlockSnow
                || block is BlockSkull) && !block.material.isLiquid
                && block !is BlockContainer)
    }
    private var blockData2: BlockData2? = null
    private fun getBlockData(pos: BlockPos): BlockData2? {
        if (isPosValid(pos.add(0, -1, 0))) {
            return BlockData2(pos.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos.add(-1, 0, 0))) {
            return BlockData2(pos.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos.add(1, 0, 0))) {
            return BlockData2(pos.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos.add(0, 0, 1))) {
            return BlockData2(pos.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos.add(0, 0, -1))) {
            return BlockData2(pos.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos2 = pos.add(-1, 0, 0)
        if (isPosValid(pos2.add(0, -1, 0))) {
            return BlockData2(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos2.add(-1, 0, 0))) {
            return BlockData2(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos2.add(1, 0, 0))) {
            return BlockData2(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos2.add(0, 0, 1))) {
            return BlockData2(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos2.add(0, 0, -1))) {
            return BlockData2(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos3 = pos.add(1, 0, 0)
        if (isPosValid(pos3.add(0, -1, 0))) {
            return BlockData2(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos3.add(-1, 0, 0))) {
            return BlockData2(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos3.add(1, 0, 0))) {
            return BlockData2(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos3.add(0, 0, 1))) {
            return BlockData2(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos3.add(0, 0, -1))) {
            return BlockData2(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos4 = pos.add(0, 0, 1)
        if (isPosValid(pos4.add(0, -1, 0))) {
            return BlockData2(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos4.add(-1, 0, 0))) {
            return BlockData2(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos4.add(1, 0, 0))) {
            return BlockData2(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos4.add(0, 0, 1))) {
            return BlockData2(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos4.add(0, 0, -1))) {
            return BlockData2(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos5 = pos.add(0, 0, -1)
        if (isPosValid(pos5.add(0, -1, 0))) {
            return BlockData2(pos5.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos5.add(-1, 0, 0))) {
            return BlockData2(pos5.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos5.add(1, 0, 0))) {
            return BlockData2(pos5.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos5.add(0, 0, 1))) {
            return BlockData2(pos5.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos5.add(0, 0, -1))) {
            return BlockData2(pos5.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(-2, 0, 0)
        if (isPosValid(pos2.add(0, -1, 0))) {
            return BlockData2(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos2.add(-1, 0, 0))) {
            return BlockData2(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos2.add(1, 0, 0))) {
            return BlockData2(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos2.add(0, 0, 1))) {
            return BlockData2(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos2.add(0, 0, -1))) {
            return BlockData2(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(2, 0, 0)
        if (isPosValid(pos3.add(0, -1, 0))) {
            return BlockData2(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos3.add(-1, 0, 0))) {
            return BlockData2(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos3.add(1, 0, 0))) {
            return BlockData2(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos3.add(0, 0, 1))) {
            return BlockData2(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos3.add(0, 0, -1))) {
            return BlockData2(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(0, 0, 2)
        if (isPosValid(pos4.add(0, -1, 0))) {
            return BlockData2(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos4.add(-1, 0, 0))) {
            return BlockData2(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos4.add(1, 0, 0))) {
            return BlockData2(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos4.add(0, 0, 1))) {
            return BlockData2(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos4.add(0, 0, -1))) {
            return BlockData2(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(0, 0, -2)
        if (isPosValid(pos5.add(0, -1, 0))) {
            return BlockData2(pos5.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos5.add(-1, 0, 0))) {
            return BlockData2(pos5.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos5.add(1, 0, 0))) {
            return BlockData2(pos5.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos5.add(0, 0, 1))) {
            return BlockData2(pos5.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos5.add(0, 0, -1))) {
            return BlockData2(pos5.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos6 = pos.add(0, -1, 0)
        if (isPosValid(pos6.add(0, -1, 0))) {
            return BlockData2(pos6.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos6.add(-1, 0, 0))) {
            return BlockData2(pos6.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos6.add(1, 0, 0))) {
            return BlockData2(pos6.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos6.add(0, 0, 1))) {
            return BlockData2(pos6.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos6.add(0, 0, -1))) {
            return BlockData2(pos6.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos7 = pos6.add(1, 0, 0)
        if (isPosValid(pos7.add(0, -1, 0))) {
            return BlockData2(pos7.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos7.add(-1, 0, 0))) {
            return BlockData2(pos7.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos7.add(1, 0, 0))) {
            return BlockData2(pos7.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos7.add(0, 0, 1))) {
            return BlockData2(pos7.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos7.add(0, 0, -1))) {
            return BlockData2(pos7.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos8 = pos6.add(-1, 0, 0)
        if (isPosValid(pos8.add(0, -1, 0))) {
            return BlockData2(pos8.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos8.add(-1, 0, 0))) {
            return BlockData2(pos8.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos8.add(1, 0, 0))) {
            return BlockData2(pos8.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos8.add(0, 0, 1))) {
            return BlockData2(pos8.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos8.add(0, 0, -1))) {
            return BlockData2(pos8.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos9 = pos6.add(0, 0, 1)
        if (isPosValid(pos9.add(0, -1, 0))) {
            return BlockData2(pos9.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos9.add(-1, 0, 0))) {
            return BlockData2(pos9.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos9.add(1, 0, 0))) {
            return BlockData2(pos9.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos9.add(0, 0, 1))) {
            return BlockData2(pos9.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos9.add(0, 0, -1))) {
            return BlockData2(pos9.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos10 = pos6.add(0, 0, -1)
        if (isPosValid(pos10.add(0, -1, 0))) {
            return BlockData2(pos10.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos10.add(-1, 0, 0))) {
            return BlockData2(pos10.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos10.add(1, 0, 0))) {
            return BlockData2(pos10.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos10.add(0, 0, 1))) {
            return BlockData2(pos10.add(0, 0, 1), EnumFacing.NORTH)
        }
        return if (isPosValid(pos10.add(0, 0, -1))) {
            BlockData2(pos10.add(0, 0, -1), EnumFacing.SOUTH)
        } else null
    }

    private fun rotation(){
        val sameY = sameYValue.get()
        val smartSpeed = smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state
        val autojump = autoJumpValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        val blockPos = BlockPos(mc.thePlayer.posX,
            if (((sameY && !sameYOnlyMove.get()) || (sameY && sameYOnlyMove.get() && MovementUtils.isMoving())) && (!towerActivation() || smartSpeed  || autojump) && launchY <= mc.thePlayer.posY) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0
            , mc.thePlayer.posZ)
        val blockData = get(blockPos)
        val blockData2 = if (getBlockData(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) == null
        ) getBlockData(
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ).down(1)
        ) else getBlockData(
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        )
        if (modeValue.get() == "OffGround" && mc.thePlayer.onGround)
            return
        when (rotationModeValue.get()) {
            "Novoline" -> {
                val entity = EntityPig(mc.theWorld)
                if (blockData2 != null) {
                    entity.posX = blockData2.position.x + 0.5
                }
                if (blockData2 != null) {
                    entity.posY = blockData2.position.y + 0.01
                }
                if (blockData2 != null) {
                    entity.posZ = blockData2.position.z + 0.5
                }

                lockRotation = RotationUtils.getRotationsNonLivingEntity(entity)
                faceBlock = true
            }
            "Spin" -> {
                spinYaw += speenSpeedValue.get()
                spinYaw = MathHelper.wrapAngleTo180_float(spinYaw)
                lockRotation = Rotation(spinYaw, speenPitchValue.get())
                faceBlock = true
            }
            "NormalPredict" ->{
                faceBlock = true
            }

            "Custom" -> {
                lockRotation = Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), customPitchValue.get())

                faceBlock = true
            }

            "IDK" -> {
                lockRotation = RotationUtils.faceBlock(blockData?.blockPos)?.rotation?.let { Rotation(mc.thePlayer.rotationYaw + 180, it.pitch) }
            }

            "IDK2" -> {
                lockRotation = RotationUtils.faceBlock(blockData?.blockPos)?.rotation
            }

            "Augustus" -> {
                lockRotation = rots
            }

            "Intave" -> {
                faceBlock = true
            }

            "Test" -> {
                var yaw = mc.thePlayer.rotationYaw
                var pitch = mc.thePlayer.rotationPitch

                val pos: BlockPos? = blockData?.blockPos
                val facing: EnumFacing? = blockData?.enumFacing
                val playerPos = mc.thePlayer.position
                when (facing) {
                    EnumFacing.EAST -> if (yaw > 136.0f || yaw < 44.0f) {
                        yaw = if (playerPos.z > pos?.z!!) {
                            135.0f
                        } else {
                            45.0f
                        }
                    }

                    EnumFacing.WEST -> if (yaw < -135.0f || yaw > -45.0f) {
                        yaw = if (playerPos.z > pos?.z!!) {
                            -135.0f
                        } else {
                            -45.0f
                        }
                    }

                    EnumFacing.NORTH -> if (yaw < -45.0f || yaw > 45.0f) {
                        yaw = if (playerPos.x > pos?.x!!) {
                            45.0f
                        } else {
                            -45.0f
                        }
                    }

                    EnumFacing.SOUTH -> if (yaw < 135.0f && yaw > -135.0f) {
                        yaw = if (playerPos.x > pos?.x!!) {
                            135.0f
                        } else {
                            -135.0f
                        }
                    }
                }

                if (!buildForward()) {
                    yaw = mc.thePlayer.rotationYaw + 180
                }

                if(staticPitch.get()){
                    pitch = 75f
                }else
                    if (facing == EnumFacing.UP) {
                        pitch = 90.0f
                    } else {
                        var found = false
                        var i = 75.0f
                        while (i <= 85.0f) {
                            val movingObjectPosition: MovingObjectPosition? = yaw?.let { raytrace(it, i) }
                            if (movingObjectPosition != null && movingObjectPosition.sideHit == blockData?.enumFacing) {
                                pitch = i
                                found = true
                                break
                            }
                            i += 0.5f
                        }
                        if (!found) {
                            pitch = 80.0f
                        }
                    }

                lockRotation = pitch?.let { yaw?.let { it1 -> Rotation(it1, it) } }
            }

            "GrimTest" ->{
                if(offGroundTicks >= 3){
                    faceBlock = true
                }else{
                    lockRotation = Rotation(mc.thePlayer.rotationYaw, if(grimLock.get()){
                        85f
                    } else{
                        mc.thePlayer.rotationPitch
                    }
                    )
                    faceBlock = false
                }
            }
            "Crazy"->{
                val random = nextDouble(0.0,0.10)
                val random2 = nextDouble(0.0,0.10)
                val random3 = nextDouble(0.02,0.09)
                val random4 = nextDouble(0.02,0.09)
                val random5 = nextDouble(0.01,0.03)
                val random6 = nextDouble(0.0,0.03)
                val random7 = nextDouble(0.0,0.03)
                val random8 = nextDouble(0.01,0.02)
                val random9 = nextDouble(0.01,0.02)
                val random10 = nextDouble(0.0,0.02)
                val random11 = nextDouble(0.0,0.02)
                if(mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.onGround){
                    lockRotation = RotationUtils.getRotations(
                        blockData2?.position?.x!!.toDouble() +random8-random10,
                        blockData2.position.y.toDouble(),
                        blockData2.position.z.toDouble()+random9-random11
                    )
                }
                if(!mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.onGround){
                    lockRotation = RotationUtils.getRotations(
                        blockData2?.position?.x!!.toDouble()+random-random6,
                        blockData2.position.y.toDouble(),
                        blockData2.position.z.toDouble()+random2-random7
                    )
                }
                if(!mc.thePlayer.onGround && !cantellyrotate){
                    tellydelay++
                    lockRotation = RotationUtils.getRotations(
                        blockData2?.position?.x!!.toDouble()+random3,
                        blockData2.position.y.toDouble() + random5,
                        blockData2.position.z.toDouble()+random4
                    )
                }

                faceBlock = true
            }
        }
        //faceBlock = true

        var rotation = lockRotation
        rotation =
            if (stabilizedRotation.get() && (!rotationModeValue.equals("Normal"))) {
                lockRotation?.let { Rotation(round(it.yaw / 45f) * 45f, it.pitch) }
            } else {
                rotation
            }
        RotationUtils.setTargetRotation(rotation)
        RotationUtils.setTargetRotation(lockRotation)

        faceBlock = true
    }

    fun raytrace(yaw: Float, pitch: Float): MovingObjectPosition {
        val partialTicks = mc.timer.renderPartialTicks
        val blockReachDistance = mc.playerController.blockReachDistance
        val vec3 = mc.thePlayer.getPositionEyes(partialTicks)
        val vec31 = getVectorForRotation(pitch, yaw)
        val vec32 = vec3.addVector(
            vec31.xCoord * blockReachDistance.toDouble(),
            vec31.yCoord * blockReachDistance.toDouble(),
            vec31.zCoord * blockReachDistance.toDouble()
        )
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true)
    }

    fun getVectorForRotation(yaw: Float, pitch: Float): Vec3 {
        fun Float.toRadians() = this * 0.017453292f
        val yawRad = yaw.toRadians()
        val pitchRad = pitch.toRadians()

        val f = MathHelper.cos(-yawRad - PI.toFloat())
        val f1 = MathHelper.sin(-yawRad - PI.toFloat())
        val f2 = -MathHelper.cos(-pitchRad)
        val f3 = MathHelper.sin(-pitchRad)

        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }



    /*fun getRotations(yawOffset: Float) {
        var found = false
        var possibleYaw = mc.thePlayer.rotationYaw - 180 + yawOffset
        while (possibleYaw <= mc.thePlayer.rotationYaw + 360 - 180 && !found) {
            var possiblePitch = 90f
            while (possiblePitch > 30 && !found) {
                if (RayCastUtil.overBlock(
                        Vector2f(possibleYaw, possiblePitch),
                        enumFacing2!!.getEnumFacing(),
                        blockFace,
                        true
                    )
                ) {
                    targetYaw = possibleYaw
                    targetPitch = possiblePitch
                    found = true
                }
                possiblePitch -= (if (possiblePitch > (if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 60 else 80)) 1 else 10).toFloat()
            }
            possibleYaw += 45f
        }
        if (!found) {
            val rotations: Vector2f = RiseRotationUtils.calculate(
                Vector3d(blockFace!!.getX().toDouble(), blockFace!!.getY().toDouble(), blockFace!!.getZ().toDouble()), enumFacing2!!.getEnumFacing()
            )
            targetYaw = rotations.x
            targetPitch = rotations.y
        }
    }*/

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onTick(event:TickEvent){
        blockPos = this.getAimBlockPos()
        start =
            mc.thePlayer.motionX === 0.0 && mc.thePlayer.motionZ === 0.0 && mc.thePlayer.onGround || !this.startTimeHelper.hasTimePassed(
                200L
            )
        CanCancelRotate = (CancelRotateMode.equals("Ground") && mc.thePlayer.onGround) || (CancelRotateMode.equals("All")) || (CancelRotateMode.equals("Air")&& !mc.thePlayer.onGround)
        if(!CanCancelRotate)rotation()

        if (blockPos != null) {
            rots = getNearestRotation()
        }
        if (objectPosition != null) {
            mc.objectMouseOver = objectPosition
        }

        if(!keepRotation.get() && CanCancelRotate && rotateTiming.equals("All")){
            lockRotation = Rotation(mc.thePlayer.rotationYaw, customPitchValue.get())
            if (!keepRotation.get() && !shouldGoDown) {
                var dif = 0.5
                if (rotateEdgeDistanceValue.get() > 0) {
                    for (i in 0..3) {
                        val blockPos = BlockPos(
                            mc.thePlayer.posX + if (i == 0) -1 else if (i == 1) 1 else 0,
                            mc.thePlayer.posY - if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0,
                            mc.thePlayer.posZ + if (i == 2) -1 else if (i == 3) 1 else 0
                        )
                        val placeInfo = get(blockPos)
                        if (isReplaceable(blockPos) && placeInfo != null) {
                            var calcDif = if (i > 1) mc.thePlayer.posZ - blockPos.z else mc.thePlayer.posX - blockPos.x
                            calcDif -= 0.5
                            if (calcDif < 0) calcDif *= -1.0
                            calcDif -= 0.5
                            if (calcDif < dif) dif = calcDif
                        }
                    }
                }
                if (placedBlocksWithoutRotate >= blockstorotate.get()) {
                    val shouldRotate = mc.theWorld.getBlockState(
                        BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                    ).block === Blocks.air || dif < if(mc.thePlayer.isPotionActive(Potion.moveSpeed) && !changerotate)rotateEdgeDistanceValue.get() + whenspeedrotate.get() else if(changerotate && mc.thePlayer.isPotionActive(Potion.moveSpeed))rotateEdgeDistanceValue.get() + whenspeedrotate.get()-0.03.toFloat() else rotateEdgeDistanceValue.get()
                    if(shouldRotate){
                        rotation()
                    }
                    placedBlocksWithoutRotate = 0
                } else placedBlocksWithoutRotate++
            }
        }

        if((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get() === "Tick"){
            place()
        }
        if(autoJumpValue.get()){
            if (!shouldGoDown) {
                var dif = 0.5
                if (jumpEdgeDistanceValue.get() > 0) {
                    for (i in 0..3) {
                        val blockPos = BlockPos(
                            mc.thePlayer.posX + if (i == 0) -1 else if (i == 1) 1 else 0,
                            mc.thePlayer.posY - if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0,
                            mc.thePlayer.posZ + if (i == 2) -1 else if (i == 3) 1 else 0
                        )
                        val placeInfo = get(blockPos)
                        if (isReplaceable(blockPos) && placeInfo != null) {
                            var calcDif = if (i > 1) mc.thePlayer.posZ - blockPos.z else mc.thePlayer.posX - blockPos.x
                            calcDif -= 0.5
                            if (calcDif < 0) calcDif *= -1.0
                            calcDif -= 0.5
                            if (calcDif < dif) dif = calcDif
                        }
                    }
                }
                if (placedBlocksWithoutJump >= blockstoJump.get()) {
                    val shouldJump = mc.theWorld.getBlockState(
                        BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                    ).block === Blocks.air || dif < jumpEdgeDistanceValue.get()
                    if(shouldJump){
                        if(!LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state && MovementUtils.isMoving() && mc.thePlayer.onGround){
                            mc.thePlayer.motionY = 0.42
                        }
                    }
                    placedBlocksWithoutJump = 0
                } else placedBlocksWithoutJump++
            }
        }
    }

    private fun isOkBlock(blockPos: BlockPos): Boolean {
        val block = mc.theWorld.getBlockState(blockPos).block
        return block !is BlockLiquid && block !is BlockAir && block !is BlockChest && block !is BlockFurnace
    }

    private fun getBlockPos(): ArrayList<BlockPos> {
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        val blockPoses = ArrayList<BlockPos>()
        for (x in playerPos.x - 2..playerPos.x + 2) {
            for (y in playerPos.y - 1..playerPos.y) {
                for (z in playerPos.z - 2..playerPos.z + 2) {
                    if (isOkBlock(BlockPos(x, y, z))) {
                        blockPoses.add(BlockPos(x, y, z))
                    }
                }
            }
        }
        if (blockPoses.isNotEmpty()) {
            blockPoses.sortWith(Comparator.comparingDouble { blockPos: BlockPos ->
                mc.thePlayer.getDistanceSq(
                    blockPos.x.toDouble() + 0.5,
                    blockPos.y.toDouble() + 0.5,
                    blockPos.z.toDouble() + 0.5
                )
            })
        }
        return blockPoses
    }

    private fun getAimBlockPos(): BlockPos? {
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        return if ((mc.gameSettings.keyBindJump.isKeyDown || !mc.thePlayer.onGround) && mc.thePlayer.moveForward === 0.0f && mc.thePlayer.moveStrafing === 0.0f && isOkBlock(
                playerPos.add(0, -1, 0)
            )
        ) {
            playerPos.add(0, -1, 0)
        } else {
            var blockPos: BlockPos? = null
            val bp: ArrayList<BlockPos> = this.getBlockPos()
            val blockPositions = ArrayList<BlockPos>()
            if (!bp.isEmpty()) {
                for (i in 0 until min(bp.size.toDouble(), 18.0).toInt()) {
                    blockPositions.add(bp[i])
                }
                blockPositions.sortWith<BlockPos>(Comparator.comparingDouble<BlockPos> { blockPos: BlockPos ->
                    this.getDistanceToBlockPos(
                        blockPos
                    )
                })
                if (blockPositions.isNotEmpty()) {
                    blockPos = blockPositions[0]
                }
            }
            blockPos
        }
    }

    private fun buildForward(): Boolean {
        val realYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)
        return realYaw > 77.5 && realYaw < 102.5 || realYaw > 167.5 || realYaw < -167.0f || realYaw < -77.5 && realYaw > -102.5 || realYaw > -12.5 && realYaw < 12.5
    }

    private fun getNearestRotation(): Rotation {
        this.objectPosition = null
        var rot: Rotation = this.rots
        val b = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
        this.hashMap.clear()
        if (this.start) {
            rot.pitch = (80.34f)
            rot.yaw = (mc.thePlayer.rotationYaw + yawOffset.get())
            rot = RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                rot,
                yawSpeed.get() + RandomUtils.nextFloat(0f, 2f)
            )
        } else {
            rot.yaw = (mc.thePlayer.rotationYaw + yawOffset.get())
            rot = RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                rot,
                yawSpeed.get() + RandomUtils.nextFloat(0f, 2f)
            )
            var x = mc.thePlayer.posX
            var z = mc.thePlayer.posZ
            val add1 = 1.288
            val add2 = 0.288
            if (!buildForward()) {
                x += mc.thePlayer.posX - this.xyz.get(0)
                z += mc.thePlayer.posZ - this.xyz.get(2)
            }
            this.xyz = doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            val maX: Double = this.blockPos?.getX()?.toDouble()!! + add1
            val miX: Double = this.blockPos?.getX()?.toDouble()!! - add2
            val maZ: Double = this.blockPos?.getZ()?.toDouble()!! + add1
            val miZ: Double = this.blockPos?.getZ()?.toDouble()!! - add2
            if (x <= maX && x >= miX && z <= maZ && z >= miZ) {
                rot.pitch = (rots.pitch)
            } else {
                val movingObjectPositions = ArrayList<MovingObjectPosition>()
                val pitches = ArrayList<Float>()
                val vec3 = mc.thePlayer.getPositionEyes(1f)
                var mm = Math.max(rots.pitch - 20.0f, -90.0f)
                while (mm < Math.min(rots.pitch + 20.0f, 90.0f)) {
                    rot.pitch = (mm)
                    //这个数值我是乱填的，因为我没有他那个fixedSensitivity2，所以如果转头有问题，那大概率是这个
                    rot.fixedSensitivity(mm)
                    val vec31: Vec3 = (rot.toDirection() * 4.5).add(vec3)
                    val m4 = mc.theWorld.rayTraceBlocks(vec3, vec31, false, false, true)
                    if (m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.isOkBlock(m4.blockPos) && m4.blockPos == this.blockPos && m4.sideHit != EnumFacing.DOWN && (m4.sideHit != EnumFacing.UP || !sameYValue.get() && mc.gameSettings.keyBindJump.isKeyDown) && m4.blockPos.y <= b.y) {
                        movingObjectPositions.add(m4)
                        val rotPitch: Float = rot.pitch
                        this.hashMap.put(rotPitch, m4)
                        pitches.add(rotPitch)
                    }
                    mm += 0.02f
                }
                movingObjectPositions.sortWith(Comparator.comparingDouble { m: MovingObjectPosition ->
                    mc.thePlayer.getDistanceSq(
                        m.blockPos.add(0.5, 0.5, 0.5)
                    )
                })
                var mm1: MovingObjectPosition? = null
                if (!movingObjectPositions.isEmpty()) {
                    mm1 = movingObjectPositions[0]
                }
                if (mm1 != null) {
                    pitches.sortWith<Float>(Comparator.comparingDouble<Float> { pitch: Float ->
                        this.distanceToLastPitch(
                            pitch
                        )
                    })
                    if (pitches.isNotEmpty()) {
                        val rotPitch = pitches[0]
                        rot.pitch = (rotPitch)
                        this.objectPosition = this.hashMap[rotPitch]
                        this.blockPos = this.objectPosition?.blockPos
                    }
                    return rot
                }
            }
        }
        return rot
    }

    private fun getDistanceToBlockPos(blockPos: BlockPos): Double {
        var distance = 1337.0
        var x = blockPos.x.toFloat()
        while (x <= (blockPos.x + 1).toFloat()) {
            var y = blockPos.y.toFloat()
            while (y <= (blockPos.y + 1).toFloat()) {
                var z = blockPos.z.toFloat()
                while (z <= (blockPos.z + 1).toFloat()) {
                    val d0 = mc.thePlayer.getDistanceSq(x.toDouble(), y.toDouble(), z.toDouble())
                    if (d0 < distance) {
                        distance = d0
                    }
                    z = (z.toDouble() + 0.2).toFloat()
                }
                y = (y.toDouble() + 0.2).toFloat()
            }
            x = (x.toDouble() + 0.2).toFloat()
        }
        return distance
    }

    private fun distanceToLastPitch(pitch: Float): Double {
        return Math.abs(pitch - this.rots.pitch).toDouble()
    }

    var safeWalking = false
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (telly.get()) {
            if (mc.gameSettings.keyBindJump.isKeyDown || mc.gameSettings.keyBindJump.pressed || GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
                sameYValue.set(false)
            } else {
                if (MovementUtils.isMoving() && mc.thePlayer.onGround){
                    mc.thePlayer.jump()
                }
                sameYValue.set(true)
            }
        }
        if(safeWalkMoveValue.get() && safeWalkValue.get() && mc.theWorld!!.getCollidingBoundingBoxes(mc.thePlayer!!, mc.thePlayer!!.entityBoundingBox
                .offset(0.0, -0.5, 0.0).expand(-safeWalkMoveSpeedValue.get().toDouble(), 0.0, -safeWalkMoveSpeedValue.get().toDouble())).isEmpty()||(airSafeValue.get() && !mc.thePlayer!!.onGround)) {
            mc.gameSettings.keyBindForward.pressed = false
            mc.gameSettings.keyBindBack.pressed = false
            mc.gameSettings.keyBindRight.pressed = false
            mc.gameSettings.keyBindLeft.pressed = false
            safeWalking = true
        }else{
            safeWalking = false
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        }
        if (rotationsValue.get() && keepRotation.get()) {
            rotation()
        }
        if(tellydelay>=sadasd.get()){
            cantellyrotate = true
        }
        if(mc.thePlayer.onGround){
            cantellyrotate = false
            tellydelay = 0
        }

        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get() === "Legit") {
            place()
        }
        if (towerActivation()) {
            shouldGoDown = false
            mc.gameSettings.keyBindSneak.pressed = false
        }
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
        } else offGroundTicks++
        if (sprintModeValue.get().equals("PlaceOff", ignoreCase = true)) {
            mc.thePlayer.isSprinting = true
        }
        if (sprintModeValue.get().equals("PlaceOn", ignoreCase = true)) {
            mc.thePlayer.isSprinting = false
        }
        if (clickTimer.hasTimePassed(clickDelay)) {
            fun sendPacket(c08: C08PacketPlayerBlockPlacement) {
                if (clickDelay < 35) {
                    mc.netHandler.addToSendQueue(c08)
                }
                if (clickDelay < 50) {
                    mc.netHandler.addToSendQueue(c08)
                }
                mc.netHandler.addToSendQueue(c08)
            }
            when (extraClickValue.get().toLowerCase()) {
                "emptyc08" -> sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)))
                "afterplace" -> {
                    if (afterPlaceC08 != null) {
                        if (mc.thePlayer.getDistanceSqToCenter(lastPlaceBlock) <10) {
                            sendPacket(afterPlaceC08!!)
                        } else {
                            afterPlaceC08 = null
                        }
                    }
                }
                "raytrace" -> {
                    val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(5.0)
                    if (rayTraceInfo != null && BlockUtils.getBlock(rayTraceInfo.blockPos) != Blocks.air) {
                        val blockPos = rayTraceInfo.blockPos
                        val hitVec = rayTraceInfo.hitVec
                        val directionVec = rayTraceInfo.sideHit.directionVec
                        val targetPos = rayTraceInfo.blockPos.add(directionVec.x, directionVec.y, directionVec.z)
                        if (mc.thePlayer.entityBoundingBox.intersectsWith(Blocks.stone.getSelectedBoundingBox(mc.theWorld, targetPos))) {
                            sendPacket(C08PacketPlayerBlockPlacement(blockPos, rayTraceInfo.sideHit.index, mc.thePlayer.inventory.getStackInSlot(slot), (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(), (hitVec.zCoord - blockPos.z.toDouble()).toFloat()))
                        } else {
                            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)))
                        }
                    }
                }
            }
            clickDelay = TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
            clickTimer.reset()
        }
        /*var blockSlot = -1
        if (mc.thePlayer.heldItem == null || mc.thePlayer.heldItem.item !is ItemBlock) {
            if (autoBlockMode.get().equals("Off", ignoreCase = true)) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            if (autoBlockMode.get().equals("Spoof", ignoreCase = true)) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            } else {
                mc.thePlayer.inventory.currentItem = blockSlot - 36
                mc.playerController.updateController()
            }
        }*/
        //mc.timer.timerSpeed = timerValue.get()
        shouldGoDown =
            downValue.get() && (!sameYValue.get()||(sameYValue.get() && !MovementUtils.isMoving() && sameYOnlyMove.get())) && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false

        // scaffold custom speed if enabled
        if (customSpeedValue.get()) MovementUtils.strafe(customMoveSpeedValue.get())
        if (mc.thePlayer.onGround) {

            // Smooth Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("smooth", ignoreCase = true)) {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                if (zitterTimer.hasTimePassed(zitterDelay.get().toLong())) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }
                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }


            // Eagle
            if (eagleValue.get() && !shouldGoDown) {
                var dif = 0.5
                if (eagleEdgeDistanceValue.get() > 0) {
                    for (i in 0..3) {
                        val blockPos = BlockPos(
                            mc.thePlayer.posX + if (i == 0) -1 else if (i == 1) 1 else 0,
                            mc.thePlayer.posY - if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0,
                            mc.thePlayer.posZ + if (i == 2) -1 else if (i == 3) 1 else 0
                        )
                        val placeInfo = get(blockPos)
                        if (isReplaceable(blockPos) && placeInfo != null) {
                            var calcDif = if (i > 1) mc.thePlayer.posZ - blockPos.z else mc.thePlayer.posX - blockPos.x
                            calcDif -= 0.5
                            if (calcDif < 0) calcDif *= -1.0
                            calcDif -= 0.5
                            if (calcDif < dif) dif = calcDif
                        }
                    }
                }
                var shouldEagle = mc.theWorld.getBlockState(
                    BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                ).block === Blocks.air || dif < eagleEdgeDistanceValue.get()
                if(shouldEagle){
                    tellydelay = 0
                }
                if(shouldEagle && eagledelay.get()){
                    eagledelaytick++
                }
                if(eagledelaytick == 1){
                    candelayeagle = true
                }
                if((mc.theWorld.getBlockState(
                        BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                    ).block != Blocks.air && candelayeagle) || !shouldEagle){
                    cantellyrotate = false
                    candelayeagle = false
                    eagledelaytick = 0
                    shouldEagle = false

                }
                //if(candelayeagle){
                //rotation()
                //}
                if(eagledelay.get()){
                    mc.gameSettings.keyBindSneak.pressed = candelayeagle
                    //if(candelayeagle)ClientUtils.displayChatMessage("DelaySneaking")else ClientUtils.displayChatMessage("StopDelaySneaking")
                }
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    if (eagleSilentValue.get()) {
                        if (eagleSneaking != shouldEagle) {
                            if(shouldEagle){
                                if(isPre1){
                                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.START_SNEAKING))
                                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                                }
                                if(!isPre1){
                                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.START_SNEAKING))
                                    mc.netHandler.addToSendQueue(C0FPacketConfirmTransaction())
                                    mc.netHandler.addToSendQueue(C0FPacketConfirmTransaction())
                                }
                            }else{
                                mc.netHandler.addToSendQueue(
                                    C0BPacketEntityAction(
                                        mc.thePlayer,
                                        C0BPacketEntityAction.Action.STOP_SNEAKING
                                    )
                                )
                            }
                        }
                        eagleSneaking = shouldEagle
                    }
                    else if(!eagledelay.get()){
                        mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    }
                    placedBlocksWithoutEagle = 0
                } else placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)) {
                MovementUtils.strafe(zitterSpeed.get())
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                mc.thePlayer.motionX -= sin(yaw) * zitterStrength.get()
                mc.thePlayer.motionZ += cos(yaw) * zitterStrength.get()
                zitterDirection = !zitterDirection
            }
        }
        if (sprintModeValue.get().equals("off", ignoreCase = true) || sprintModeValue.get()
                .equals("ground", ignoreCase = true) && !mc.thePlayer.onGround || sprintModeValue.get()
                .equals("air", ignoreCase = true) && mc.thePlayer.onGround || sprintModeValue.get()
                .equals("falldownoff", ignoreCase = true) && mc.thePlayer.fallDistance > 0 ||
            sprintModeValue.get().equals("grimoff", ignoreCase = true) && offGroundTicks >= 3 ||(
                    sprintModeValue.get().equals("smart",ignoreCase = true) && mc.thePlayer.moveForward > 0.3 && (lockRotation == null || (abs(RotationUtils.getAngleDifference(lockRotation!!.yaw, mc.thePlayer.rotationYaw)) < 90)))
        ) {
            mc.thePlayer.isSprinting = false
        }

        //Auto Jump thingy
        if (shouldGoDown) {
            launchY = mc.thePlayer.posY.toInt() - 1
        } else if (!sameYValue.get() || (sameYValue.get() && !MovementUtils.isMoving() && sameYOnlyMove.get())) {
            if (!autoJumpValue.get() && !(smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state) || GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || mc.thePlayer.posY < launchY) launchY = mc.thePlayer.posY.toInt()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet
        //Verus
        if (packet is C03PacketPlayer) {
            if (doSpoof) {
                doSpoof = false
                packet.onGround = true
            }
        }

        if (sprintModeValue.get().equals("NoPacket", ignoreCase = true)) {
            if (packet is C0BPacketEntityAction &&
                (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING)
            ) event.cancelEvent()
        }

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            if(packet.slotId == slot) {
                event.cancelEvent()
            } else {
                slot = packet.slotId
            }
        }else if (packet is C08PacketPlayerBlockPlacement) {
            // c08 item override to solve issues in scaffold and some other modules, maybe bypass some anticheat in future
            packet.stack = mc.thePlayer.inventory.mainInventory[slot]
            // illegal facing checks
            packet.facingX = packet.facingX.coerceIn(-1.0000F, 1.0000F)
            packet.facingY = packet.facingY.coerceIn(-1.0000F, 1.0000F)
            packet.facingZ = packet.facingZ.coerceIn(-1.0000F, 1.0000F)
        }
        if((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get() === "Packet"){
            place()
        }
    }

    @EventTarget //took it from applyrotationstrafe XD. staticyaw comes from bestnub.
    fun onStrafe(event: StrafeEvent) {
        if (TestStrafeFix.get()) {
            val yaw = if (lockRotation != null) lockRotation!!.yaw else RotationUtils.targetRotation.yaw
            val dif = ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 23.5f - 135) + 180) / 45).toInt()
            val strafe = event.strafe
            val forward = event.forward
            val friction = event.friction
            var calcForward = 0f
            var calcStrafe = 0f
            when (dif) {
                0 -> {
                    calcForward = forward
                    calcStrafe = strafe
                }

                1 -> {
                    calcForward += forward
                    calcStrafe -= forward
                    calcForward += strafe
                    calcStrafe += strafe
                }

                2 -> {
                    calcForward = strafe
                    calcStrafe = -forward
                }

                3 -> {
                    calcForward -= forward
                    calcStrafe -= forward
                    calcForward += strafe
                    calcStrafe -= strafe
                }

                4 -> {
                    calcForward = -forward
                    calcStrafe = -strafe
                }

                5 -> {
                    calcForward -= forward
                    calcStrafe += forward
                    calcForward -= strafe
                    calcStrafe -= strafe
                }

                6 -> {
                    calcForward = -strafe
                    calcStrafe = forward
                }

                7 -> {
                    calcForward += forward
                    calcStrafe += forward
                    calcForward -= strafe
                    calcStrafe += strafe
                }
            }
            if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
                calcForward *= 0.5f
            }

            if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
                calcStrafe *= 0.5f
            }

            var f = calcStrafe * calcStrafe + calcForward * calcForward

            if (f >= 1.0E-4f) {
                f = MathHelper.sqrt_float(f)

                if (f < 1.0f) f = 1.0f

                f = friction / f
                calcStrafe *= f
                calcForward *= f

                val yawSin = MathHelper.sin((yaw * Math.PI / 180f).toFloat())
                val yawCos = MathHelper.cos((yaw * Math.PI / 180f).toFloat())

                mc.thePlayer.motionX += calcStrafe * yawCos - calcForward * yawSin
                mc.thePlayer.motionZ += calcForward * yawCos + calcStrafe * yawSin
            }
            event.cancelEvent()
        }
        if(safeWalkMoveValue.get() && safeWalkValue.get() &&mc.theWorld!!.getCollidingBoundingBoxes(mc.thePlayer!!, mc.thePlayer!!.entityBoundingBox
                .offset(0.0, -0.5, 0.0).expand(-safeWalkMoveSpeedValue.get().toDouble(), 0.0, -safeWalkMoveSpeedValue.get().toDouble())).isEmpty()||(airSafeValue.get() && !mc.thePlayer!!.onGround)) {
            mc.gameSettings.keyBindForward.pressed = false
            mc.gameSettings.keyBindBack.pressed = false
            mc.gameSettings.keyBindRight.pressed = false
            mc.gameSettings.keyBindLeft.pressed = false
            event.cancelEvent()
        }else{
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        }
        if (modeValue.get() == "OffGround" && mc.thePlayer.onGround)
            return
        if (lockRotation != null && rotationStrafeValue.get()) {
            val dif =
                ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - lockRotation!!.yaw - 23.5f - 135) + 180) / 45).toInt()
            val yaw = lockRotation!!.yaw
            val strafe = event.strafe
            val forward = event.forward
            val friction = event.friction
            var calcForward = 0f
            var calcStrafe = 0f
            when (dif) {
                0 -> {
                    calcForward = forward
                    calcStrafe = strafe
                }

                1 -> {
                    calcForward += forward
                    calcStrafe -= forward
                    calcForward += strafe
                    calcStrafe += strafe
                }

                2 -> {
                    calcForward = strafe
                    calcStrafe = -forward
                }

                3 -> {
                    calcForward -= forward
                    calcStrafe -= forward
                    calcForward += strafe
                    calcStrafe -= strafe
                }

                4 -> {
                    calcForward = -forward
                    calcStrafe = -strafe
                }

                5 -> {
                    calcForward -= forward
                    calcStrafe += forward
                    calcForward -= strafe
                    calcStrafe -= strafe
                }

                6 -> {
                    calcForward = -strafe
                    calcStrafe = forward
                }

                7 -> {
                    calcForward += forward
                    calcStrafe += forward
                    calcForward -= strafe
                    calcStrafe += strafe
                }
            }
            if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
                calcForward *= 0.5f
            }
            if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
                calcStrafe *= 0.5f
            }
            var f = calcStrafe * calcStrafe + calcForward * calcForward
            if (f >= 1.0E-4f) {
                f = MathHelper.sqrt_float(f)
                if (f < 1.0f) f = 1.0f
                f = friction / f
                calcStrafe *= f
                calcForward *= f
                val yawSin = MathHelper.sin((yaw * Math.PI / 180f).toFloat())
                val yawCos = MathHelper.cos((yaw * Math.PI / 180f).toFloat())
                mc.thePlayer.motionX += (calcStrafe * yawCos - calcForward * yawSin).toDouble()
                mc.thePlayer.motionZ += (calcForward * yawCos + calcStrafe * yawSin).toDouble()
            }
            event.cancelEvent()
        }
    }

    private fun shouldPlace(): Boolean {
        val placeWhenAir2  = modeValue.get() == "OffGround" && !mc.thePlayer.onGround
        val placeWhenAir = placeConditionValue.get().equals("air", ignoreCase = true)
        val placeWhenFall = placeConditionValue.get().equals("falldown", ignoreCase = true)
        val placeWhenNegativeMotion = placeConditionValue.get().equals("negativemotion", ignoreCase = true)
        val alwaysPlace = placeConditionValue.get().equals("always", ignoreCase = true)
        return towerActivation() || alwaysPlace ||  placeWhenAir2 || placeWhenAir && !mc.thePlayer.onGround || placeWhenFall && mc.thePlayer.fallDistance > 0 || placeWhenNegativeMotion && mc.thePlayer.motionY < 0
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        towerStatus = false
        // Tower
        towerStatus = BlockUtils.getBlock(
            BlockPos(
                mc.thePlayer.posX,
                mc.thePlayer.posY + 2,
                mc.thePlayer.posZ
            )
        ) is BlockAir
       towerStatus = mc.gameSettings.keyBindJump.isKeyDown
        if(event.eventState==EventState.PRE){
            isPre1 = true
        }else{
            isPre1 = false
        }
        // XZReducer
        mc.thePlayer.motionX *= xzMultiplier.get().toDouble()
        mc.thePlayer.motionZ *= xzMultiplier.get().toDouble()
        if (speedPotSlow.get()) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                mc.thePlayer.motionX = mc.thePlayer.motionX * 0.85f
                mc.thePlayer.motionZ = mc.thePlayer.motionZ * 0.85f
            }
        }
        if(strafefix.get()){
            LiquidBounce.moduleManager[StrafeFix::class.java]!!.state = true
        }
        if(canrotate){
            rotatetick++
        }
        if(canrotate){
            stoprotatetick++
        }
        if (event.eventState === EventState.PRE) {
            if (!shouldPlace() || (if (!autoBlockMode.get().equals("Off", ignoreCase = true))
                    if(sortByHighestAmount.get()){
                        InventoryUtils.findAutoBlockBlock()!! == -1
                    }else{
                        InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock)
                    }
                else
                    mc.thePlayer.heldItem == null ||
                            mc.thePlayer.heldItem.item !is ItemBlock)
            ) return
            findBlock(modeValue.get() == "Expand" && expandLengthValue.get() > 1, area = true)
        }
        when(event.eventState){
            EventState.POST -> {
                when(rotateTiming.get()) {
                    "Post"->{
                        lockRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
                        LiquidBounce.moduleManager[StrafeFix::class.java]!!.state = true
                    }
                    "DelayPost"->{
                        if(!canrotate && PostStopRotateTick.get()>=stoprotatetick){
                            lockRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
                        }
                        if(!canrotate && PostStopRotateTick.get()<stoprotatetick){
                            stoprotatetick = 0
                            canrotate = true
                        }
                        LiquidBounce.moduleManager[StrafeFix::class.java]!!.state = true
                    }
                }
            }

            EventState.PRE -> {
                when(rotateTiming.get()) {
                    "Post"->{
                        rotation()
                        LiquidBounce.moduleManager[StrafeFix::class.java]!!.state = true
                    }
                    "DelayPost"->{
                        if(PostKeepRotateTick.get()>=rotatetick && canrotate){
                            rotation()
                        }
                        if(PostKeepRotateTick.get()<rotatetick && canrotate){
                            rotatetick = 0
                            canrotate = false
                        }
                        LiquidBounce.moduleManager[StrafeFix::class.java]!!.state = true
                    }
                }
            }
        }
        val mode = modeValue.get()
        val eventState = event.eventState

        // I think patches should be here instead
        for (i in 0..7) {
            if (mc.thePlayer.inventory.mainInventory[i] != null
                && mc.thePlayer.inventory.mainInventory[i].stackSize <= 0
            ) mc.thePlayer.inventory.mainInventory[i] = null
        }
        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true)
        ) {
            place()
        }
        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true) && towerActivation()
        ) {
            place()
        }
        if (eventState === EventState.PRE) {
            if (!shouldPlace() || (if (!autoBlockMode.get()
                        .equals("Off", ignoreCase = true)
                ) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null ||
                        mc.thePlayer.heldItem.item !is ItemBlock)
            ) return
            findBlock(mode == "Expand" && expandLengthValue.get() > 1, area = true)
        }
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
        }
        if (!towerActivation()) {
            verusState = 0
            return
        }
        mc.timer.timerSpeed = towerTimerValue.get()
        if (placeModeValue.get().equals(eventState.stateName, ignoreCase = true)) place()

        if(event.eventState == EventState.POST){
            if (towerActivation()) {
                move(event)
            }
        }
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean, area: Boolean) {
        val player = mc.thePlayer ?: return

        val sameY = sameYValue.get()
        val smartSpeed = smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state
        val autojump = autoJumpValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        val blockPos = BlockPos(
            mc.thePlayer.posX,
            if (((sameY && !sameYOnlyMove.get()) || (sameY && sameYOnlyMove.get() && MovementUtils.isMoving())) && (!towerActivation() || smartSpeed  || autojump) && launchY <= mc.thePlayer.posY) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
            mc.thePlayer.posZ
        )

        if (!expand && (!isReplaceable(blockPos) || search(blockPos, !shouldGoDown, area))) {
            return
        }

        if (expand) {
            val yaw = player.rotationYaw.toDouble()
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPos.add(x * i, 0, z * i), false, area)) {
                    return
                }
            }
        } else {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPos.add(x, 0, z), !shouldGoDown, area)) {
                        return
                    }
                }
            }
        }
    }

    /**
     * Place target block
     */
    private fun randomNumber(max: Double, min: Double): Double {
        return Math.random() * (max - min) + min
    }
    fun getVec3(pos: BlockPos, facing: EnumFacing): Vec3 {
        var x = pos.x + 0.5
        var y = pos.y + 0.5
        var z = pos.z + 0.5
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            x += randomNumber(0.3, -0.3)
            z += randomNumber(0.3, -0.3)
        } else {
            y += randomNumber(0.3, -0.3)
        }
        if (facing == EnumFacing.WEST || facing == EnumFacing.EAST) {
            z += randomNumber(0.3, -0.3)
        }
        if (facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH) {
            x += randomNumber(0.3, -0.3)
        }
        return Vec3(x, y, z)
    }
    private fun place() {
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
            return
        }

        if (!towerActivation() && (!delayTimer.hasTimePassed(delay) || smartDelay.get() && mc.rightClickDelayTimer > 0 || ((autoJumpValue.get() || smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state) && !GameSettings.isKeyDown(
                mc.gameSettings.keyBindJump
            )) && launchY - 1 != targetPlace!!.vec3.yCoord.toInt())
        ) {
            if(sameYValue.get() && !sameYOnlyMove.get() || sameYValue.get() && MovementUtils.isMoving() && sameYOnlyMove.get())
                return
        }
        /*var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || mc.thePlayer.heldItem.item !is ItemBlock) {
            if (autoBlockMode.get().equals("Off", ignoreCase = true)) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            if (autoBlockMode.get().equals("Spoof", ignoreCase = true) || autoBlockMode.get().equals("LiteSpoof", ignoreCase = true)) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
                itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
            } else {
                mc.thePlayer.inventory.currentItem = blockSlot - 36
                mc.playerController.updateController()
            }
        }*/
        val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(6.0)
        if(!hitableCheckOnGround.get()){
            when (hitableCheckValue.get().toLowerCase()) {
                "simple" -> {

                    if (rayTraceInfo != null && (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos))) {
                        return
                    }
                }

                "strict" -> {

                    if (rayTraceInfo != null && (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos) || rayTraceInfo.sideHit != targetPlace!!.enumFacing)) {
                        return
                    }
                }
            }
        }
        if(hitableCheckOnGround.get()){
            if(mc.thePlayer.onGround){
                when (hitableCheckOnGroundValue.get().toLowerCase()) {
                    "simple" -> {
                        if (rayTraceInfo != null && !rayTraceInfo.blockPos.equals(targetPlace!!.blockPos)) {
                            return
                        }
                    }

                    "strict" -> {
                        if (rayTraceInfo != null && (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos) || rayTraceInfo.sideHit != targetPlace!!.enumFacing)) {
                            return
                        }
                    }
                }
            }
            if(!mc.thePlayer.onGround){
                when (hitableCheckValue.get().toLowerCase()) {
                    "simple" -> {

                        if (rayTraceInfo != null && !rayTraceInfo.blockPos.equals(targetPlace!!.blockPos)) {
                            return
                        }
                    }
                    "strict" -> {

                        if (rayTraceInfo != null && (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos) || rayTraceInfo.sideHit != targetPlace!!.enumFacing)) {
                            return
                        }
                    }
                }
            }
            val f: Float = (targetPlace!!.vec3.xCoord - targetPlace!!.blockPos.x.toDouble()).toFloat()
            val f1: Float = (targetPlace!!.vec3.yCoord - targetPlace!!.blockPos.y.toDouble()).toFloat()
            val f2: Float = (targetPlace!!.vec3.zCoord - targetPlace!!.blockPos.z.toDouble()).toFloat()
            if (f > 1 || f1 > 1 || f2 > 1 || f < 0 || f1 < 0 || f2 < 0) {
                if (invalidPlaceFacingMode. equals ("CancelIt")) {
                    targetPlace = null
                    faceBlock = false
                    return
                } else if (invalidPlaceFacingMode.equals("FixIt")) {
                    val vec = targetPlace!!.vec3
                    val pos = targetPlace!!.blockPos
                    targetPlace!!.vec3 = Vec3(
                        MathHelper.clamp_double(vec.xCoord, pos.x + 0.0, pos.x + 1.0),
                        MathHelper.clamp_double(vec.yCoord, pos.y + 0.0, pos.y + 1.0),
                        MathHelper.clamp_double(vec.zCoord, pos.z + 0.0, pos.z + 1.0)
                    )
                }
            }
        }




        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || mc.thePlayer.heldItem.item !is ItemBlock) {
            if (autoBlockMode.get().equals("Off", ignoreCase = true)) return
            blockSlot = if(sortByHighestAmount.get()){
                InventoryUtils.findAutoBlockBlock()!!
            }else{
                InventoryUtils.findAutoBlockBlock()!!
            }
            if (blockSlot == -1) return
            if (autoBlockMode.get().equals("Spoof", ignoreCase = true)) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            } else {
                mc.thePlayer.inventory.currentItem = blockSlot - 36
            }
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }
        // blacklist check
        if (itemStack != null && itemStack.item != null && itemStack.item is ItemBlock) {
            val block = (itemStack.item as ItemBlock).getBlock()
            if (InventoryUtils.BLOCK_BLACKLIST.contains(block) || !block.isFullCube || itemStack.stackSize <= 0) return
        }
        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer,
                mc.theWorld,
                itemStack,
                targetPlace!!.blockPos,
                targetPlace!!.enumFacing,
                targetPlace!!.vec3
            )
            ||
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.currentEquippedItem, blockData2!!.position, blockData2!!.facing, getVec3(blockData2!!.position, blockData2!!.facing))
        ) {
            tellyPlaceTicks++
            delayTimer.reset()
            delay = if (!placeableDelay.get()) 0L else TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }
            lastPlaceBlock = targetPlace!!.blockPos.add(targetPlace!!.enumFacing.directionVec)
            when (extraClickValue.get().toLowerCase()) {
                "afterplace" -> {
                    // fake click
                    val blockPos = targetPlace!!.blockPos
                    val hitVec = targetPlace!!.vec3
                    afterPlaceC08 = C08PacketPlayerBlockPlacement(targetPlace!!.blockPos, targetPlace!!.enumFacing.index, itemStack, (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(), (hitVec.zCoord - blockPos.z.toDouble()).toFloat())
                }
            }
            if (sprintModeValue.get().equals("PlaceOff", ignoreCase = true)) {
                mc.thePlayer.isSprinting = false

            }
            if (sprintModeValue.get().equals("PlaceOn", ignoreCase = true)) {
                mc.thePlayer.isSprinting = true
            }
            if (swingValue.get()) mc.thePlayer.swingItem() else mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
        // Reset
        targetPlace = null
        if (!stayAutoBlock.get() && blockSlot >= 0 && !autoBlockMode.get()
                .equals("Switch", ignoreCase = true)
        ) mc.netHandler.addToSendQueue(
            C09PacketHeldItemChange(
                mc.thePlayer.inventory.currentItem
            )
        )
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        tellyPlaceTicks = 0
        if (mc.thePlayer == null) return
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SNEAKING
                )
            )
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        faceBlock = false
        if (lastSlot != mc.thePlayer.inventory.currentItem && autoBlockMode.get().equals("switch", ignoreCase = true)) {
            mc.thePlayer.inventory.currentItem = lastSlot
            mc.playerController.updateController()
        }
        if (slot != mc.thePlayer.inventory.currentItem && autoBlockMode.get().equals("spoof", ignoreCase = true)) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    /**
     * Entity movement event
     *
     * @param event
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!safeWalkValue.get() || shouldGoDown || safeWalkMoveValue.get()) return
        if (airSafeValue.get() || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerActivation()) {
            event.cancelEvent()
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
        if (progress >= 1) progress = 1f
        val scaledResolution = ScaledResolution(mc)
        val infoWidth2 = Fonts.minecraftFont.getStringWidth(blocksAmount.toString() + "")
        if (modeDisplay.get()) {
            val speed = LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )
            if (autoJumpValue.get() || smartSpeedValue.get() && speed!!.state) {
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                    Color.WHITE.rgb,
                    false
                )
            } else {
                if (towerActivation()) {
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                        Color.WHITE.rgb,
                        false
                    )
                } else {
                    if (placeModeValue.equals("Pre")) {
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            Color.WHITE.rgb,
                            false
                        )
                    } else if (placeModeValue.equals("Post")) {
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            Color.WHITE.rgb,
                            false
                        )
                    }else{
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            Color.WHITE.rgb,
                            false
                        )
                    }
                }
            }
        }
    }

    private fun getBlockColor(count: Int): Int {
        val f = count.toFloat()
        val f1 = 64f
        val f2 = 0.0f.coerceAtLeast(f.coerceAtMost(f1) / f1)
        return Color.HSBtoRGB(f2 / 3.0f, 1.0f, 1.0f) or -0x1000000
    }

    private fun renderItemStack(stack: ItemStack, x: Int, y: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, x, y)
        mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (markValue.get()){
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) (-sin(yaw)).roundToInt()
            else mc.thePlayer.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt()
            else mc.thePlayer.horizontalFacing.directionVec.z
            for (i in 0 until if (modeValue.get()
                    .equals("Expand", ignoreCase = true) && !towerActivation()
            ) expandLengthValue.get() + 1 else 2) {
                val sameY = sameYValue.get()
                val smartSpeed = smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state
                val autojump = autoJumpValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                val blockPos = BlockPos(
                    mc.thePlayer.posX,
                    if (((sameY && !sameYOnlyMove.get()) || (sameY && sameYOnlyMove.get() && MovementUtils.isMoving())) && (!towerActivation() || smartSpeed  || autojump) && launchY <= mc.thePlayer.posY
                    ) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                    mc.thePlayer.posZ
                )
                val placeInfo = get(blockPos)
                if (isReplaceable(blockPos) && placeInfo != null) {
                    RenderUtils.drawBlockBox(
                        blockPos,
                        Color(redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()),
                        false
                    )
                    break
                }
            }
        }
        if(circleValue.get()){
            val target = mc.thePlayer
            if (circlerenderModeValue.get() != "None") {
                if (target == null) return
                val counter = intArrayOf(0)
                if (circlerenderModeValue.get().equals("Circle", ignoreCase = true)) {
                    GL11.glPushMatrix()
                    GL11.glDisable(3553)
                    GL11.glEnable(2848)
                    GL11.glEnable(2881)
                    GL11.glEnable(2832)
                    GL11.glEnable(3042)
                    GL11.glBlendFunc(770, 771)
                    GL11.glHint(3154, 4354)
                    GL11.glHint(3155, 4354)
                    GL11.glHint(3153, 4354)
                    GL11.glDisable(2929)
                    GL11.glDepthMask(false)
                    GL11.glLineWidth(lineWidthValue.get())
                    GL11.glBegin(3)
                    val x =
                        target.lastTickPosX + (target.posX - target.lastTickPosX) * event!!.partialTicks - mc.renderManager.viewerPosX
                    val y =
                        target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                    val z =
                        target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    for (i in 0..359) {
                        val rainbow = Color(
                            Color.HSBtoRGB(
                                ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f).toFloat(),
                                0.7f,
                                1.0f
                            )
                        )
                        GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                        GL11.glVertex3d(
                            x + radiusValue.get() * cos(i * 6.283185307179586 / 45.0),
                            y,
                            z + radiusValue.get() * sin(i * 6.283185307179586 / 45.0)
                        )
                    }
                    GL11.glEnd()
                    GL11.glDepthMask(true)
                    GL11.glEnable(2929)
                    GL11.glDisable(2848)
                    GL11.glDisable(2881)
                    GL11.glEnable(2832)
                    GL11.glEnable(3553)
                    GL11.glPopMatrix()
                } else {
                    val rad = radiusValue.get()
                    GL11.glPushMatrix()
                    GL11.glDisable(3553)
                    RenderUtils.startDrawing()
                    GL11.glDisable(2929)
                    GL11.glDepthMask(false)
                    GL11.glLineWidth(lineWidthValue.get())
                    GL11.glBegin(3)
                    val x =
                        target.lastTickPosX + (target.posX - target.lastTickPosX) * event!!.partialTicks - mc.renderManager.viewerPosX
                    val y =
                        target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                    val z =
                        target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    for (i in 0..10) {
                        counter[0] = counter[0] + 1
                        val rainbow = Color(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        //final Color rainbow = new Color(Color.HSBtoRGB((float) ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f), 0.7f, 1.0f));
                        GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                        if (rad < 0.8 && rad > 0.0) GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 3.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 3.0)
                        )
                        if (rad < 1.5 && rad > 0.7) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 4.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 4.0)
                            )
                        }
                        if (rad < 2.0 && rad > 1.4) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 5.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 5.0)
                            )
                        }
                        if (rad < 2.4 && rad > 1.9) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 6.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 6.0)
                            )
                        }
                        if (rad < 2.7 && rad > 2.3) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 7.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 7.0)
                            )
                        }
                        if (rad < 6.0 && rad > 2.6) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 8.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 8.0)
                            )
                        }
                        if (rad < 7.0 && rad > 5.9) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 9.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 9.0)
                            )
                        }
                        if (rad < 11.0) if (rad > 6.9) {
                            counter[0] = counter[0] + 1
                            RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                            GL11.glVertex3d(
                                x + rad * cos(i * 6.283185307179586 / 10.0),
                                y,
                                z + rad * sin(i * 6.283185307179586 / 10.0)
                            )
                        }
                    }
                    GL11.glEnd()
                    GL11.glDepthMask(true)
                    GL11.glEnable(2929)
                    RenderUtils.stopDrawing()
                    GL11.glEnable(3553)
                    GL11.glPopMatrix()
                }
            }
        }
    }

    private fun search(blockPos: BlockPos, raycast: Boolean, area: Boolean): Boolean {
        val player = mc.thePlayer ?: return false

        if (!isReplaceable(blockPos)) {
            return false
        }

        val maxReach = mc.playerController.blockReachDistance

        val eyes = player.getEyeVec3()
        var placeRotation: PlaceRotation? = null

        var currPlaceRotation: PlaceRotation?

        for (side in EnumFacing.values()) {
            val neighbor = blockPos.offset(side)

            if (!canBeClicked(neighbor)) {
                continue
            }

            if (!area) {
                currPlaceRotation =
                    findTargetPlace(blockPos, neighbor, Vec3(0.5, 0.5, 0.5), side, eyes, maxReach, raycast)
                        ?: continue

                if (placeRotation == null || RotationUtils.getRotationDifference(
                        currPlaceRotation.rotation, currRotation
                    ) < RotationUtils.getRotationDifference(placeRotation.rotation, currRotation)
                ) {
                    placeRotation = currPlaceRotation
                }
            } else {
                var x = 0.1
                while (x < 0.9) {
                    var y = 0.1
                    while (y < 0.9) {
                        var z = 0.1
                        while (z < 0.9) {
                            currPlaceRotation =
                                findTargetPlace(blockPos, neighbor, Vec3(x, y, z), side, eyes, maxReach, raycast)

                            if (currPlaceRotation == null) {
                                z += 0.1
                                continue
                            }

                            if (placeRotation == null || RotationUtils.getRotationDifference(
                                    currPlaceRotation.rotation, currRotation
                                ) < RotationUtils.getRotationDifference(placeRotation.rotation, currRotation)
                            ) {
                                placeRotation = currPlaceRotation
                            }

                            z += 0.1
                        }
                        y += 0.1
                    }
                    x += 0.1
                }
            }
        }

        placeRotation ?: return false

        if (rotationsValue.get() && (rotationModeValue.equals("Normal") || rotationModeValue.equals("GrimTest") && offGroundTicks >= 3)) {
            lockRotation2 = RotationUtils.limitAngleChange(
                currRotation,
                placeRotation.rotation,
                RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
            )
        }
        if(rotationsValue.get() && rotationModeValue.equals("Telly")){
            lockRotation = if (!towerStatus) {
                Rotation(
                    if (tellyPlaceTicks == 0 && false) mc.thePlayer.rotationYaw + 180 else if (offGroundTicks < 0) mc.thePlayer.rotationYaw else if (false) mc.thePlayer.rotationYaw + 180 else placeRotation.rotation.yaw,
                    placeRotation.rotation.pitch
                )
            } else Rotation(
                if (false) mc.thePlayer.rotationYaw + 180 else placeRotation.rotation.yaw,
                placeRotation.rotation.pitch
            )
        }
        if(rotationsValue.get() && rotationModeValue.equals("Intave")){
            lockRotation = Rotation(mc.thePlayer.rotationYaw + 180, placeRotation.rotation.pitch)
        }

        targetPlace = placeRotation.placeInfo
        return true
    }

    @EventTarget
    private fun setRotation(rotation: Rotation) {
        if (!canrotate) {
            return
        }
        RotationUtils.setTargetRotation(rotation, 0)
    }

    /**
     * For expand scaffold, fixes vector values that should match according to direction vector
     */
    private fun modifyVec(original: Vec3, direction: EnumFacing, pos: Vec3, shouldModify: Boolean): Vec3 {
        if (!shouldModify) {
            return original
        }

        val x = original.xCoord
        val y = original.yCoord
        val z = original.zCoord

        val side = direction.opposite

        return when (side.axis ?: return original) {
            EnumFacing.Axis.Y -> Vec3(x, pos.yCoord + side.directionVec.y.coerceAtLeast(0), z)
            EnumFacing.Axis.X -> Vec3(pos.xCoord + side.directionVec.x.coerceAtLeast(0), y, z)
            EnumFacing.Axis.Z -> Vec3(x, y, pos.zCoord + side.directionVec.z.coerceAtLeast(0))
        }

    }
    private val shouldKeepLaunchPosition
        get() = sameYValue.get()
    private fun findBlock2(expand: Boolean, area: Boolean) {
        val player = mc.thePlayer ?: return

        val blockPosition = if (shouldGoDown) {
            if (player.posY == player.posY.roundToInt() + 0.5) {
                BlockPos(player.posX, player.posY - 0.6, player.posZ)
            } else {
                BlockPos(player.posX, player.posY - 0.6, player.posZ).down()
            }
        } else if (shouldKeepLaunchPosition && launchY <= player.posY) {
            BlockPos(player.posX, launchY - 1.0, player.posZ)
        } else if (player.posY == player.posY.roundToInt() + 0.5) {
            BlockPos(player)
        } else {
            BlockPos(player).down()
        }

        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown, area))) {
            return
        }

        if (expand) {
            val yaw = player.rotationYaw
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(x * i, 0, z * i), false, area)) {
                    return
                }
            }
        } else if (search.get()) {
            for (x in -1..1) {
                if (search(blockPosition.add(x, 0, 0), !shouldGoDown, area)) {
                    return
                }
            }
            for (z in -1..1) {
                if (search(blockPosition.add(0, 0, z), !shouldGoDown, area)) {
                    return
                }
            }
        }
    }

    private fun findTargetPlace(
        pos: BlockPos, offsetPos: BlockPos, vec3: Vec3, side: EnumFacing, eyes: Vec3, maxReach: Float, raycast: Boolean
    ): PlaceRotation? {
        val world = mc.theWorld ?: return null

        val vec = Vec3(pos).add(vec3).addVector(
            side.directionVec.x * vec3.xCoord, side.directionVec.y * vec3.yCoord, side.directionVec.z * vec3.zCoord
        )

        val distance = eyes.distanceTo(vec)

        if (raycast && (distance > maxReach || world.rayTraceBlocks(eyes, vec, false, true, false) != null)) {
            return null
        }

        val diff = vec.subtract(eyes)

        if (side.axis != EnumFacing.Axis.Y) {
            val dist = abs(if (side.axis == EnumFacing.Axis.Z) diff.zCoord else diff.xCoord)

            if (dist < 0) {
                return null
            }
        }

        var rotation = RotationUtils.toRotation(vec, false)
        var rotation2 = RotationUtils.toRotation(vec, true)

        rotation = if (stabilizedRotation.get()) {
            Rotation(round(rotation.yaw / 45f) * 45f, rotation.pitch)
        } else {
            rotation
        }

        if (rotationModeValue.equals("Normal") || (rotationModeValue.equals("GrimTest") && offGroundTicks >= 3)) {
            lockRotation = rotation
        }
        if(rotationModeValue.equals("NormalPredict")){
            lockRotation = rotation2
        }
        if(rotationModeValue.equals("Crazy") && cantellyrotate){
            lockRotation = rotation2
        }

        // If the current rotation already looks at the target block and side, then return right here
        performBlockRaytrace(currRotation, maxReach)?.let { raytrace ->
            if (raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite)) {
                return PlaceRotation(
                    PlaceInfo(
                        raytrace.blockPos,
                        side.opposite,
                        modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
                    ), currRotation
                )
            }
        }

        val raytrace = performBlockRaytrace(rotation, maxReach) ?: return null

        if (raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite)) {
            return PlaceRotation(
                PlaceInfo(
                    raytrace.blockPos, side.opposite, modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
                ), rotation
            )
        }

        return null
    }

    private fun performBlockRaytrace(rotation: Rotation, maxReach: Float): MovingObjectPosition? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val eyes = player.getEyeVec3()
        val rotationVec = RotationUtils.getVectorForRotation(rotation)

        val reach =
            eyes.addVector(rotationVec.xCoord * maxReach, rotationVec.yCoord * maxReach, rotationVec.zCoord * maxReach)

        return world.rayTraceBlocks(eyes, reach, false, false, true)
    }

    private val blocksAmount: Int
        /**
         * @return hotbar blocks amount
         */
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock) {
                    val block = (itemStack.item as ItemBlock).getBlock()
                    if (!InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube) amount += itemStack.stackSize
                }
            }
            return amount
        }
    override val tag: String
        get() = placeModeValue.get()
}

private operator fun Vec3.times(number: Double) = Vec3(xCoord * number, yCoord * number, zCoord * number)

