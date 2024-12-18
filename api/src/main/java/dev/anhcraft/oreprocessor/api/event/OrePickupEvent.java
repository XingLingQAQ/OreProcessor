package dev.anhcraft.oreprocessor.api.event;

import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when ores are picked up and will be transferred to the storage.<br>
 * This event is also called even when the ore was considered unsuitable, in this case {@link #isCancelled()} will return true.
 * Set {@link Cancellable#setCancelled(boolean)} to bypass this.
 * @deprecated switch to {@link DropPickupEvent}
 */
@Deprecated
public class OrePickupEvent extends DropPickupEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public OrePickupEvent(Player player, Block block, BlockState brokenState, Ore ore, UMaterial feedstock, int amount) {
        super(player, ore, new UItemStack(feedstock, amount), block, brokenState, TriggerSource.BLOCK_BREAKING);
    }

    @NotNull
    public UMaterial getFeedstock() {
        return getItem().material();
    }

    public void setFeedstock(@NotNull UMaterial feedstock) {
        setItem(new UItemStack(feedstock, getItem().amount()));
    }

    public int getAmount() {
        return getItem().amount();
    }

    public void setAmount(int amount) {
        setItem(new UItemStack(getItem().material(), amount));
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
}
