package dev.horrorlab.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class KeycardItem extends Item {
    private final int level;

    public KeycardItem(int level, Settings settings) {
        super(settings);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.horrorlab.keycard", level).formatted(Formatting.GRAY));
    }
}
