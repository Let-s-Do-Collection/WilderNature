package net.satisfy.wildernature.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.registry.RecipeRegistry;
import net.satisfy.wildernature.registry.TagsRegistry;
import net.satisfy.wildernature.util.Truffling;
import org.jetbrains.annotations.NotNull;

public class TrufflingRecipe extends CustomRecipe {
    private final String group;
    private final NonNullList<Ingredient> ingredients;

    public TrufflingRecipe(String group, NonNullList<Ingredient> ingredients) {
        super(CraftingBookCategory.MISC);
        this.group = group;
        this.ingredients = ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput recipeInput) {
        return NonNullList.withSize(recipeInput.items().size(), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(CraftingInput recipeInput, Level level) {
        boolean hasFoodInput = false;
        NonNullList<Boolean> matches = NonNullList.withSize(this.ingredients.size(), false);
        int itemsCount = 0;

        for (int slotIndex = 0; slotIndex < recipeInput.items().size(); slotIndex++) {
            ItemStack stackInSlot = recipeInput.getItem(slotIndex);
            if (stackInSlot.isEmpty())
                continue;

            itemsCount++;

            if ((stackInSlot.is(TagsRegistry.CAN_BE_TRUFFLED) || stackInSlot.has(DataComponents.FOOD)) && !hasFoodInput && !Truffling.isTruffled(stackInSlot))
                hasFoodInput = true;

            for (int ingredientIndex = 0; ingredientIndex < this.ingredients.size(); ingredientIndex++) {
                if (this.ingredients.get(ingredientIndex).test(stackInSlot) && !matches.get(ingredientIndex))
                    matches.set(ingredientIndex, true);
            }
        }

        return hasFoodInput && matches.stream().allMatch(match -> match) && itemsCount == this.ingredients.size() + 1;
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInput, HolderLookup.Provider provider) {
        for (int index = 0; index < recipeInput.items().size(); index++) {
            ItemStack itemStack = recipeInput.getItem(index);

            if (itemStack.is(TagsRegistry.CAN_BE_TRUFFLED) || itemStack.has(DataComponents.FOOD)) {
                ItemStack resultStack = itemStack.copy();
                resultStack.setCount(1);

                return Truffling.setTruffled(resultStack);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 || height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.TRUFFLING.get();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public static class Serializer implements RecipeSerializer<TrufflingRecipe> {
        public static final StreamCodec<RegistryFriendlyByteBuf, TrufflingRecipe> STREAM_CODEC =
                StreamCodec.of(TrufflingRecipe.Serializer::toNetwork, TrufflingRecipe.Serializer::fromNetwork);
        private static final MapCodec<TrufflingRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapelessRecipe) -> shapelessRecipe.group), Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((list) -> {
            Ingredient[] ingredients = list.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
            if (ingredients.length == 0) {
                return DataResult.error(() -> {
                    return "No ingredients for truffling recipe";
                });
            } else {
                return ingredients.length > 9 ? DataResult.error(() -> {
                    return "Too many ingredients for truffling recipe";
                }) : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
            }
        }, DataResult::success).forGetter((shapelessRecipe) -> {
            return shapelessRecipe.ingredients;
        })).apply(instance, TrufflingRecipe::new));

        public static @NotNull TrufflingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int i = buffer.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);
            nonNullList.replaceAll((ingredient) -> {
                return Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            });
            return new TrufflingRecipe(group, nonNullList);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, TrufflingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.ingredients.size());

            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
        }

        @Override
        public MapCodec<TrufflingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TrufflingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
