package dev.anhcraft.oreprocessor.handler;

import com.google.common.base.Preconditions;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.event.OreMineEvent;
import dev.anhcraft.oreprocessor.api.event.OrePickupEvent;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public final class ProcessingPlant {
  private final OreProcessor plugin;

  // A set of block mined - used for anti duping mechanism (see below)
  // - Persistence is NOT needed
  // - A cleanup task will run periodically to minimize memory usage
  //private final Set<BlockLocation> minedBlockLocations; // TODO implement

  public ProcessingPlant(OreProcessor plugin) {
    this.plugin = plugin;
   // this.minedBlockLocations = new HashSet<>();
  }

  /**
   * Fires the handler when a block is mined.<br>
   * Cautions:
   * <ul>
   *   <li>The call must be made <b>synchronous</b></li>
   *   <li>Up to <b>one call</b> should be made per break; multiple calls would result in duplication</li>
   *   <li>The block must hold the <b>breaking state</b> (not when it has turned into air)</li>
   * </ul>
   * @param player who mined the block
   * @param block the block mined
   * @return the result
   */
  public Result fireOnMine(Player player, Block block) {
    if (block.isEmpty())
      return Result.PASS;

    // TODO implement anti-dupe

    if (!isEnabled(player))
      return Result.PASS;

    if (!plugin.mainConfig.behaviourSettings.processSilkTouchItems &&
      player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
      return Result.PASS;

    UMaterial material = OreProcessor.getApi().identifyMaterial(block);
    if (material == null) return Result.PASS;
    Ore ore = OreProcessor.getApi().getBlockOre(material);
    if (ore == null) return Result.PASS;

    PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
    OreData oreData = playerData.getOreData(ore.getId());
    boolean isFull = oreData != null && oreData.isFull();
    Bukkit.getPluginManager().callEvent(new OreMineEvent(player, block, ore, material, isFull));

    if (!isFull || plugin.mainConfig.behaviourSettings.enableMiningStatOnFullStorage) {
      StatisticHelper.increaseMiningCount(ore.getId(), playerData);
      StatisticHelper.increaseMiningCount(ore.getId(), OreProcessor.getApi().getServerData());
    }

    if (isFull) {
      if (plugin.mainConfig.behaviourSettings.dropOnFullStorage)
        return Result.PASS;

      plugin.msg(player, plugin.messageConfig.storageFull);
      return Result.DENY;
    }

    return Result.PASS;
  }

  /**
   * Schedules the loot drop collector.<br>
   * This method is preferred for supporting 3rd-party integration that does not provide block loot manipulation.<br>
   * Cautions:
   * <ul>
   *   <li>The call must be made <b>synchronous</b></li>
   *   <li>Multiple calls could be made without resulting in duplication</li>
   *   <li>The state of the block is used to determine the ore</li>
   * </ul>
   * @param player the player
   * @param block the block
   * @return result
   */
  public Result scheduleLootDropCollector(Player player, Block block) {
    if (!isEnabled(player))
      return Result.PASS;

    // Silk touch can exist along with other AE, so it is impossible to ignore here
    // if (!plugin.mainConfig.behaviourSettings.processSilkTouchItems &&
    //        player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;

    UMaterial material = OreProcessor.getApi().identifyMaterial(block);
    if (material == null) return Result.PASS;
    Ore ore = OreProcessor.getApi().getBlockOre(material);
    if (ore == null) return Result.PASS;

    PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
    OreData oreData = playerData.requireOreData(ore.getId());
    // early check for optimization
    if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
      return Result.PASS;

    Location location = block.getLocation();
    int radius = plugin.mainConfig.behaviourSettings.itemPickupRadius;

    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      Collection<Entity> entities = block.getWorld().getNearbyEntities(location, radius, radius, radius);

      boolean has = false;

      for (Entity entity : entities) {
        if (!(entity instanceof Item) || entity.isDead()) continue;
        ItemStack itemStack = ((Item) entity).getItemStack();
        UMaterial feedstock = OreProcessor.getApi().identifyMaterial(itemStack);
        if (feedstock == null) continue;
        int amount = itemStack.getAmount();

        // TODO there is currently no event fired for picking up (through the collector)

        // re-check every time we add a feedstock
        if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
          break; // for remaining, the item entities are not removed, so they would be dropped later

        if (ore.isAcceptableFeedstock(feedstock)) {
          StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
          StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
          oreData.addFeedstock(feedstock, amount);
          entity.remove();
          has = true;
        }
      }

      if (has && !playerData.isTutorialHidden()) {
        for (String msg : plugin.messageConfig.firstTimeTutorial) {
          OreProcessor.getInstance().rawMsg(player, msg);
        }
      }
    }, 3);

    return Result.PASS;
  }

  /**
   * Collects the given loot directly.<br>
   * Any item that is successfully collected would be removed from the given list
   * Cautions:
   * <ul>
   *   <li>The call must be made <b>synchronous</b></li>
   *   <li>Multiple calls could result in duplication</li>
   *   <li>The state of the block is used to determine the ore</li>
   * </ul>
   * @param player the player
   * @param state the state when the block is breaking
   * @param loot the loot (must be modifiable)
   * @return result
   */
  public Result collectLoot(Player player, BlockState state, List<Item> loot) {
    Preconditions.checkArgument(!isUnmodifiable(loot), "Given loot must be modifiable");

    if (!isEnabled(player))
      return Result.PASS;

    Ore ore = OreProcessor.getApi().getBlockOre(UMaterial.of(state.getType()));
    if (ore == null) return Result.PASS;

    PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
    OreData oreData = playerData.requireOreData(ore.getId());
    if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
      return Result.PASS;

    boolean has = false;

    for (Iterator<Item> iterator = loot.iterator(); iterator.hasNext(); ) {
      ItemStack eventItem = iterator.next().getItemStack();
      UMaterial feedstock = OreProcessor.getApi().identifyMaterial(eventItem);
      if (feedstock == null) continue;
      int amount = eventItem.getAmount();

      OrePickupEvent pickupEvent = new OrePickupEvent(player, state.getBlock(), state, ore, feedstock, amount);
      pickupEvent.setCancelled(!ore.isAcceptableFeedstock(feedstock));
      Bukkit.getPluginManager().callEvent(pickupEvent);

      if (!pickupEvent.isCancelled()) {
        feedstock = pickupEvent.getFeedstock();
        amount = pickupEvent.getAmount();
        StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
        StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
        oreData.addFeedstock(feedstock, amount);
        iterator.remove();
        has = true;
      }
    }

    if (has && !playerData.isTutorialHidden()) {
      for (String msg : plugin.messageConfig.firstTimeTutorial) {
        plugin.rawMsg(player, msg);
      }
    }

    return Result.PASS_WITH_IN_PLACE_MODIFY;
  }

  /**
   * Collects the given loot directly.<br>
   * Any item that is successfully collected would be removed from the given list
   * Cautions:
   * <ul>
   *   <li>The call must be made <b>synchronous</b></li>
   *   <li>Multiple calls could result in duplication</li>
   * </ul>
   * @param player the player
   * @param loot the loot (must be modifiable)
   * @return result
   */
  public Result collectLoot(Player player, Collection<? extends ItemStack> loot) {
    Preconditions.checkArgument(!isUnmodifiable(loot), "Given loot must be modifiable");

    if (!isEnabled(player))
      return Result.PASS;

    PlayerData playerData = plugin.playerDataManager.getData(player);
    boolean has = false;

    for (Iterator<? extends ItemStack> it = loot.iterator(); it.hasNext(); ) {
      ItemStack item = it.next();
      UMaterial feedstock = OreProcessor.getApi().identifyMaterial(item);
      if (feedstock == null) continue;
      int amount = item.getAmount();
      Collection<Ore> ores = OreProcessor.getApi().getOresAllowFeedstock(feedstock);
      if (ores.isEmpty()) continue;

      boolean added = false;

      // TODO we should implement a dynamic storing similar to /ore store
      for (Ore ore : ores) {
        OreData oreData = playerData.requireOreData(ore.getId());
        if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
          continue;

        added = true;
        StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
        StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
        oreData.addFeedstock(feedstock, amount);
        it.remove();
        break; // add once only
      }

      if (added) {
        it.remove();
        has = true;
      }
    }

    if (has && !playerData.isTutorialHidden()) {
      for (String msg : plugin.messageConfig.firstTimeTutorial) {
        plugin.rawMsg(player, msg);
      }
    }

    return Result.PASS_WITH_IN_PLACE_MODIFY;
  }

  private static boolean isUnmodifiable(Collection<?> collection) {
    try {
      collection.addAll(Collections.emptyList());
      return false;
    } catch (UnsupportedOperationException UnsupportedOperationException) {
      return true;
    }
  }

  private boolean isEnabled(Player player) {
    if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
      return false;

    return plugin.mainConfig.whitelistWorlds == null ||
      plugin.mainConfig.whitelistWorlds.isEmpty() ||
      plugin.mainConfig.whitelistWorlds.contains(player.getWorld().getName());
  }

  public enum Result {
    PASS,
    PASS_WITH_IN_PLACE_MODIFY,
    DENY
  }
}
