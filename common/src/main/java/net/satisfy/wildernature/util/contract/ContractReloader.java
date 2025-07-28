package net.satisfy.wildernature.util.contract;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.satisfy.wildernature.util.BountyBoardTier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ContractReloader implements ResourceManagerReloadListener {
    private static final HashMap<ResourceLocation, Contract> contracts = new HashMap<>();
    public static final HashMap<ResourceLocation, BountyBoardTier> tiers = new HashMap<>();

    public static Contract getContract(ResourceLocation location) {
        var path = location.getPath();
        int index = path.indexOf("/");
        var newLocation = path.substring(index + 1);
        var rl = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), newLocation);

        return contracts.get(rl);
    }

    public static List<ResourceLocation> getContractsOfTier(ResourceLocation tierId) {
        return contracts.keySet().stream().filter(key -> {
            var allTiers = new ArrayList<ResourceLocation>();
            ResourceLocation id = tierId;

            while (true) {
                allTiers.add(id);
                final var idCopy = id;
                var tier = BountyBoardTier.byId(id).orElseThrow(() ->
                        new RuntimeException("Error: Not found tier with id %s".formatted(idCopy)));

                if (tier.previousTier().isPresent()) {
                    id = tier.previousTier().get();
                } else {
                    break;
                }
            }
            return allTiers.contains(contracts.get(key).tier());
        }).toList();
    }

    public static ResourceLocation getRandomContractOfTier(ResourceLocation tier) {
        var contractsOfTier = getContractsOfTier(tier);
        if (contractsOfTier.isEmpty()) {
            throw new RuntimeException("No contracts available for tier: " + tier);
        }
        return contractsOfTier.get(new Random().nextInt(contractsOfTier.size()));
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        contracts.clear();
        var contractResources = manager.listResources("wildernature_contracts", path -> path.getPath().endsWith(".json"));

        contractResources.forEach((resourceLocation, resource) -> {
            try (var open = resource.open()) {
                int index = resourceLocation.getPath().indexOf("/");
                var pathEdits = resourceLocation.getPath().substring(index + 1);

                var jsonString = new String(open.readAllBytes(), StandardCharsets.UTF_8);
                var contract = Contract.CODEC.parse(JsonOps.INSTANCE, new Gson().fromJson(jsonString, JsonElement.class))
                        .getOrThrow(error -> {
                            throw new RuntimeException("Failed to parse contract: " + error);
                        });

                var rl = ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), pathEdits);
                contracts.put(rl, contract);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load contract: " + resourceLocation, e);
            }
        });

        tiers.clear();
        var tierResources = manager.listResources("wildernature_tiers", path -> path.getPath().endsWith(".json"));

        tierResources.forEach((resourceLocation, resource) -> {
            try (var open = resource.open()) {
                var namespace = resourceLocation.getNamespace();
                var pathEdits = resourceLocation.getPath()
                        .substring("wildernature_tiers/".length(), resourceLocation.getPath().length() - ".json".length());

                var jsonString = new String(open.readAllBytes(), StandardCharsets.UTF_8);
                var tier = BountyBoardTier.CODEC.parse(JsonOps.INSTANCE, new Gson().fromJson(jsonString, JsonElement.class))
                        .getOrThrow(error -> {
                            throw new RuntimeException("Failed to parse tier: " + error);
                        });

                var rl = ResourceLocation.fromNamespaceAndPath(namespace, pathEdits);
                tiers.put(rl, tier);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load tier: " + resourceLocation, e);
            }
        });
    }
}
