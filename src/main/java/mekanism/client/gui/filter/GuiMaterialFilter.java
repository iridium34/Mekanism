package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public abstract class GuiMaterialFilter<FILTER extends IMaterialFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTypeFilter<FILTER, TILE, CONTAINER> {

    protected GuiMaterialFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.MATERIAL_FILTER), 43, 6, 0x404040);
        drawString(MekanismLang.STATUS.translate(status), 35, 20, 0x00CD00);
        drawString(MekanismLang.MATERIAL_FILTER_DETAILS.translate(), 35, 32, 0x00CD00);
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    protected void materialMouseClicked() {
        ItemStack stack = minecraft.player.inventory.getItemStack();
        if (!stack.isEmpty() && !InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            if (stack.getItem() instanceof BlockItem) {
                if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                    filter.setMaterialItem(stack.copy());
                    filter.getMaterialItem().setCount(1);
                }
            }
        } else if (stack.isEmpty() && InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            filter.setMaterialItem(ItemStack.EMPTY);
        }
        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
    }
}