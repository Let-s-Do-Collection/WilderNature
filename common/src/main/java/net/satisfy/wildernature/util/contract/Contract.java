package net.satisfy.wildernature.util.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

import java.util.Objects;
import java.util.Optional;

public record Contract(ResourceLocation tier, ItemStack contractStack, ResourceLocation targetPredicate,
                       ResourceLocation damagePredicate, int count, String name, String description,
                       ContractReward reward) {

    public static final Codec<Contract> CODEC = RecordCodecBuilder.create(contractInstance -> contractInstance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(Contract::tier),
            ItemStack.CODEC.fieldOf("contractStack").forGetter(Contract::contractStack),
            ResourceLocation.CODEC.fieldOf("targetPredicate").forGetter(Contract::targetPredicate),
            ResourceLocation.CODEC.fieldOf("damagePredicate").forGetter(Contract::damagePredicate),
            Codec.INT.fieldOf("count").forGetter(Contract::count),
            Codec.STRING.fieldOf("name").forGetter(Contract::name),
            Codec.STRING.fieldOf("description").forGetter(Contract::description),
            ContractReward.CODEC.fieldOf("reward").forGetter(Contract::reward)
    ).apply(contractInstance, Contract::new));

    public static Contract fromId(ResourceLocation contract) {
        return Objects.requireNonNullElse(ContractReloader.getContract(contract), ERROR_CONTRACT);
    }

    public static final Contract ERROR_CONTRACT = new Contract(
            WilderNatureIdentifier.of("tier1"),
            ItemStack.EMPTY,
            WilderNatureIdentifier.of("damage_any"),
            WilderNatureIdentifier.of("_error"),
            0,
            "Deleted contract",
            "Error: This contract was deleted from datapack",
            new ContractReward(0, 0, Optional.empty())
    );
}
