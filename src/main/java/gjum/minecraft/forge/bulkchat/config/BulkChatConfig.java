package gjum.minecraft.forge.bulkchat.config;


import gjum.minecraft.forge.bulkchat.BulkChatMod;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.Set;

public class BulkChatConfig {
    public static final String CATEGORY_MAIN = "Main";

    public static final BulkChatConfig instance = new BulkChatConfig();

    public Configuration config;

    public String filePath;
    private Property propFilePath;

    private BulkChatConfig() {
    }

    public void load(File configFile) {
        config = new Configuration(configFile, BulkChatMod.VERSION);
        syncProperties();
        config.load();
        syncProperties();
        syncValues();
    }

    public void afterGuiSave() {
        syncProperties();
        syncValues();
    }

    /**
     * no idea why this has to be called so often, ideally the prop* would stay the same,
     * but it looks like they get disassociated from the config sometimes and setting them no longer has any effect
     */
    private void syncProperties() {
        propFilePath = config.get(CATEGORY_MAIN, "bulk chat file path", "", "Ideally absolute, but relative to .minecraft might work too");
    }

    /**
     * called every time a prop is changed, to apply the new values to the fields and to save the values to the config file
     */
    private void syncValues() {
        filePath = propFilePath.getString();

        if (config.hasChanged()) {
            config.save();
            syncProperties();
            BulkChatMod.logger.info("Saved config.");
        }
    }
}
