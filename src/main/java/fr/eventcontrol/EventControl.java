package fr.eventcontrol;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mod(EventControl.MOD_ID)
public class EventControl {
    public static final String MOD_ID = "eventcontrol";
    public static final String MOD_NAME = "Event Master";
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<EventMenu>> EVENT_MENU_TYPE =
        MENU_TYPES.register("event_menu", () -> new MenuType<>(EventMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<LavaMenu>> LAVA_MENU_TYPE =
        MENU_TYPES.register("lava_menu", () -> new MenuType<>(LavaMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<DeathSwapMenu>> DEATH_SWAP_MENU_TYPE =
        MENU_TYPES.register("death_swap_menu", () -> new MenuType<>(DeathSwapMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<RandomEffectsMenu>> RANDOM_EFFECTS_MENU_TYPE =
        MENU_TYPES.register("random_effects_menu", () -> new MenuType<>(RandomEffectsMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<GameSpeedMenu>> GAME_SPEED_MENU_TYPE =
        MENU_TYPES.register("game_speed_menu", () -> new MenuType<>(GameSpeedMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<PlayerSelectMenu>> PLAYER_SELECT_MENU_TYPE =
        MENU_TYPES.register("player_select_menu", () -> new MenuType<>(PlayerSelectMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<PlayerInventorySelectMenu>> PLAYER_INVENTORY_SELECT_MENU_TYPE =
        MENU_TYPES.register("player_inventory_select_menu", () -> new MenuType<>(PlayerInventorySelectMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<PlayerSizeMenu>> PLAYER_SIZE_MENU_TYPE =
        MENU_TYPES.register("player_size_menu", () -> new MenuType<>(PlayerSizeMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<PlayerInventoryMenu>> PLAYER_INVENTORY_MENU_TYPE =
        MENU_TYPES.register("player_inventory_menu", () -> new MenuType<>(PlayerInventoryMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<ConfirmMenu>> CONFIRM_MENU_TYPE =
        MENU_TYPES.register("confirm_menu", () -> new MenuType<>(ConfirmMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<VirusMenu>> VIRUS_MENU_TYPE =
        MENU_TYPES.register("virus_menu", () -> new MenuType<>(VirusMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<JumpDeathMenu>> JUMP_DEATH_MENU_TYPE =
        MENU_TYPES.register("jump_death_menu", () -> new MenuType<>(JumpDeathMenu::new, null));
    public static final DeferredHolder<MenuType<?>, MenuType<ToggleSettingsMenu>> TOGGLE_INVENTORY_MENU_TYPE =
        MENU_TYPES.register("toggle_inventory_menu", () -> new MenuType<>((id, inventory) ->
            new ToggleSettingsMenu(id, inventory, ToggleSettingsMenu.INVENTORY), null));
    public static final DeferredHolder<MenuType<?>, MenuType<ToggleSettingsMenu>> TOGGLE_BORDER_MENU_TYPE =
        MENU_TYPES.register("toggle_border_menu", () -> new MenuType<>((id, inventory) ->
            new ToggleSettingsMenu(id, inventory, ToggleSettingsMenu.BORDER), null));
    public static final DeferredHolder<MenuType<?>, MenuType<ToggleSettingsMenu>> TOGGLE_HEALTH_MENU_TYPE =
        MENU_TYPES.register("toggle_health_menu", () -> new MenuType<>((id, inventory) ->
            new ToggleSettingsMenu(id, inventory, ToggleSettingsMenu.HEALTH), null));
    public static final DeferredHolder<MenuType<?>, MenuType<ToggleSettingsMenu>> TOGGLE_SUN_MENU_TYPE =
        MENU_TYPES.register("toggle_sun_menu", () -> new MenuType<>((id, inventory) ->
            new ToggleSettingsMenu(id, inventory, ToggleSettingsMenu.SUN), null));

    private static boolean sharedInventory;
    private static boolean sharedHealth;
    private static boolean sharedHealthDeathInProgress;
    private static boolean sharedHealthDeathTriggered;
    private static ItemStack[] inventorySnapshot;
    private static float sharedHealthValue = 20.0F;
    private static boolean risingLava;
    private static int lavaPreparationTicks;
    private static int lavaRiseTicks;
    private static int lavaPreparationTotalTicks = 20 * 60 * 15;
    private static int lavaRiseIntervalTicks = 20 * 60;
    private static int lavaPreparationMinutes = 15;
    private static int lavaSpeedMultiplier = 1;
    private static final int LAVA_CHUNKS_PER_TICK = 24;
    private static final int LAVA_RADIUS_CHUNKS = LavaLogic.computeSafeLavaRadiusChunks();
    private static final int LAVA_PRIORITY_RADIUS_CHUNKS = 6;
    private static final int LAVA_CAVE_FILL_DEPTH = 8;
    private static int lavaLevel = Integer.MIN_VALUE;
    private static final Queue<LavaWork> lavaWorkQueue = new ArrayDeque<>();
    private static final Set<LavaWork> queuedLavaChunks = new HashSet<>();
    private static boolean growingBorder;
    private static long borderStartGameTime;
    private static double borderCenterX;
    private static double borderCenterZ;
    private static int borderDay;
    private static boolean deathSwap;
    private static boolean deathSwapShowTimer = true;
    private static int deathSwapIntervalTicks = 20 * 60 * 5;
    private static int deathSwapTicks;
    private static final List<UUID> deathSwapPlayers = new ArrayList<>();
    private static boolean randomEffects;
    private static boolean randomEffectsShowTimer = true;
    private static int randomEffectsIntervalTicks = 20 * 30;
    private static int randomEffectsTicks;
    private static UUID randomEffectsTarget;
    private static MobEffectInstance activeRandomEffect;
    private static int gameSpeedMultiplier = 1;
    private static final Map<UUID, Double> originalMobSpeeds = new HashMap<>();
    private static final Map<UUID, Double> originalMobAttackSpeeds = new HashMap<>();
    private static Integer originalRandomTickSpeed;
    private static Float originalTickRate;
    private static final List<String> eventHistory = new ArrayList<>();
    private static final DateTimeFormatter HISTORY_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public EventControl(net.neoforged.bus.api.IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
        modEventBus.addListener(EventControl::registerPayloads);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registrar.playToClient(TimerSyncPayload.TYPE, TimerSyncPayload.STREAM_CODEC, TimerHud::handle);
        } else {
            registrar.playToClient(TimerSyncPayload.TYPE, TimerSyncPayload.STREAM_CODEC,
                (payload, context) -> { });
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("event")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                player.openMenu(new EventMenu.Provider());
                return 1;
            })
            .then(Commands.literal("stop").executes(context -> {
                MinecraftServer server = context.getSource().getServer();
                stopAllEvents(server);
                context.getSource().sendSuccess(() -> Component.literal("Tous les événements sont arrêtés."), true);
                return 1;
            }))
            .then(Commands.literal("status").executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal(statusText()), false);
                return 1;
            }))
            .then(Commands.literal("virus").executes(context -> {
                VirusEvent.start(context.getSource().getServer());
                context.getSource().sendSuccess(() -> Component.literal("Evenement Virus demarre."), true);
                return 1;
            }))
            .then(Commands.literal("jumpdeath").executes(context -> {
                JumpDeathEvent.start(context.getSource().getServer().getPlayerList().getPlayers());
                context.getSource().sendSuccess(() -> Component.literal("Saut interdit: le prochain saut sera mortel."), true);
                return 1;
            }))
            .then(Commands.literal("sun").executes(context -> {
                SunBurnEvent.start(context.getSource().getServer());
                context.getSource().sendSuccess(() -> Component.literal("Soleil dangereux active."), true);
                return 1;
            }))
            .then(Commands.literal("history").executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal(String.join(" | ", eventHistory)), false);
                return 1;
            }))
            .then(Commands.literal("reset").executes(context -> {
                stopAllEvents(context.getSource().getServer());
                eventHistory.clear();
                context.getSource().sendSuccess(() -> Component.literal("État et historique réinitialisés."), true);
                return 1;
            })));
    }

    private static String statusText() {
        return "Lave=" + risingLava + ", Death Swap=" + deathSwap + ", Effets=" + randomEffects
            + ", Bordure=" + growingBorder + ", Virus=" + VirusEvent.isActive()
            + ", Saut mortel=" + JumpDeathEvent.isActive() + ", Soleil=" + SunBurnEvent.isActive()
            + ", Vitesse=x" + gameSpeedMultiplier;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (sharedHealthDeathTriggered && !players.isEmpty() && players.stream().allMatch(ServerPlayer::isAlive)) {
            sharedHealthDeathTriggered = false;
        }
        if (sharedInventory) {
            synchronizeInventory(players);
        }
        if (sharedHealth) {
            synchronizeHealth(players);
        }
        tickRisingLava(server, players);
        tickGrowingBorder(server, players);
        tickDeathSwap(server, players);
        tickRandomEffects(server, players);
        VirusEvent.tick(server, players);
        JumpDeathEvent.tick(players);
        SunBurnEvent.tick(players);
        sendTimerSync(players);
        applyGameSpeed(server);
    }

    private static void sendTimerSync(List<ServerPlayer> players) {
        TimerSyncPayload payload = new TimerSyncPayload(
            risingLava, risingLava ? (lavaPreparationTicks > 0 ? lavaPreparationTicks : lavaRiseTicks) : 0,
            risingLava ? (lavaPreparationTicks > 0 ? lavaPreparationTotalTicks : lavaRiseIntervalTicks) : 1,
            deathSwap, deathSwapTicks, deathSwapIntervalTicks,
            randomEffects, randomEffectsTicks, randomEffectsIntervalTicks,
            VirusEvent.hasTimer(), VirusEvent.getTimerRemainingTicks(), VirusEvent.getTimerTotalTicks());
        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static void applyGameSpeed(MinecraftServer server) {
        if (originalTickRate == null) {
            originalTickRate = server.tickRateManager().tickrate();
        }
        float targetTickRate = originalTickRate * gameSpeedMultiplier;
        if (server.tickRateManager().tickrate() != targetTickRate) {
            server.tickRateManager().setTickRate(targetTickRate);
        }
        if (originalRandomTickSpeed == null) {
            originalRandomTickSpeed = server.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        }
        server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING)
            .set(Math.min(1000, originalRandomTickSpeed * gameSpeedMultiplier), server);
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (!(entity instanceof Mob mob)) {
                    continue;
                }
                AttributeInstance movement = mob.getAttribute(Attributes.MOVEMENT_SPEED);
                if (movement == null) {
                    continue;
                }
                originalMobSpeeds.putIfAbsent(mob.getUUID(), movement.getBaseValue());
                double originalSpeed = originalMobSpeeds.get(mob.getUUID());
                movement.setBaseValue(originalSpeed * gameSpeedMultiplier);
                AttributeInstance attackSpeed = mob.getAttribute(Attributes.ATTACK_SPEED);
                if (attackSpeed != null) {
                    originalMobAttackSpeeds.putIfAbsent(mob.getUUID(), attackSpeed.getBaseValue());
                    attackSpeed.setBaseValue(originalMobAttackSpeeds.get(mob.getUUID()) * gameSpeedMultiplier);
                }
            }
        }
    }

    private static void restoreGameSpeed(MinecraftServer server) {
        if (originalRandomTickSpeed != null) {
            server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(originalRandomTickSpeed, server);
        }
        if (originalTickRate != null) {
            server.tickRateManager().setTickRate(originalTickRate);
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (!(entity instanceof Mob mob)) {
                    continue;
                }
                AttributeInstance movement = mob.getAttribute(Attributes.MOVEMENT_SPEED);
                if (movement != null && originalMobSpeeds.containsKey(mob.getUUID())) {
                    movement.setBaseValue(originalMobSpeeds.get(mob.getUUID()));
                }
                AttributeInstance attackSpeed = mob.getAttribute(Attributes.ATTACK_SPEED);
                if (attackSpeed != null && originalMobAttackSpeeds.containsKey(mob.getUUID())) {
                    attackSpeed.setBaseValue(originalMobAttackSpeeds.get(mob.getUUID()));
                }
            }
        }
        originalMobSpeeds.clear();
        originalMobAttackSpeeds.clear();
        originalRandomTickSpeed = null;
        originalTickRate = null;
    }

    private static void tickRandomEffects(MinecraftServer server, List<ServerPlayer> players) {
        if (!randomEffects || randomEffectsTarget == null) {
            return;
        }
        ServerPlayer target = server.getPlayerList().getPlayer(randomEffectsTarget);
        if (target == null) {
            stopRandomEffects(server);
            return;
        }
        randomEffectsTicks--;
        if (randomEffectsTicks <= 0) {
            randomEffectsTicks = randomEffectsIntervalTicks;
            applyRandomEffect(target);
        }
    }

    private static void applyRandomEffect(ServerPlayer target) {
        MobEffectInstance[] effects = {
            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, randomEffectsIntervalTicks, 0),
            new MobEffectInstance(MobEffects.JUMP, randomEffectsIntervalTicks, 0),
            new MobEffectInstance(MobEffects.DIG_SPEED, randomEffectsIntervalTicks, 0),
            new MobEffectInstance(MobEffects.NIGHT_VISION, randomEffectsIntervalTicks, 0),
            new MobEffectInstance(MobEffects.WEAKNESS, randomEffectsIntervalTicks, 0),
            new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, randomEffectsIntervalTicks, 0),
            new MobEffectInstance(MobEffects.GLOWING, randomEffectsIntervalTicks, 0)
        };
            if (activeRandomEffect != null) {
                target.removeEffect(activeRandomEffect.getEffect());
            }
            activeRandomEffect = effects[ThreadLocalRandom.current().nextInt(effects.length)];
            target.addEffect(activeRandomEffect);
    }

    private static void tickDeathSwap(MinecraftServer server, List<ServerPlayer> players) {
        if (!deathSwap) {
            return;
        }
        if (deathSwapPlayers.size() < 2) {
            stopDeathSwap(server, "Death Swap arrêté : il faut au moins deux joueurs.");
            return;
        }
        deathSwapTicks--;
        if (deathSwapTicks <= 0) {
            deathSwapTicks = deathSwapIntervalTicks;
            swapDeathSwapPositions(server);
        }
    }

    private static void swapDeathSwapPositions(MinecraftServer server) {
        List<ServerPlayer> participants = new ArrayList<>(findDeathSwapPlayers(server));
        if (participants.size() < 2) {
            return;
        }
        Collections.shuffle(participants);
        List<SwapPosition> positions = participants.stream()
            .map(player -> new SwapPosition(player.serverLevel(), player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()))
            .toList();
        for (int index = 0; index < participants.size(); index++) {
            SwapPosition target = positions.get((index + 1) % positions.size());
            participants.get(index).teleportTo(target.level(), target.x(), target.y(), target.z(),
                target.yRot(), target.xRot());
        }
        server.getPlayerList().broadcastSystemMessage(Component.literal("Death Swap : positions échangées !"), false);
        announce(participants, "ÉCHANGE !", "Les positions ont changé");
    }

    private record SwapPosition(ServerLevel level, double x, double y, double z, float yRot, float xRot) { }

    private static List<ServerPlayer> findDeathSwapPlayers(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
            .filter(player -> deathSwapPlayers.contains(player.getUUID()))
            .toList();
    }

    private static void tickGrowingBorder(MinecraftServer server, List<ServerPlayer> players) {
        if (!growingBorder || players.isEmpty()) {
            return;
        }
        long elapsedDays = Math.max(0L, (server.overworld().getGameTime() - borderStartGameTime) / 24000L);
        int targetDay = (int) Math.min(30L, elapsedDays + 1L);
        if (targetDay <= borderDay) {
            return;
        }
        borderDay = targetDay;
        double size = Math.scalb(1.0D, borderDay - 1);
        server.overworld().getWorldBorder().setCenter(borderCenterX, borderCenterZ);
        server.overworld().getWorldBorder().setSize(Math.min(size, 5_999_968.0D));
        server.getPlayerList().broadcastSystemMessage(
            Component.literal("Bordure : jour " + borderDay + " - zone " + formatSize(size) + "x" + formatSize(size)), false);
    }

    private static String formatSize(double size) {
        return String.valueOf((int) Math.min(size, Integer.MAX_VALUE));
    }

    private static void tickRisingLava(MinecraftServer server, List<ServerPlayer> players) {
        if (!risingLava || players.isEmpty()) {
            return;
        }

        if (lavaLevel == Integer.MIN_VALUE) {
            lavaLevel = server.overworld().getMinBuildHeight() + 1;
        }

        if (lavaPreparationTicks > 0) {
            lavaPreparationTicks--;
            if (lavaPreparationTicks == 0) {
                queueLavaLayer(server.overworld(), players);
            }
            return;
        }

        lavaRiseTicks--;
        if (lavaRiseTicks <= 0) {
            lavaRiseTicks = lavaRiseIntervalTicks;
            if (lavaLevel >= server.overworld().getMaxBuildHeight() - 1) {
                setRisingLava(false);
                return;
            }
            lavaLevel++;
            queueLavaLayer(server.overworld(), players);
        }
        processLavaQueue(server.overworld());
    }

    private static void queueLavaLayer(ServerLevel overworld, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            if (player.level() != overworld) {
                continue;
            }
            int centerChunkX = player.blockPosition().getX() >> 4;
            int centerChunkZ = player.blockPosition().getZ() >> 4;
            for (int chunkX = centerChunkX - LAVA_PRIORITY_RADIUS_CHUNKS;
                 chunkX <= centerChunkX + LAVA_PRIORITY_RADIUS_CHUNKS; chunkX++) {
                for (int chunkZ = centerChunkZ - LAVA_PRIORITY_RADIUS_CHUNKS;
                     chunkZ <= centerChunkZ + LAVA_PRIORITY_RADIUS_CHUNKS; chunkZ++) {
                    if (overworld.getChunkSource().hasChunk(chunkX, chunkZ)) {
                        queueChunk(chunkX, chunkZ, lavaLevel);
                    }
                }
            }
            for (int chunkX = centerChunkX - LAVA_RADIUS_CHUNKS;
                 chunkX <= centerChunkX + LAVA_RADIUS_CHUNKS; chunkX++) {
                for (int chunkZ = centerChunkZ - LAVA_RADIUS_CHUNKS;
                     chunkZ <= centerChunkZ + LAVA_RADIUS_CHUNKS; chunkZ++) {
                    if (Math.abs(chunkX - centerChunkX) <= LAVA_PRIORITY_RADIUS_CHUNKS
                        && Math.abs(chunkZ - centerChunkZ) <= LAVA_PRIORITY_RADIUS_CHUNKS) {
                        continue;
                    }
                    if (overworld.getChunkSource().hasChunk(chunkX, chunkZ)) {
                        queueChunk(chunkX, chunkZ, lavaLevel);
                    }
                }
            }
            placeBorder(overworld, centerChunkX, centerChunkZ);
        }
    }

    private static void queueChunk(int chunkX, int chunkZ, int level) {
        LavaWork work = new LavaWork(chunkX, chunkZ, level);
        if (queuedLavaChunks.add(work)) {
            lavaWorkQueue.add(work);
        }
    }

    private static void processLavaQueue(ServerLevel overworld) {
        for (int work = 0; work < LAVA_CHUNKS_PER_TICK && !lavaWorkQueue.isEmpty(); work++) {
            LavaWork lavaWork = lavaWorkQueue.remove();
            queuedLavaChunks.remove(lavaWork);
            int chunkX = lavaWork.chunkX();
            int chunkZ = lavaWork.chunkZ();
            if (!overworld.getChunkSource().hasChunk(chunkX, chunkZ)) {
                continue;
            }
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    BlockPos position = new BlockPos(
                        (chunkX << 4) + localX, lavaWork.level(), (chunkZ << 4) + localZ);
                    BlockState currentState = overworld.getBlockState(position);
                    if (!currentState.is(Blocks.BEDROCK) && !currentState.is(Blocks.LAVA)) {
                        overworld.setBlock(position, Blocks.LAVA.defaultBlockState(), 2);
                        fillCaveBelow(overworld, position);
                    }
                }
            }
        }
    }

    private static void fillCaveBelow(ServerLevel overworld, BlockPos surface) {
        int minY = Math.max(overworld.getMinBuildHeight() + 1, surface.getY() - LAVA_CAVE_FILL_DEPTH);
        for (int y = surface.getY() - 1; y >= minY; y--) {
            BlockPos below = new BlockPos(surface.getX(), y, surface.getZ());
            if (overworld.getBlockState(below).isAir()) {
                overworld.setBlock(below, Blocks.LAVA.defaultBlockState(), 2);
            } else {
                break;
            }
        }
    }

    private static void placeBorder(ServerLevel overworld, int centerChunkX, int centerChunkZ) {
        int minX = (centerChunkX - LAVA_RADIUS_CHUNKS) << 4;
        int maxX = ((centerChunkX + LAVA_RADIUS_CHUNKS + 1) << 4) - 1;
        int minZ = (centerChunkZ - LAVA_RADIUS_CHUNKS) << 4;
        int maxZ = ((centerChunkZ + LAVA_RADIUS_CHUNKS + 1) << 4) - 1;
        for (int coordinate = minX; coordinate <= maxX; coordinate++) {
            placeBarrier(overworld, coordinate, lavaLevel, minZ);
            placeBarrier(overworld, coordinate, lavaLevel, maxZ);
        }
        for (int coordinate = minZ; coordinate <= maxZ; coordinate++) {
            placeBarrier(overworld, minX, lavaLevel, coordinate);
            placeBarrier(overworld, maxX, lavaLevel, coordinate);
        }
    }

    private static void placeBarrier(ServerLevel overworld, int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        if (overworld.getChunkSource().hasChunk(x >> 4, z >> 4)
            && (overworld.getBlockState(position).isAir()
                || overworld.getBlockState(position).is(Blocks.LAVA))) {
            overworld.setBlock(position, Blocks.BARRIER.defaultBlockState(), 2);
        }
    }

    private record LavaWork(int chunkX, int chunkZ, int level) { }

    private static String formatTime(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static void synchronizeInventory(List<ServerPlayer> players) {
        if (players.isEmpty()) {
            inventorySnapshot = null;
            return;
        }
        if (inventorySnapshot == null) {
            inventorySnapshot = copyInventory(players.getFirst());
        } else {
            for (ServerPlayer player : players) {
                if (!sameInventory(player, inventorySnapshot)) {
                    inventorySnapshot = copyInventory(player);
                    break;
                }
            }
        }
        for (ServerPlayer player : players) {
            applyInventory(player, inventorySnapshot);
        }
    }

    private static ItemStack[] copyInventory(ServerPlayer player) {
        ItemStack[] result = new ItemStack[player.getInventory().getContainerSize()];
        for (int slot = 0; slot < result.length; slot++) {
            result[slot] = player.getInventory().getItem(slot).copy();
        }
        return result;
    }

    private static boolean sameInventory(ServerPlayer player, ItemStack[] snapshot) {
        if (player.getInventory().getContainerSize() != snapshot.length) {
            return false;
        }
        for (int slot = 0; slot < snapshot.length; slot++) {
            if (!ItemStack.matches(player.getInventory().getItem(slot), snapshot[slot])) {
                return false;
            }
        }
        return true;
    }

    private static void applyInventory(ServerPlayer player, ItemStack[] snapshot) {
        for (int slot = 0; slot < snapshot.length; slot++) {
            player.getInventory().setItem(slot, snapshot[slot].copy());
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static void synchronizeHealth(List<ServerPlayer> players) {
        List<Float> livingHealthValues = players.stream()
            .filter(ServerPlayer::isAlive)
            .map(ServerPlayer::getHealth)
            .toList();

        if (livingHealthValues.isEmpty()) {
            sharedHealthValue = 20.0F;
            return;
        }

        float nextSharedHealthValue = SharedHealthLogic.calculateSharedHealthValue(livingHealthValues);
        if (nextSharedHealthValue > 0.0F) {
            sharedHealthValue = nextSharedHealthValue;
        }

        for (ServerPlayer player : players) {
            if (!player.isAlive()) {
                continue;
            }
            if (player.getHealth() != sharedHealthValue) {
                player.setHealth(sharedHealthValue);
            }
        }
    }


    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && sharedHealth) {
            player.setHealth(sharedHealthValue);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getServer() != null
            && event.getEntity().getServer().getPlayerList().getPlayers().isEmpty()) {
            inventorySnapshot = null;
            sharedHealthValue = 20.0F;
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer deadPlayer && sharedHealth
            && !sharedHealthDeathInProgress && !sharedHealthDeathTriggered && deadPlayer.getServer() != null) {
            sharedHealthDeathInProgress = true;
            sharedHealthDeathTriggered = true;
            sharedHealthValue = 0.0F;
            for (ServerPlayer player : deadPlayer.getServer().getPlayerList().getPlayers()) {
                if (player != deadPlayer && player.isAlive()) {
                    player.hurt(player.damageSources().generic(), player.getMaxHealth());
                }
            }
            sharedHealthDeathInProgress = false;
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer deadPlayer) || !deathSwap
            || !deathSwapPlayers.contains(deadPlayer.getUUID()) || deadPlayer.getServer() == null) {
            return;
        }
        List<ServerPlayer> winners = findDeathSwapPlayers(deadPlayer.getServer()).stream()
            .filter(player -> player != deadPlayer && !player.isDeadOrDying())
            .toList();
        if (!winners.isEmpty()) {
            String names = winners.stream().map(player -> player.getName().getString()).reduce((a, b) -> a + ", " + b).orElse("");
            showDeathSwapResult(deadPlayer.getServer());
            stopDeathSwap(deadPlayer.getServer(), "Death Swap terminé ! Gagnant(s) : " + names);
        }
    }

    public static boolean isSharedInventory() {
        return sharedInventory;
    }

    public static boolean isSharedHealth() {
        return sharedHealth;
    }

    public static boolean isRisingLava() {
        return risingLava;
    }

    public static boolean isGrowingBorder() {
        return growingBorder;
    }

    public static boolean isDeathSwap() {
        return deathSwap;
    }

    public static void setSharedInventory(boolean enabled) {
        sharedInventory = enabled;
        inventorySnapshot = null;
    }

    public static void setSharedHealth(boolean enabled) {
        sharedHealth = enabled;
        sharedHealthDeathInProgress = false;
        sharedHealthDeathTriggered = false;
        if (!enabled) {
            sharedHealthValue = 20.0F;
            return;
        }
        sharedHealthValue = 20.0F;
    }

    public static void setRisingLava(boolean enabled) {
        if (enabled) {
            startRisingLava(lavaPreparationMinutes, lavaSpeedMultiplier);
        } else {
            risingLava = false;
            lavaPreparationTicks = 0;
            lavaRiseTicks = 0;
            lavaLevel = Integer.MIN_VALUE;
            lavaWorkQueue.clear();
            queuedLavaChunks.clear();
        }
    }

    public static void startRisingLava(int preparationMinutes, int speedMultiplier) {
        risingLava = true;
        lavaPreparationMinutes = preparationMinutes;
        lavaSpeedMultiplier = speedMultiplier;
        lavaPreparationTotalTicks = 20 * 60 * preparationMinutes;
        lavaPreparationTicks = lavaPreparationTotalTicks;
        lavaRiseIntervalTicks = Math.max(1, (20 * 60) / speedMultiplier);
        lavaRiseTicks = lavaRiseIntervalTicks;
        lavaLevel = Integer.MIN_VALUE;
        lavaWorkQueue.clear();
        queuedLavaChunks.clear();
        recordEvent("Lave démarrée (préparation " + preparationMinutes + " min, vitesse x" + speedMultiplier + ")");
    }

    public static void setLavaSpeedMultiplier(int speedMultiplier) {
        lavaSpeedMultiplier = Math.max(1, Math.min(3, speedMultiplier));
        lavaRiseIntervalTicks = Math.max(1, (20 * 60) / lavaSpeedMultiplier);
        if (risingLava) {
            lavaRiseTicks = Math.min(lavaRiseTicks, lavaRiseIntervalTicks);
        }
    }

    public static void startGrowingBorder(MinecraftServer server, ServerPlayer player) {
        growingBorder = true;
        borderStartGameTime = server.overworld().getGameTime();
        borderCenterX = player.getX();
        borderCenterZ = player.getZ();
        borderDay = 1;
        server.overworld().getWorldBorder().setCenter(borderCenterX, borderCenterZ);
        server.overworld().getWorldBorder().setSize(1.0D);
        server.getPlayerList().broadcastSystemMessage(
            Component.literal("Bordure démarrée : jour 1 - zone 1x1"), false);
        recordEvent("Bordure démarrée par " + player.getName().getString());
        announce(server.getPlayerList().getPlayers(), "BORDURE ACTIVE", "La zone évolue chaque jour");
    }

    public static void stopGrowingBorder(MinecraftServer server) {
        growingBorder = false;
        server.overworld().getWorldBorder().setSize(59_999_968.0D);
        server.getPlayerList().broadcastSystemMessage(Component.literal("Bordure arrêtée"), false);
    }

    public static void startDeathSwap(MinecraftServer server, int minutes, boolean showTimer) {
        deathSwapPlayers.clear();
        deathSwapPlayers.addAll(server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList());
        deathSwap = true;
        deathSwapShowTimer = showTimer;
        deathSwapIntervalTicks = 20 * 60 * minutes;
        deathSwapTicks = deathSwapIntervalTicks;
        server.getPlayerList().broadcastSystemMessage(Component.literal(
            "Death Swap démarré : échange toutes les " + minutes + " minutes."), false);
        recordEvent("Death Swap démarré (" + minutes + " min)");
        announce(server.getPlayerList().getPlayers(), "DEATH SWAP", "Premier échange bientôt");
        playSound(server.getPlayerList().getPlayers());
    }

    public static void stopDeathSwap(MinecraftServer server, String message) {
        deathSwap = false;
        deathSwapPlayers.clear();
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    private static void showDeathSwapResult(MinecraftServer server) {
        List<ServerPlayer> activePlayers = findDeathSwapPlayers(server);
        for (ServerPlayer player : activePlayers) {
            boolean winner = !player.isDeadOrDying();
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(
                winner ? "TU AS GAGNÉ !" : "TU AS PERDU !")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(
                winner ? "Félicitations" : "Le Death Swap est terminé")));
        }
        playSound(server.getPlayerList().getPlayers());
    }

    public static boolean isDeathSwapTimerVisible() {
        return deathSwapShowTimer;
    }

    public static void setDeathSwapTimerVisible(boolean visible) {
        deathSwapShowTimer = visible;
    }

    public static boolean isRandomEffectsBarVisible() {
        return randomEffectsShowTimer;
    }

    public static void setRandomEffectsBarVisible(boolean visible) {
        randomEffectsShowTimer = visible;
    }

    public static boolean isRandomEffects() {
        return randomEffects;
    }

    public static int getGameSpeedMultiplier() {
        return gameSpeedMultiplier;
    }

    public static void setGameSpeedMultiplier(int multiplier) {
        gameSpeedMultiplier = Math.max(1, Math.min(50, multiplier));
    }

    public static void resetGameSpeed(MinecraftServer server) {
        if (gameSpeedMultiplier != 1) {
            gameSpeedMultiplier = 1;
        }
        restoreGameSpeed(server);
    }

    public static void startRandomEffects(MinecraftServer server, ServerPlayer target, int seconds) {
        randomEffects = true;
        randomEffectsTarget = target.getUUID();
        randomEffectsIntervalTicks = 20 * seconds;
        randomEffectsTicks = randomEffectsIntervalTicks;
        applyRandomEffect(target);
        server.getPlayerList().broadcastSystemMessage(Component.literal(
            "Effets aléatoires activés pour " + target.getName().getString()), false);
        recordEvent("Effets aléatoires pour " + target.getName().getString() + " (" + seconds + " s)");
        announce(server.getPlayerList().getPlayers(), "EFFET ALÉATOIRE", "Cible : " + target.getName().getString());
    }

    public static void stopRandomEffects(MinecraftServer server) {
        randomEffects = false;
        ServerPlayer target = randomEffectsTarget == null ? null : server.getPlayerList().getPlayer(randomEffectsTarget);
        if (target != null && activeRandomEffect != null) {
            target.removeEffect(activeRandomEffect.getEffect());
        }
        activeRandomEffect = null;
        randomEffectsTarget = null;
        server.getPlayerList().broadcastSystemMessage(Component.literal("Effets aléatoires arrêtés"), false);
    }

    public static void stopAllEvents(MinecraftServer server) {
        sharedInventory = false;
        sharedHealth = false;
        inventorySnapshot = null;
        sharedHealthValue = 20.0F;
        if (risingLava) {
            setRisingLava(false);
        }
        if (deathSwap) {
            stopDeathSwap(server, "Death Swap arrêté.");
        }
        if (randomEffects) {
            stopRandomEffects(server);
        }
        if (growingBorder) {
            stopGrowingBorder(server);
        }
        if (VirusEvent.isActive()) {
            VirusEvent.stop(server, true);
        }
        JumpDeathEvent.stop();
        if (SunBurnEvent.isActive()) {
            SunBurnEvent.stop(server);
        }
        resetGameSpeed(server);
        recordEvent("Tous les événements arrêtés");
    }

    private static void recordEvent(String message) {
        eventHistory.add(LocalDateTime.now().format(HISTORY_TIME) + " - " + message);
        if (eventHistory.size() > 20) {
            eventHistory.removeFirst();
        }
    }

    private static void announce(List<ServerPlayer> players, String title, String subtitle) {
        for (ServerPlayer player : players) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        }
        playSound(players);
    }

    private static void playSound(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0F, 1.0F);
        }
    }


    public static void changePlayerSize(ServerPlayer player, double amount) {
        AttributeInstance scale = player.getAttribute(Attributes.SCALE);
        if (scale == null) {
            return;
        }
        scale.setBaseValue(Math.max(0.25D, Math.min(4.0D, scale.getBaseValue() + amount)));
        player.refreshDimensions();
    }

    public static void resetPlayerSize(ServerPlayer player) {
        AttributeInstance scale = player.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0D);
            player.refreshDimensions();
        }
    }

    public static double getPlayerSize(ServerPlayer player) {
        AttributeInstance scale = player.getAttribute(Attributes.SCALE);
        return scale == null ? 1.0D : scale.getBaseValue();
    }

    public static int getLavaPreparationMinutes() {
        return lavaPreparationMinutes;
    }

    public static int getLavaSpeedMultiplier() {
        return lavaSpeedMultiplier;
    }

    public static ItemStack statusItem(boolean enabled, String label) {
        ItemStack item = new ItemStack(enabled ? Items.LIME_DYE : Items.GRAY_DYE);
        item.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
            Component.literal(label + (enabled ? " : ACTIVÉ" : " : INACTIF"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        return item;
    }

    public static ItemStack namedItem(net.minecraft.world.item.Item item, String label) {
        ItemStack stack = new ItemStack(item);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
            Component.literal(label).withStyle(ChatFormatting.GOLD));
        return stack;
    }

    public static void openMenuLater(ServerPlayer player, MenuProvider provider) {
        if (player.getServer() != null) {
            player.closeContainer();
            player.getServer().execute(() -> {
                if (player.isAlive()) {
                    player.openMenu(provider);
                }
            });
        }
    }
}
