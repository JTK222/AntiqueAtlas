package hunternif.mc.impl.atlas.mixin;

import hunternif.mc.impl.atlas.api.AtlasAPI;
import hunternif.mc.impl.atlas.mixinhooks.CartographyTableHooks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(targets = "net.minecraft.screen.CartographyTableScreenHandler$4")
class MixinCartographyTableScreenHandlerSlot {

    @Inject(method = "canInsert", at = @At("RETURN"), cancellable = true)
    void antiqueatlas_canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(stack.getItem() == AtlasAPI.getAtlasItem() || info.getReturnValueZ());
    }
}

@Mixin(CartographyTableScreenHandler.class)
public abstract class MixinCartographyTableScreenHandler extends ScreenHandler {
    @Shadow
    CraftingResultInventory resultSlot;

    protected MixinCartographyTableScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "method_17382", at = @At("HEAD"), cancellable = true)
    void antiqueatlas_call(ItemStack atlas, ItemStack map, ItemStack result, World world, BlockPos pos, CallbackInfo info) {
        if (atlas.getItem() == AtlasAPI.getAtlasItem() && map.getItem() == Items.FILLED_MAP) {
            resultSlot.setStack(2, atlas.copy());

            this.sendContentUpdates();

            info.cancel();
        }
    }

    @Inject(method = "transferSlot", at = @At("HEAD"), cancellable = true)
    void antiqueatlas_transferSlot(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> info) {
        if (index >= 0 && index <= 2) return;

        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();

            if (stack.getItem() != AtlasAPI.getAtlasItem()) return;

            boolean result = this.insertItem(stack, 0, 2, false);

            if (!result) {
                info.setReturnValue(ItemStack.EMPTY);
            }
        }
    }

}

@Mixin(targets = "net.minecraft.screen.CartographyTableScreenHandler$5")
class MixinCartographyTableScreenHandlerResultSlot {

    CartographyTableScreenHandler antiqueatlas_handler;

    @Inject(method = "<init>", at = @At("TAIL"))
    void antiqueatlas_init(CartographyTableScreenHandler handler, Inventory inv, int a, int b, int c, ScreenHandlerContext context, CallbackInfo info) {
        antiqueatlas_handler = handler;
    }

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    void antiqueatlas_onTakeItem(PlayerEntity player, ItemStack atlas, CallbackInfoReturnable<ItemStack> info) {
        if (atlas.getItem() == AtlasAPI.getAtlasItem()) {
            ItemStack map = antiqueatlas_handler.slots.get(0).getStack();

            CartographyTableHooks.onTakeItem(player, map, atlas);
        }
    }
}
