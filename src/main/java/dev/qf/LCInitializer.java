package dev.qf;

import dev.qf.canvas.CanvasAssets;
import dev.qf.canvas.CanvasFonts;
import dev.qf.commands.LCCommandInitializer;
import dev.qf.player.Employees;
import dev.qf.player.StatType;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class LCInitializer implements ModInitializer {
    public static final String MOD_ID = "lc";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        LOGGER.info("Hello Fabric world!");
        CommandRegistrationCallback.EVENT.register(
                (dispatcher,
                 access,
                 env) -> LCCommandInitializer.register(dispatcher));

        CanvasFonts.load();
        CanvasAssets.load();
        Placeholders.register(id("stat"), (ctx, arg) -> {
            if (!ctx.hasPlayer()) {
                return PlaceholderResult.invalid("This context requires player");
            }
            if (arg == null) {
                return PlaceholderResult.invalid("No argument provided");
            }

            StatType type = StatType.getByName(arg);
            if (type == null) {
                return PlaceholderResult.invalid("Unknown stat type");
            }
            Employees employees = (Employees) ctx.player();
            int value = employees.lc$getStat(type);

            return PlaceholderResult.value(String.valueOf(value));
        });

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}