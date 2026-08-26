package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

final class VirusEvent {
    private static final int RESPAWN_TICKS = 20 * 20;
    private static final ResourceLocation SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(EventControl.MOD_ID, "virus_speed");
    private static final ResourceLocation DAMAGE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(EventControl.MOD_ID, "virus_damage");

    private static boolean active;
    private static boolean showTimer = true;
    private static int graceDurationSeconds = 60;
    private static int graceTicks;
    private static int respawnTicks = -1;
    private static int deaths;
    private static int miningTicks;
    private static BlockPos miningPosition;
    private static Mob virus;
    private VirusEvent() { }

    static boolean isActive() {
        return active;
    }

    static boolean hasTimer() {
        return active && (graceTicks > 0 || respawnTicks >= 0);
    }

    static int getTimerRemainingTicks() {
        return graceTicks > 0 ? graceTicks : Math.max(0, respawnTicks);
    }

    static int getTimerTotalTicks() {
        return graceTicks > 0 ? graceDurationSeconds * 20 : RESPAWN_TICKS;
    }

    static boolean isTimerVisible() {
        return showTimer;
    }

    static int getGraceDurationSeconds() {
        return graceDurationSeconds;
    }

    static void setTimerVisible(boolean visible) {
        showTimer = visible;
    }

    static void setGraceDurationSeconds(int seconds) {
        graceDurationSeconds = Math.max(10, Math.min(300, seconds));
    }

    static void start(MinecraftServer server) {
        stop(server, false);
        active = true;
        graceTicks = graceDurationSeconds * 20;
        respawnTicks = -1;
        deaths = 0;
        miningTicks = 0;
        virus = null;
        server.getPlayerList().broadcastSystemMessage(
            Component.literal("Le Virus arrive dans " + graceDurationSeconds + " secondes. Preparez-vous !"), false);
    }

    static void tick(MinecraftServer server, List<ServerPlayer> players) {
        if (!active || players.isEmpty()) {
            return;
        }
        if (graceTicks > 0) {
            graceTicks--;
            if (graceTicks == 0) {
                spawn(server, players);
            }
            return;
        }
        if (virus == null || virus.isRemoved() || virus.isDeadOrDying()) {
            if (respawnTicks < 0) {
                if (virus != null) {
                    clearMiningProgress();
                    deaths++;
                }
                respawnTicks = RESPAWN_TICKS;
                virus = null;
                server.getPlayerList().broadcastSystemMessage(
                    Component.literal("Le Virus a ete elimine. Il revient dans 20 secondes..."), false);
            } else if (--respawnTicks <= 0) {
                respawnTicks = -1;
                spawn(server, players);
                return;
            }
            return;
        }
        chase(players);
        mineBlocks();
    }

    static void stop(MinecraftServer server, boolean announce) {
        if (virus != null && !virus.isRemoved()) {
            clearMiningProgress();
            virus.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        active = false;
        graceTicks = 0;
        respawnTicks = -1;
        virus = null;
        if (announce) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("Evenement Virus arrete."), false);
        }
    }

    private static void spawn(MinecraftServer server, List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();
        virus = EntityType.VINDICATOR.create(level);
        if (virus == null) {
            return;
        }
        virus.setCustomName(Component.literal("Virus - Niveau " + getLevel()));
        virus.setCustomNameVisible(true);
        virus.moveTo(target.getX() + 6.0D, target.getY(), target.getZ() + 6.0D,
            target.getYRot(), target.getXRot());
        equipForEvolution();
        level.addFreshEntity(virus);
        server.getPlayerList().broadcastSystemMessage(
            Component.literal("Le Virus est apparu !"), false);
    }

    private static void chase(List<ServerPlayer> players) {
        ServerPlayer target = players.stream().filter(ServerPlayer::isAlive)
            .min((left, right) -> Double.compare(left.distanceToSqr(virus), right.distanceToSqr(virus)))
            .orElse(null);
        if (target == null) {
            return;
        }
        Vec3 direction = target.position().subtract(virus.position());
        double distance = direction.length();
        if (distance > 2.2D) {
            double speed = virus.isInWater() ? 0.06D : 0.10D + (getLevel() - 1) * 0.04D;
            virus.getNavigation().moveTo(target, speed * 10.0D);
            virus.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.FEET, target.position());
        } else if (virus.tickCount % 10 == 0) {
            virus.swing(InteractionHand.MAIN_HAND);
            target.hurt(virus.damageSources().mobAttack(virus), 3.0F + getLevel() * 2.0F);
        }
    }

    private static void mineBlocks() {
        if (virus == null || virus.level().isClientSide()) {
            return;
        }
        BlockPos feet = virus.blockPosition();
        net.minecraft.core.Direction direction = virus.getDirection();
        BlockPos position = new BlockPos(feet.getX() + direction.getStepX(), feet.getY(),
            feet.getZ() + direction.getStepZ());
        BlockState block = virus.level().getBlockState(position);
        if (block.isAir() || block.getDestroySpeed(virus.level(), position) < 0.0F
            || block.is(net.minecraft.world.level.block.Blocks.BEDROCK)) {
            resetMiningProgress();
            return;
        }
        if (!position.equals(miningPosition)) {
            resetMiningProgress();
            miningPosition = position;
        }
        miningTicks++;
        int requiredTicks = new int[]{160, 80, 30, 8}[getLevel() - 1];
        int crackStage = Math.min(9, miningTicks * 10 / requiredTicks);
        if (miningTicks % 6 == 1) {
            virus.swing(InteractionHand.MAIN_HAND);
        }
        virus.level().destroyBlockProgress(virus.getId(), position, crackStage);
        if (miningTicks >= requiredTicks) {
            clearMiningProgress();
            virus.level().destroyBlock(position, true, virus);
            miningTicks = 0;
        }
    }

    private static void resetMiningProgress() {
        clearMiningProgress();
        miningTicks = 0;
    }

    private static void clearMiningProgress() {
        if (virus != null && miningPosition != null && virus.level() instanceof ServerLevel level) {
            level.destroyBlockProgress(virus.getId(), miningPosition, -1);
        }
        miningPosition = null;
    }

    private static void equipForEvolution() {
        int level = getLevel();
        if (level >= 2) {
            virus.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            virus.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            virus.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            virus.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            virus.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        }
        if (level >= 3) {
            virus.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            virus.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            virus.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            virus.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            virus.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        }
        if (level >= 4) {
            virus.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            virus.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
            virus.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            virus.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
            virus.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        }
        AttributeInstance speed = virus.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(new double[]{0.20D, 0.30D, 0.42D, 0.55D}[level - 1]);
        }
        AttributeInstance damage = virus.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(new double[]{4.0D, 7.0D, 11.0D, 16.0D}[level - 1]);
        }
        AttributeInstance health = virus.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(new double[]{20.0D, 32.0D, 48.0D, 64.0D}[level - 1]);
            virus.setHealth((float) health.getValue());
        }
        AttributeInstance armor = virus.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(new double[]{0.0D, 8.0D, 16.0D, 24.0D}[level - 1]);
        }
        AttributeInstance toughness = virus.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null) {
            toughness.setBaseValue(new double[]{0.0D, 2.0D, 8.0D, 12.0D}[level - 1]);
        }
    }

    private static int getLevel() {
        return Math.min(4, deaths + 1);
    }

}
