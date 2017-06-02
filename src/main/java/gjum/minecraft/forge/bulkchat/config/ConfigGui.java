package gjum.minecraft.forge.bulkchat.config;

import gjum.minecraft.forge.bulkchat.BulkChatMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(),
                BulkChatMod.MOD_ID, false, false, "BulkChat config");
    }

    private static List<IConfigElement> getConfigElements() {
        Configuration config = BulkChatConfig.instance.config;
        return new ConfigElement(config.getCategory(BulkChatConfig.CATEGORY_MAIN)).getChildElements();
    }

}
