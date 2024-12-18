package dev.anhcraft.oreprocessor.api.event;

import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when the drop is going to be added to the ore storage.<br>
 * There are many possible sources where the drop come from:
 * <ul>
 *   <li>Block-breaking with vanilla drops</li>
 *   <li>Block-breaking with extra drops</li>
 *   <li>Multi block-breaking, e.g. due to an enchantment</li>
 *   <li>Lucky item bonus</li>
 * </ul>
 * Many of the sources above could come from 3rd-party plugin.
 */
public class DropPickupEvent extends PlayerEvent implements Cancellable {
  private static final HandlerList handlers = new HandlerList();

  private final Ore ore;
  private UItemStack item;
  private final Block block;
  private final BlockState brokenState;
  private final TriggerSource triggerSource;
  private boolean cancelled;

  public static DropPickupEvent fromBreakingBlock(@NotNull Player player, @NotNull Ore ore, @NotNull UItemStack item,
                                                  @NotNull Block block, @NotNull BlockState blockState) {
    return new DropPickupEvent(player, ore, item, block, blockState, TriggerSource.BLOCK_BREAKING);
  }

  public static DropPickupEvent fromCollectingDrops(@NotNull Player player, @NotNull Ore ore, @NotNull UItemStack item) {
    return new DropPickupEvent(player, ore, item, null, null, TriggerSource.DROP_COLLECTOR);
  }

  public DropPickupEvent(@NotNull Player player, @NotNull Ore ore, @NotNull UItemStack item,
                         @Nullable Block block, @Nullable BlockState blockState, @NotNull TriggerSource triggerSource) {
    super(player);
    this.ore = ore;
    this.item = item;
    this.block = block;
    this.brokenState = blockState;
    this.triggerSource = triggerSource;
  }

  @NotNull
  public Ore getOre() {
    return ore;
  }

  @NotNull
  public UItemStack getItem() {
    return item;
  }

  public void setItem(@NotNull UItemStack item) {
    this.item = item;
  }

  /**
   * Returns the block broken.<br>
   * This could be {@code null} because a drop could come from other way: multi-block breaking, lucky bonus, custom
   * drops, etc.
   * @return the block that was mined
   */
  @Nullable
  public Block getBlock() {
    return block;
  }

  /**
   * Returns the state of the block during the mining.<br>
   * This could be {@code null} because a drop could come from other way: multi-block breaking, lucky bonus, custom
   * drops, etc.
   * @return the block state when mined
   */
  @Nullable
  public BlockState getBrokenState() {
    return brokenState;
  }

  @NotNull
  public TriggerSource getTriggerSource() {
    return triggerSource;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  public enum TriggerSource {
    /**
     * The event was triggered by breaking block
     */
    BLOCK_BREAKING,

    /**
     * The event was triggered by the drop collector
     */
    DROP_COLLECTOR
  }
}
