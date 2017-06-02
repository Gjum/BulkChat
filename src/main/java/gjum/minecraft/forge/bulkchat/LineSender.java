package gjum.minecraft.forge.bulkchat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class LineSender {
    private final Minecraft mc = Minecraft.getMinecraft();

    private InputStream inStream;
    private BufferedReader reader;

    private long lastMsgSendTime = 0;
    private int msgWaitMs = 1000;

    void onTps(float tps) {
        msgWaitMs = (int) (1000 * 20f / tps);
        showChat("Set send interval to " + msgWaitMs + " for tps " + tps);
        // reset sending timer because presumably the user sent a /tps
        lastMsgSendTime = System.currentTimeMillis();
    }

    void setupSending(String filePath) {
        showChat("Reading commands from " + filePath);
        try {
            Path path = Paths.get(filePath);
            inStream = Files.newInputStream(path);
            reader = new BufferedReader(new InputStreamReader(inStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void onTick() {
        if (reader == null)
            return;
        if (lastMsgSendTime + msgWaitMs > System.currentTimeMillis())
            return;

        lastMsgSendTime = System.currentTimeMillis();

        sendNextLine();
    }

    void stopSending() {
        showChat("Done sending");
        try {
            reader.close();
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader = null;
        }
    }

    boolean isIdle() {
        return reader == null;
    }

    private void sendNextLine() {
        String line;
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (line == null) {
                stopSending();
                return;
            }
            if (line.length() > 0) break;
        }

        NetHandlerPlayClient connection = mc.getConnection();
        if (connection == null) {
            BulkChatMod.logger.error("connection == null");
            return;
        }

        // send line to server
        connection.sendPacket(new CPacketChatMessage(line));

        // show what we just sent
        showChat(line);
    }

    private void showChat(String msg) {
    // mc.ingameGUI.setOverlayMessage("Sent command: " + line, false);
        mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("")
                .appendSibling(new TextComponentString("[BulkChat] ").setStyle(new Style().setColor(TextFormatting.DARK_GRAY)))
                .appendSibling(new TextComponentString(msg).setStyle(new Style().setColor(TextFormatting.GRAY))));
    }
}
