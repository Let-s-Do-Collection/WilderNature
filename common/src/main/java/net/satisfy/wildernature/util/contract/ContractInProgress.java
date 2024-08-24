package net.satisfy.wildernature.util.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.satisfy.wildernature.client.gui.handlers.BountyBlockScreenHandler;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ContractInProgress {
    public static final HashMap<UUID,ContractInProgress> progressPerPlayer = new HashMap<>();
    private static ResourceLocation contract(ContractInProgress o) {
        return ((ContractInProgress) o).s_contract;
    }
    public final ResourceLocation s_contract;

    private static long id(Object o) {
        return ((ContractInProgress)o).boardId;
    }
    public final long boardId;

    public boolean isFinished() {
        return count <= 0;
    }

    public int count;
    private static int count(Object o) {
        return ((ContractInProgress)o).count;
    }
    public static final Codec<ContractInProgress> SERVER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("contract").forGetter(ContractInProgress::contract),
            Codec.INT.fieldOf("count").forGetter(ContractInProgress::count),
            Codec.LONG.fieldOf("id").forGetter(ContractInProgress::id)
    ).apply(instance, ContractInProgress::new));

    public ContractInProgress(ResourceLocation contract, int count, long id) {
        this.s_contract = contract;
        this.boardId = id;
        this.count = count;
    }

    public static EventResult onEntityDeath(LivingEntity livingEntity, DamageSource damageSource) {
        var attacker = damageSource.getEntity();
        if(attacker instanceof ServerPlayer serverPlayer){
            var contract = (progressPerPlayer.get(serverPlayer.getUUID()));
            if(contract!=null){
                contract.onEntityDeath(livingEntity,damageSource,serverPlayer);
            }
        }
        return EventResult.pass();
    }

    public void onEntityDeath(LivingEntity livingEntity, DamageSource damageSource, Player sourcePlayer){
        Contract contract = this.s_getContract();
        try {
            if (isFinished())
                return;
            var entityLootParams = new LootParams.Builder((ServerLevel) livingEntity.level())
                    .withParameter(LootContextParams.ORIGIN, livingEntity.getPosition(0))
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, livingEntity)
                    .create(LootContextParamSets.COMMAND);
            var entityLootContext = new LootContext.Builder(entityLootParams).create(null);
            var entityCondition = (LootItemCondition) Objects.requireNonNull(livingEntity.getServer()).getLootData().getElement(LootDataType.PREDICATE, contract.targetPredicate());
            var entityResult = entityCondition != null && entityCondition.test(entityLootContext);

            var damageLootParams = new LootParams.Builder((ServerLevel) livingEntity.level())
                    .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                    .withParameter(LootContextParams.ORIGIN, livingEntity.getPosition(0))
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                    .withParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                    .withParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity())
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, sourcePlayer)
                    .create(LootContextParamSets.ENTITY);

            var damageLootContext = new LootContext.Builder(damageLootParams).create(null);
            var damageCondition = (LootItemCondition) livingEntity.getServer().getLootData().getElement(LootDataType.PREDICATE, contract.damagePredicate());
            var damageResult = damageCondition == null || damageCondition.test(damageLootContext);
            if (damageCondition == null) {
                sourcePlayer.sendSystemMessage(Component.literal("Data error: contract %s has wrong damage predicate id (%s). Please check if name is correct".formatted(this.s_contract, contract.damagePredicate())));
                return;
            }

            var predicateOrId = (entityResult || BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType()).equals(contract.targetPredicate()));
            if (Platform.isDevelopmentEnvironment()) {
                sourcePlayer.sendSystemMessage(Component.literal("_entity: %b; damage: %b".formatted(predicateOrId, damageResult)));
            }
            if (predicateOrId && damageResult) {
                count--;
            }
            if (Platform.isDevelopmentEnvironment()) {
                sourcePlayer.sendSystemMessage(Component.literal("_Entities left: " + count));
            }
            if (count <= 0) {
                if (Platform.isDevelopmentEnvironment()) {
                    sourcePlayer.sendSystemMessage(Component.literal("_Finished contract \"%s\"".formatted(contract.name(), contract.description())));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Objects.requireNonNull(sourcePlayer.getServer()).halt(false);
        }
    }

    @Override
    public String toString() {
        return "[%d, %s, %d]".formatted(this.boardId,this.s_contract.toString(),this.count);
    }

    public void onFinish(ServerPlayer sourcePlayer) {
        var bountyHandler = ((BountyBlockScreenHandler)sourcePlayer.containerMenu);
        if(bountyHandler.s_targetEntity.boardId == boardId){
            bountyHandler.s_targetEntity.addXp(this.s_getContract().reward().blockExpReward());
        }
        else{
            if(Platform.isDevelopmentEnvironment()){
                sourcePlayer.sendSystemMessage(Component.literal("_not given xp to block because contract taken in other board"));
            }
        }
        if(s_getContract().reward().playerRewardLoot().isEmpty()){
            return;
        }

        Objects.requireNonNull(sourcePlayer.level()
                        .getServer())
                .getLootData()
                .getLootTable(
                        s_getContract()
                                .reward()
                                .playerRewardLoot().get()
                )
                .getRandomItems(
                        new LootParams.Builder((ServerLevel) sourcePlayer.level())
                                .withParameter(
                                        LootContextParams.THIS_ENTITY,
                                        sourcePlayer)
                                .withParameter(
                                        LootContextParams.ORIGIN,
                                        sourcePlayer.getPosition(0))
                                .create(LootContextParamSets.ADVANCEMENT_REWARD),
                        sourcePlayer.getLootTableSeed(),
                        sourcePlayer::spawnAtLocation
                );
    }

    public Contract s_getContract() {
        return Contract.fromId(this.s_contract);
    }
}
