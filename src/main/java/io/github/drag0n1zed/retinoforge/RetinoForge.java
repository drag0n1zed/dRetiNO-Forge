package io.github.drag0n1zed.retinoforge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod(RetinoForge.MODID)
public class RetinoForge {
    public static final String MODID = "retinoforge";
    // Use the modern LogUtils logger and the requested styling
    public static final Logger LOGGER = LogUtils.getLogger();

    public RetinoForge(FMLJavaModLoadingContext context) {
        // Register listeners on the correct buses
        context.getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        final String MOD_NAME = "dRetiNO-Forge";
        LOGGER.info("[{}] FMLClientSetupEvent received. Checking system...", MOD_NAME);
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            LOGGER.info("{}: Not a macOS system, will do nothing.", MOD_NAME);
            return;
        }
        LOGGER.info("[{}] macOS detected. Checking fml.toml...", MOD_NAME);

        Path fmlConfigPath = FMLPaths.CONFIGDIR.get().resolve("fml.toml");
        if (!Files.exists(fmlConfigPath)) {
            LOGGER.warn("{}: fml.toml not found. It may be generated on next launch.", MOD_NAME);
            return;
        }

        try {
            List<String> lines = Files.readAllLines(fmlConfigPath);
            int settingLineToPatch = -1;
            int commentLineToPatch = -1;
            boolean settingsNeedPatch = false;
            boolean commentsNeedPatch = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.startsWith("#Should we control the window.")) {
                    commentLineToPatch = i;
                    commentsNeedPatch = true;
                }
                if (line.startsWith("earlyWindowControl") && !line.startsWith("#")) {
                    settingLineToPatch = i;
                    if (!line.contains("false")) {
                        settingsNeedPatch = true;
                    }
                    break;
                }
            }

            if (settingsNeedPatch) {
                LOGGER.info("[{}] fml.toml needs patching. Modifying 'earlyWindowControl' and adding comment.", MOD_NAME);

                String originalLine = lines.get(settingLineToPatch);
                String indentation = originalLine.substring(0, originalLine.indexOf("earlyWindowControl"));

                String commentLine = indentation + "# Patched by " + MOD_NAME + " to fix macOS display issues";
                String settingLine = indentation + "earlyWindowControl = false";

                if(commentsNeedPatch) {lines.set(commentLineToPatch, commentLine);}
                lines.set(settingLineToPatch, settingLine);

                Files.write(fmlConfigPath, lines);
                LOGGER.info("[{}] Successfully patched fml.toml. A restart is required for the fix to apply.", MOD_NAME);

            } else if (settingLineToPatch != -1) {
                LOGGER.info("[{}] fml.toml is already correctly configured. No patch needed.", MOD_NAME);

            } else {
                LOGGER.info("[{}] 'earlyWindowControl' not found in fml.toml. Appending setting.", MOD_NAME);
                lines.add("");
                lines.add("# The setting below is patched by  " + MOD_NAME + " to fix macOS display issues");
                lines.add("earlyWindowControl = false");
                Files.write(fmlConfigPath, lines);
                LOGGER.info("[{}] Successfully added setting to fml.toml. A restart is required for the fix to apply.", MOD_NAME);
            }

        } catch (IOException e) {
            LOGGER.error("[{}] Failed to read or write fml.toml!", MOD_NAME, e);
        }
    }
}