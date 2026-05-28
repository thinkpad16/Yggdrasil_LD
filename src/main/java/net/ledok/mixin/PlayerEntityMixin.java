package net.ledok.mixin;

import net.ledok.YggdrasilLdMod;
import net.ledok.util.DroppableSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract Inventory getInventory();

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void yggdrasil_partialKeepInventory(CallbackInfo ci) {

        if (!YggdrasilLdMod.CONFIG.partial_inventory_save_enabled) {
            return;
        }

        Player victim = (Player) (Object) this;
        // level() is the new getWorld(), getGameRules() is now part of the level
        if (!victim.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        handleStandardDeathDrops(victim);
        ci.cancel();
    }

    private void handleStandardDeathDrops(Player victim) {
        List<DroppableSlot> finalSlotsToDrop = new ArrayList<>();

        // --- Standard Percentage Drops ---
        double baseDropPercentage = YggdrasilLdMod.CONFIG.keep_inventory_drop_percentage;
        double finalDropPercentage = Math.max(0, Math.min(100, baseDropPercentage));

        if (finalDropPercentage > 0) {
            List<DroppableSlot> mainInvPool = getMainInventorySlots(victim);
            int itemsToDropCount = (int) Math.floor(mainInvPool.size() * (finalDropPercentage / 100.0));
            addRandomSlotsToList(finalSlotsToDrop, mainInvPool, itemsToDropCount);
        }

        dropItemsFromSlots(victim, finalSlotsToDrop);
    }

    private List<DroppableSlot> getMainInventorySlots(Player player) {
        List<DroppableSlot> slots = new ArrayList<>();
        Inventory inventory = player.getInventory();
        // Main inventory (slots 9-35)
        for (int i = 9; i <= 35; i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.items.get(slotIndex), () -> inventory.items.set(slotIndex, ItemStack.EMPTY));
        }
        return slots;
    }

    private void addSlotIfNotEmpty(List<DroppableSlot> list, ItemStack stack, Runnable clearAction) {
        if (!stack.isEmpty()) {
            list.add(new DroppableSlot(stack.copy(), clearAction));
        }
    }

    private void addRandomSlotsToList(List<DroppableSlot> targetList, List<DroppableSlot> sourceSlots, int count) {
        if (count <= 0 || sourceSlots.isEmpty()) {
            return;
        }
        Collections.shuffle(sourceSlots);
        int amountToAdd = Math.min(sourceSlots.size(), count);
        for(int i = 0; i < amountToAdd; i++) {
            targetList.add(sourceSlots.get(i));
        }
    }

    private void dropItemsFromSlots(Player player, List<DroppableSlot> slots) {
        for (DroppableSlot slot : slots) {
            // dropStack is now drop(stack, dropAround, trace)
            player.drop(slot.stack(), true, false);
            // FIX: The accessor for a record component is a method call.
            // This now correctly calls the action to clear the item from its original slot.
            slot.clearSlotAction().run();
        }
    }
}
