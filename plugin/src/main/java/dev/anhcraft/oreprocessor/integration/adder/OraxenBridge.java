package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.integration.Integration;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanic;
import io.th0rgal.oraxen.utils.drops.Drop;
import io.th0rgal.oraxen.utils.drops.Loot;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class OraxenBridge implements Integration, ItemCustomizer {
    private final OreProcessor plugin;
    private final Enchantment fortuneEnchant;

    public OraxenBridge(OreProcessor plugin) {
        this.plugin = plugin;
        this.fortuneEnchant = EnchantmentWrapper.getByName("fortune");
    }

    // TODO we do not know if Oraxen add custom drops to vanilla blocks lol (look at ItemsAdderBridge for code)

    @Override
    public MaterialClass getMaterialClass() {
        return MaterialClass.ORAXEN;
    }

    @Override
    public Set<UMaterial> getMaterials() {
        return OraxenItems.nameStream().map(UMaterial::fromOraxen).collect(Collectors.toSet());
    }

    @Override
    public ItemStack buildItem(@NotNull UMaterial material) {
        return OraxenItems.getItemById(material.getIdentifier()).build();
    }

    @Override
    public UItemStack identifyItem(@NotNull ItemStack item) {
        String id = OraxenItems.getIdByItem(item);
        return id == null ? null : new UItemStack(UMaterial.fromOraxen(id), item.getAmount());
    }

    @Override
    public UMaterial identifyMaterial(@NotNull ItemStack item) {
        String id = OraxenItems.getIdByItem(item);
        return id == null ? null : UMaterial.fromOraxen(id);
    }

    @Override
    public @Nullable UMaterial identifyMaterial(@NotNull Block block) {
        BlockMechanic blockMechanic = OraxenBlocks.getBlockMechanic(block);
        return blockMechanic == null ? null : UMaterial.fromOraxen(blockMechanic.getItemID());
    }

    // Keep the following code in sync with https://github.com/oraxen/oraxen/blob/master/core/src/main/java/io/th0rgal/oraxen/utils/drops/Drop.java

    private int getFortuneMultiplier(Drop drop, ItemStack itemInHand) {
        int fortuneMultiplier = 1;
        if (itemInHand != null) {
            ItemMeta itemMeta = itemInHand.getItemMeta();
            if (itemMeta != null) {
                if (drop.isFortune() && itemMeta.hasEnchant(fortuneEnchant))
                    fortuneMultiplier += ThreadLocalRandom.current().nextInt(itemMeta.getEnchantLevel(fortuneEnchant));
            }
        }
        return fortuneMultiplier;
    }

    @Override
    public @Nullable List<ItemStack> getLoot(@NotNull Block block, @NotNull ItemStack tool) {
        BlockMechanic blockMechanic = OraxenBlocks.getBlockMechanic(block);
        if (blockMechanic == null || !blockMechanic.getDrop().canDrop(tool)) return null;
        int fortuneMultiplier = getFortuneMultiplier(blockMechanic.getDrop(), tool);
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Loot loot : blockMechanic.getDrop().getLoots()) {
            if (ThreadLocalRandom.current().nextFloat() > loot.getProbability()) continue;
            ItemStack item = loot.getItem(fortuneMultiplier);
            if (item == null) continue;
            itemStacks.add(item);
        }
        return itemStacks;
    }
}
