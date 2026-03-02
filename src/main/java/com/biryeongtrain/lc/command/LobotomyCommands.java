package com.biryeongtrain.lc.command;

import com.biryeongtrain.lc.entity.ContainmentAbnormalityEntity;
import com.biryeongtrain.lc.game.containment.ContainmentChamber;
import com.biryeongtrain.lc.game.containment.ContainmentChamberManager;
import com.biryeongtrain.lc.game.data.AbnormalityCatalog;
import com.biryeongtrain.lc.game.data.AbnormalityDefinition;
import com.biryeongtrain.lc.game.manager.GameManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class LobotomyCommands {
    private static final SuggestionProvider<ServerCommandSource> ACTIVE_VOTE_CANDIDATES = (context, builder) ->
            CommandSource.suggestMatching(GameManager.getInstance().getActiveCandidateIds(), builder);
    private static final SuggestionProvider<ServerCommandSource> ABNORMALITY_IDS = (context, builder) ->
            CommandSource.suggestMatching(AbnormalityCatalog.allIds(), builder);
    private static final SuggestionProvider<ServerCommandSource> BBMODEL_KEYS = (context, builder) ->
            CommandSource.suggestMatching(AbnormalityCatalog.allBbModelKeys(), builder);

    private LobotomyCommands() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lobotomy_corporation")
                .then(CommandManager.literal("start")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            String message = GameManager.getInstance().startNextDayVote(context.getSource().getServer());
                            context.getSource().sendFeedback(() -> Text.literal(message), false);
                            return 1;
                        }))
                .then(CommandManager.literal("day")
                        .then(CommandManager.literal("start")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    String message = GameManager.getInstance().startNextDayVote(context.getSource().getServer());
                                    context.getSource().sendFeedback(() -> Text.literal(message), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("resolve")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    String message = GameManager.getInstance().forceResolveVote(context.getSource().getServer());
                                    context.getSource().sendFeedback(() -> Text.literal(message), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("status")
                                .executes(context -> {
                                    for (Text line : GameManager.getInstance().createStatusLines(context.getSource().getServer())) {
                                        context.getSource().sendFeedback(() -> line, false);
                                    }
                                    return 1;
                                })))
                .then(CommandManager.literal("vote")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .then(CommandManager.argument("abnormality_id", StringArgumentType.word())
                                .suggests(ACTIVE_VOTE_CANDIDATES)
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String abnormalityId = StringArgumentType.getString(context, "abnormality_id");
                                    String message = GameManager.getInstance().castVote(
                                            context.getSource().getServer(),
                                            player,
                                            abnormalityId
                                    );
                                    context.getSource().sendFeedback(() -> Text.literal(message), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("chamber")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("create")
                                .requires(ServerCommandSource::isExecutedByPlayer)
                                .executes(context -> createChamber(context.getSource(), 4, 3))
                                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> createChamber(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "radius"),
                                                3
                                        ))
                                        .then(CommandManager.argument("half_height", IntegerArgumentType.integer(1, 32))
                                                .executes(context -> createChamber(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "radius"),
                                                        IntegerArgumentType.getInteger(context, "half_height")
                                                )))))
                        .then(CommandManager.literal("spawn")
                                .requires(ServerCommandSource::isExecutedByPlayer)
                                .then(CommandManager.argument("abnormality_id", StringArgumentType.word())
                                        .suggests(ABNORMALITY_IDS)
                                        .executes(context -> spawnContainedAbnormality(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "abnormality_id")
                                        )))))
                .then(CommandManager.literal("debug")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("spawn_bbmodel")
                                .requires(ServerCommandSource::isExecutedByPlayer)
                                .then(CommandManager.argument("bbmodel_key", StringArgumentType.word())
                                        .suggests(BBMODEL_KEYS)
                                        .executes(context -> spawnDebugAbnormalityByBbModelKey(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "bbmodel_key")
                                        ))))));
    }

    private static int createChamber(ServerCommandSource source, int radius, int halfHeight) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command must be executed by a player."));
            return 0;
        }

        ServerWorld world = source.getWorld();
        ContainmentChamber chamber = ContainmentChamberManager.create(world, player.getBlockPos(), radius, halfHeight);
        source.sendFeedback(() -> Text.literal(
                "Created containment chamber "
                        + chamber.id()
                        + " at "
                        + chamber.center().toShortString()
                        + " r=" + chamber.horizontalRadius()
                        + " h=" + chamber.verticalHalfHeight()
        ), false);
        return 1;
    }

    private static int spawnContainedAbnormality(ServerCommandSource source, String abnormalityId) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command must be executed by a player."));
            return 0;
        }

        ServerWorld world = source.getWorld();

        AbnormalityDefinition abnormality = AbnormalityCatalog.byId(abnormalityId);
        if (abnormality == null) {
            source.sendError(Text.literal("Unknown abnormality id: " + abnormalityId));
            return 0;
        }

        ContainmentChamber chamber = ContainmentChamberManager.findNearest(world, player.getBlockPos());
        if (chamber == null || !chamber.contains(player.getBlockPos())) {
            chamber = ContainmentChamberManager.create(world, player.getBlockPos(), 4, 3);
        }

        final ContainmentChamber selectedChamber = chamber;
        ContainmentAbnormalityEntity entity = ContainmentAbnormalityEntity.spawn(world, chamber, abnormality);
        source.sendFeedback(() -> Text.literal(
                "Spawned containment entity "
                        + entity.getUuidAsString()
                        + " | abnormality=" + abnormality.id()
                        + " | chamber=" + selectedChamber.id()
        ), false);
        return 1;
    }

    private static int spawnDebugAbnormalityByBbModelKey(ServerCommandSource source, String bbModelKey) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command must be executed by a player."));
            return 0;
        }

        ServerWorld world = source.getWorld();

        AbnormalityDefinition abnormality = AbnormalityCatalog.byBbModelKey(bbModelKey);
        if (abnormality == null) {
            source.sendError(Text.literal("Unknown bbmodel key: " + bbModelKey));
            return 0;
        }

        ContainmentChamber chamber = ContainmentChamberManager.findNearest(world, player.getBlockPos());
        if (chamber == null || !chamber.contains(player.getBlockPos())) {
            chamber = ContainmentChamberManager.create(world, player.getBlockPos(), 4, 3);
        }

        final ContainmentChamber selectedChamber = chamber;
        ContainmentAbnormalityEntity entity = ContainmentAbnormalityEntity.spawn(world, chamber, abnormality);
        source.sendFeedback(() -> Text.literal(
                "Debug spawned containment entity "
                        + entity.getUuidAsString()
                        + " | bbmodel=" + abnormality.bbModelData().key()
                        + " | abnormality=" + abnormality.id()
                        + " | chamber=" + selectedChamber.id()
        ), false);
        return 1;
    }
}
