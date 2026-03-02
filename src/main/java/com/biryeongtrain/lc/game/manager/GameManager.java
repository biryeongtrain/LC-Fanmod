package com.biryeongtrain.lc.game.manager;

import com.biryeongtrain.lc.game.data.AbnormalityCatalog;
import com.biryeongtrain.lc.game.data.AbnormalityDefinition;
import com.biryeongtrain.lc.game.data.AbnormalityRiskLevel;
import com.biryeongtrain.lc.game.data.DepartmentState;
import com.biryeongtrain.lc.game.data.DepartmentType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class GameManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("lobotomy_corporation");
    private static final GameManager INSTANCE = new GameManager();
    private static final int DEFAULT_CANDIDATE_COUNT = 3;
    private static final int[] CANDIDATE_SLOTS = {11, 13, 15};

    private final EnumMap<DepartmentType, DepartmentState> departments = new EnumMap<>(DepartmentType.class);
    private final Map<UUID, Integer> survivedDays = new HashMap<>();
    private final Map<UUID, Long> firstSeenOrder = new HashMap<>();
    private final Set<String> selectedAbnormalities = new HashSet<>();
    private final Map<UUID, SimpleGui> openVoteDialogs = new HashMap<>();
    private final Random random = new Random();

    private long firstSeenCounter = 0;
    private int currentDay = 0;
    @Nullable
    private VoteSession activeVote = null;

    private GameManager() {
        for (DepartmentType type : DepartmentType.values()) {
            this.departments.put(type, new DepartmentState(type));
        }
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public synchronized void reset() {
        for (DepartmentState state : this.departments.values()) {
            state.setTeamLeader(null);
        }
        for (DepartmentType type : DepartmentType.values()) {
            this.departments.put(type, new DepartmentState(type));
        }
        this.survivedDays.clear();
        this.firstSeenOrder.clear();
        this.selectedAbnormalities.clear();
        this.closeVoteDialogs();
        this.activeVote = null;
        this.currentDay = 0;
        this.firstSeenCounter = 0;
    }

    public synchronized String startNextDayVote(MinecraftServer server) {
        if (this.activeVote != null) {
            return "이미 일차 투표가 진행 중입니다.";
        }

        List<ServerPlayerEntity> voters = this.getEligiblePlayers(server);
        if (voters.isEmpty()) {
            return "투표할 플레이어가 없습니다. 온라인 플레이어가 필요합니다.";
        }

        for (ServerPlayerEntity player : voters) {
            this.ensurePlayerTracked(player);
        }

        List<AbnormalityDefinition> candidates = AbnormalityCatalog.pickCandidates(
                this.selectedAbnormalities,
                DEFAULT_CANDIDATE_COUNT,
                this.random
        );

        if (candidates.isEmpty()) {
            return "선정 가능한 환상체 후보가 없습니다.";
        }

        int day = this.currentDay + 1;
        this.activeVote = new VoteSession(day, candidates, voters);
        this.openVoteDialogs.clear();

        for (ServerPlayerEntity player : voters) {
            this.openVoteDialog(player, this.activeVote);
            player.sendMessage(Text.literal("일차 " + day + " 환상체 투표가 시작되었습니다."), false);
        }

        this.broadcast(server, Text.literal("일차 " + day + " 환상체 투표 시작: /lobotomy_corporation vote <id>"));
        return "일차 " + day + " 투표를 시작했습니다. 대상 플레이어: " + voters.size() + "명";
    }

    public synchronized String castVote(MinecraftServer server, ServerPlayerEntity player, String abnormalityId) {
        if (this.activeVote == null) {
            return "현재 진행 중인 투표가 없습니다.";
        }

        if (!this.activeVote.isEligible(player.getUuid())) {
            return "이번 투표의 대상 플레이어가 아닙니다.";
        }

        String normalizedId = abnormalityId.toLowerCase(Locale.ROOT);
        if (!this.activeVote.hasCandidate(normalizedId)) {
            return "유효하지 않은 환상체 ID입니다: " + abnormalityId;
        }

        this.activeVote.putVote(player.getUuid(), normalizedId);

        int voteCount = this.activeVote.voteCount();
        int totalCount = this.activeVote.eligibleCount();
        this.broadcast(
                server,
                Text.literal(player.getName().getString() + " voted (" + voteCount + "/" + totalCount + ")")
        );

        if (voteCount >= totalCount) {
            return this.resolveVoteInternal(server, false);
        }

        return "투표 완료: " + normalizedId + " (" + voteCount + "/" + totalCount + ")";
    }

    public synchronized String forceResolveVote(MinecraftServer server) {
        if (this.activeVote == null) {
            return "강제 종료할 투표가 없습니다.";
        }
        return this.resolveVoteInternal(server, true);
    }

    public synchronized List<Text> createStatusLines(MinecraftServer server) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal("=== Lobotomy Round Status ===").formatted(Formatting.GOLD));
        lines.add(Text.literal("Current Day: " + this.currentDay));
        lines.add(Text.literal("Selected Abnormalities: " + this.selectedAbnormalities.size()));

        if (this.activeVote != null) {
            lines.add(Text.literal(
                    "Active Vote Day " + this.activeVote.day + " (" + this.activeVote.voteCount() + "/" + this.activeVote.eligibleCount() + ")"
            ).formatted(Formatting.YELLOW));
            String candidates = this.activeVote.candidates.stream()
                    .map(abnormality -> abnormality.id() + " (" + abnormality.name() + ")")
                    .collect(Collectors.joining(", "));
            lines.add(Text.literal("Candidates: " + candidates));
        } else {
            lines.add(Text.literal("Active Vote: none"));
        }

        lines.add(Text.literal("--- Departments ---").formatted(Formatting.AQUA));
        for (DepartmentState state : this.departments.values()) {
            String leaderName = this.resolveLeaderName(server, state.teamLeader());
            String opened = state.isOpened() ? "OPEN" : "LOCKED";
            lines.add(Text.literal(
                    state.type().displayName()
                            + " | " + opened
                            + " | Lv." + state.expansionLevel()
                            + " | Leader: " + leaderName
            ));
        }
        return lines;
    }

    public synchronized Collection<String> getActiveCandidateIds() {
        if (this.activeVote == null) {
            return List.of();
        }
        return this.activeVote.candidatesById.keySet();
    }

    public synchronized void onPlayerJoin(ServerPlayerEntity player) {
        this.ensurePlayerTracked(player);

        if (this.activeVote == null) {
            return;
        }

        UUID uuid = player.getUuid();
        if (!this.activeVote.isEligible(uuid) || this.activeVote.hasVoted(uuid)) {
            return;
        }

        this.openVoteDialog(player, this.activeVote);
        player.sendMessage(Text.literal("진행 중인 환상체 투표 창을 다시 열었습니다."), false);
    }

    public synchronized void onPlayerDeath(MinecraftServer server, ServerPlayerEntity deadPlayer) {
        UUID deadUuid = deadPlayer.getUuid();
        for (DepartmentState state : this.departments.values()) {
            if (!state.isOpened()) {
                continue;
            }
            if (!deadUuid.equals(state.teamLeader())) {
                continue;
            }

            UUID nextLeader = this.selectLeaderCandidate(server, state.type());
            state.setTeamLeader(nextLeader);
            String nextName = this.resolveLeaderName(server, nextLeader);

            this.broadcast(
                    server,
                    Text.literal(state.type().displayName() + " 팀장 사망. 새 팀장: " + nextName)
                            .formatted(Formatting.RED)
            );
            LOGGER.info("Leader changed for department {} after death of {} -> {}",
                    state.type(), deadUuid, nextLeader);
        }
    }

    private String resolveVoteInternal(MinecraftServer server, boolean forced) {
        VoteSession vote = this.activeVote;
        if (vote == null) {
            return "진행 중인 투표가 없습니다.";
        }

        AbnormalityDefinition winner = vote.pickWinner(this.random);
        this.selectedAbnormalities.add(winner.id());
        this.currentDay = vote.day;

        this.incrementSurvivalDays(server);
        String departmentResult = this.applyDepartmentProgress(server, winner);
        String voteSummary = vote.buildVoteSummary();

        this.closeVoteDialogs();
        this.activeVote = null;

        String result = "일차 " + this.currentDay
                + " 환상체 결정: " + winner.name()
                + " [" + winner.id() + "]"
                + (forced ? " (강제 종료)" : "")
                + " | " + departmentResult;

        this.broadcast(server, Text.literal(result).formatted(Formatting.GREEN));
        this.broadcast(server, Text.literal("투표 집계: " + voteSummary).formatted(Formatting.GRAY));
        return result;
    }

    private void openVoteDialog(ServerPlayerEntity player, VoteSession vote) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Day " + vote.day + " - Abnormality Vote").formatted(Formatting.DARK_RED));
        gui.setLockPlayerInventory(true);

        for (int i = 0; i < vote.candidates.size() && i < CANDIDATE_SLOTS.length; i++) {
            AbnormalityDefinition abnormality = vote.candidates.get(i);
            int slot = CANDIDATE_SLOTS[i];

            GuiElementBuilder builder = new GuiElementBuilder(this.itemForRisk(abnormality.riskLevel()))
                    .setName(Text.literal(abnormality.name()).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("ID: " + abnormality.id()).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Risk: " + abnormality.riskLevel()).formatted(Formatting.RED))
                    .addLoreLine(Text.literal("BBModel: " + abnormality.bbModelData().key()).formatted(Formatting.BLUE))
                    .addLoreLine(Text.literal("Compatible: " + this.formatCompatibleDepartments(abnormality)).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("클릭해서 투표").formatted(Formatting.GREEN))
                    .setCallback((index, type, action, clickedGui) -> {
                        String message = this.castVote(player.getServer(), player, abnormality.id());
                        player.sendMessage(Text.literal(message), false);
                        clickedGui.close();
                    });

            gui.setSlot(slot, builder.build());
        }

        gui.open();
        this.openVoteDialogs.put(player.getUuid(), gui);
    }

    private String applyDepartmentProgress(MinecraftServer server, AbnormalityDefinition selectedAbnormality) {
        List<DepartmentState> compatibleDepartments = AbnormalityCatalog.compatibleDepartments(selectedAbnormality).stream()
                .map(this.departments::get)
                .collect(Collectors.toCollection(ArrayList::new));

        List<DepartmentState> compatibleClosed = compatibleDepartments.stream()
                .filter(state -> !state.isOpened())
                .collect(Collectors.toCollection(ArrayList::new));

        if (!compatibleClosed.isEmpty()) {
            DepartmentState opened = compatibleClosed.get(this.random.nextInt(compatibleClosed.size()));
            UUID leader = this.selectLeaderCandidate(server, opened.type());
            opened.open(leader);
            return "부서 개방: "
                    + opened.type().displayName()
                    + " (팀장: " + this.resolveLeaderName(server, leader) + ")";
        }

        List<DepartmentState> expandableCandidates = compatibleDepartments.stream()
                .filter(DepartmentState::isOpened)
                .collect(Collectors.toCollection(ArrayList::new));

        if (expandableCandidates.isEmpty()) {
            expandableCandidates = this.departments.values().stream()
                    .filter(DepartmentState::isOpened)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (expandableCandidates.isEmpty()) {
            return "개방/확장 가능한 부서가 없습니다.";
        }

        DepartmentState expanded = expandableCandidates.get(this.random.nextInt(expandableCandidates.size()));
        expanded.expand();
        return "부서 확장: " + expanded.type().displayName()
                + " -> Lv." + expanded.expansionLevel()
                + " (Risk " + selectedAbnormality.riskLevel() + ")";
    }

    @Nullable
    private UUID selectLeaderCandidate(MinecraftServer server, DepartmentType targetDepartment) {
        List<ServerPlayerEntity> alivePlayers = this.getAlivePlayers(server);
        if (alivePlayers.isEmpty()) {
            return null;
        }

        Set<UUID> occupiedLeaders = this.departments.values().stream()
                .filter(DepartmentState::isOpened)
                .filter(state -> state.type() != targetDepartment)
                .map(DepartmentState::teamLeader)
                .filter(uuid -> uuid != null)
                .collect(Collectors.toSet());

        alivePlayers.sort(Comparator
                .comparingInt((ServerPlayerEntity player) -> this.survivedDays.getOrDefault(player.getUuid(), 0))
                .reversed()
                .thenComparingLong(player -> this.firstSeenOrder.getOrDefault(player.getUuid(), Long.MAX_VALUE))
                .thenComparing(player -> player.getName().getString(), String.CASE_INSENSITIVE_ORDER));

        for (ServerPlayerEntity player : alivePlayers) {
            if (!occupiedLeaders.contains(player.getUuid())) {
                return player.getUuid();
            }
        }

        return alivePlayers.getFirst().getUuid();
    }

    private void incrementSurvivalDays(MinecraftServer server) {
        for (ServerPlayerEntity player : this.getAlivePlayers(server)) {
            this.ensurePlayerTracked(player);
            this.survivedDays.merge(player.getUuid(), 1, Integer::sum);
        }
    }

    private List<ServerPlayerEntity> getEligiblePlayers(MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !player.isSpectator())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ServerPlayerEntity> getAlivePlayers(MinecraftServer server) {
        return this.getEligiblePlayers(server).stream()
                .filter(player -> !player.isDead())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void ensurePlayerTracked(ServerPlayerEntity player) {
        this.firstSeenOrder.computeIfAbsent(player.getUuid(), ignored -> this.firstSeenCounter++);
        this.survivedDays.putIfAbsent(player.getUuid(), 0);
    }

    private void broadcast(MinecraftServer server, Text text) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(text, false);
        }
    }

    private void closeVoteDialogs() {
        for (SimpleGui gui : this.openVoteDialogs.values()) {
            if (gui != null && gui.isOpen()) {
                gui.close();
            }
        }
        this.openVoteDialogs.clear();
    }

    private String resolveLeaderName(MinecraftServer server, @Nullable UUID uuid) {
        if (uuid == null) {
            return "none";
        }
        ServerPlayerEntity onlinePlayer = server.getPlayerManager().getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName().getString();
        }
        return uuid.toString();
    }

    private Item itemForRisk(AbnormalityRiskLevel riskLevel) {
        return switch (riskLevel) {
            case ZAYIN -> Items.WHITE_WOOL;
            case TETH -> Items.LIGHT_GRAY_WOOL;
            case HE -> Items.YELLOW_WOOL;
            case WAW -> Items.ORANGE_WOOL;
            case ALEPH -> Items.RED_WOOL;
        };
    }

    private String formatCompatibleDepartments(AbnormalityDefinition abnormality) {
        return AbnormalityCatalog.compatibleDepartments(abnormality).stream()
                .map(type -> type.displayName() + "(<= " + type.maxRiskLevel() + ")")
                .collect(Collectors.joining(", "));
    }

    private static final class VoteSession {
        private final int day;
        private final List<AbnormalityDefinition> candidates;
        private final Set<UUID> eligibleVoters;
        private final Map<UUID, String> votes = new HashMap<>();
        private final Map<String, AbnormalityDefinition> candidatesById;

        private VoteSession(int day, List<AbnormalityDefinition> candidates, List<ServerPlayerEntity> voters) {
            this.day = day;
            this.candidates = List.copyOf(candidates);
            this.eligibleVoters = voters.stream()
                    .map(ServerPlayerEntity::getUuid)
                    .collect(Collectors.toCollection(HashSet::new));
            this.candidatesById = this.candidates.stream()
                    .collect(Collectors.toUnmodifiableMap(AbnormalityDefinition::id, abnormality -> abnormality));
        }

        private boolean hasCandidate(String id) {
            return this.candidatesById.containsKey(id);
        }

        private boolean isEligible(UUID playerUuid) {
            return this.eligibleVoters.contains(playerUuid);
        }

        private boolean hasVoted(UUID playerUuid) {
            return this.votes.containsKey(playerUuid);
        }

        private void putVote(UUID playerUuid, String abnormalityId) {
            this.votes.put(playerUuid, abnormalityId);
        }

        private int voteCount() {
            return this.votes.size();
        }

        private int eligibleCount() {
            return this.eligibleVoters.size();
        }

        private AbnormalityDefinition pickWinner(Random random) {
            if (this.votes.isEmpty()) {
                return this.candidates.get(random.nextInt(this.candidates.size()));
            }

            Map<String, Long> counts = this.votes.values().stream()
                    .collect(Collectors.groupingBy(voteId -> voteId, Collectors.counting()));

            long maxCount = counts.values().stream().mapToLong(Long::longValue).max().orElse(0L);
            List<String> winners = counts.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxCount)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(ArrayList::new));

            String winnerId = winners.get(random.nextInt(winners.size()));
            return this.candidatesById.get(winnerId);
        }

        private String buildVoteSummary() {
            Map<String, Long> counts = this.votes.values().stream()
                    .collect(Collectors.groupingBy(voteId -> voteId, Collectors.counting()));

            if (counts.isEmpty()) {
                return "투표 없음";
            }

            return this.candidates.stream()
                    .map(candidate -> candidate.id() + "=" + counts.getOrDefault(candidate.id(), 0L))
                    .collect(Collectors.joining(", "));
        }
    }
}
