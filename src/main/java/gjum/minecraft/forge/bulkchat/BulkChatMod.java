package gjum.minecraft.forge.bulkchat;

import gjum.minecraft.forge.bulkchat.config.BulkChatConfig;
import gjum.minecraft.forge.bulkchat.config.ConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(
        modid = BulkChatMod.MOD_ID,
        name = BulkChatMod.MOD_NAME,
        version = BulkChatMod.VERSION,
        guiFactory = "gjum.minecraft.forge.bulkchat.config.ConfigGuiFactory",
        clientSideOnly = true)
public class BulkChatMod {
    public static final String MOD_ID = "bulkchat";
    public static final String MOD_NAME = "BulkChat";
    public static final String VERSION = "@VERSION@";
    public static final String BUILD_TIME = "@BUILD_TIME@";

    private static final Pattern tpsPattern = Pattern.compile(".*TPS from last 1m, 5m, 15m: ([.0-9]+), [.0-9]+, [.0-9]+.*");

    @Mod.Instance(MOD_ID)
    public static BulkChatMod instance;

    public static Logger logger;

    private final KeyBinding startSending = new KeyBinding("Start/stop sending bulk commands", Keyboard.KEY_NONE, MOD_NAME);
    private final KeyBinding openMenu = new KeyBinding("Open " + MOD_NAME + " menu", Keyboard.KEY_NONE, MOD_NAME);

    private final LineSender lineSender = new LineSender();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        File configFile = event.getSuggestedConfigurationFile();
        logger.info("Loading config from " + configFile);
        BulkChatConfig.instance.load(configFile);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("%s version %s built at %s", MOD_NAME, VERSION, BUILD_TIME);
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(startSending);
        ClientRegistry.registerKeyBinding(openMenu);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(BulkChatMod.MOD_ID)) {
            BulkChatConfig.instance.afterGuiSave();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        Matcher matcher = tpsPattern.matcher(event.getMessage().getFormattedText());
        if (matcher.matches()) {
            float tps = Float.parseFloat(matcher.group(1));
            lineSender.onTps(tps);
        }
        // TODO chat command for sending file
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        lineSender.onTick();
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (startSending.isPressed()) {
            String commandsFilePath = BulkChatConfig.instance.filePath;
            if (commandsFilePath.length() <= 0) {
                // no path set, open config gui so user can set it
                Minecraft.getMinecraft().displayGuiScreen(new ConfigGui(null));
                return; // prevent opening the gui twice
            } else if (lineSender.isIdle()) {
                lineSender.setupSending(commandsFilePath);
            } else {
                lineSender.stopSending();
            }
        }
        if (openMenu.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new ConfigGui(null));
        }
    }

}
