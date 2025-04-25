package dev.qf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.qf.canvas.TestCanvas;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class LCCommandInitializer {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("lc_test")
                .requires(source -> source.hasPermissionLevel(2))
                .build();

        LiteralCommandNode<ServerCommandSource> genCanvas = CommandManager.literal("gen_canvas")
                .executes(TestCanvasCommand::generate)
                .build();


        LiteralCommandNode<ServerCommandSource> destroyCanvas = CommandManager.literal("destroy_canvas")
                .executes(TestCanvasCommand::destroy)
                .build();

        LiteralCommandNode<ServerCommandSource> drawCanvas = CommandManager.literal("draw_canvas")
                        .executes(TestCanvasCommand::draw)
                                .build();


        LiteralCommandNode<ServerCommandSource> toggleSidebar = CommandManager.literal("toggle_sidebar")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> TestCanvasCommand.toggleEmployeeSidebar(ctx, EntityArgumentType.getPlayer(ctx, "player"))))
                                .build();

        LiteralCommandNode<ServerCommandSource> toggleAnimtaion = CommandManager.literal("toggle_anim")
                        .executes(TestCanvasCommand::animate)
                                .build();

        root.addChild(genCanvas);
        root.addChild(destroyCanvas);
        root.addChild(drawCanvas);
        root.addChild(toggleSidebar);
        root.addChild(toggleAnimtaion);

        dispatcher.getRoot().addChild(root);

    }
}
