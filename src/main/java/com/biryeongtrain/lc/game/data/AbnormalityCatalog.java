package com.biryeongtrain.lc.game.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class AbnormalityCatalog {
    private static final List<AbnormalityDefinition> ALL = List.of(
            new AbnormalityDefinition(
                    "one_sin",
                    "One Sin and Hundreds of Good Deeds",
                    AbnormalityRiskLevel.ZAYIN,
                    new BbModelData("one_sin", "one_sin")
            ),
            new AbnormalityDefinition(
                    "scorched_girl",
                    "Scorched Girl",
                    AbnormalityRiskLevel.TETH,
                    new BbModelData("scorched_girl", "scorched_girl")
            ),
            new AbnormalityDefinition(
                    "punishing_bird",
                    "Punishing Bird",
                    AbnormalityRiskLevel.TETH,
                    new BbModelData("punishing_bird", "punishing_bird")
            ),
            new AbnormalityDefinition(
                    "forsaken_murderer",
                    "Forsaken Murderer",
                    AbnormalityRiskLevel.TETH,
                    new BbModelData("forsaken_murderer", "forsaken_murderer")
            ),
            new AbnormalityDefinition(
                    "fairy_festival",
                    "Fairy Festival",
                    AbnormalityRiskLevel.HE,
                    new BbModelData("fairy_festival", "fairy_festival")
            ),
            new AbnormalityDefinition(
                    "old_lady",
                    "Old Lady",
                    AbnormalityRiskLevel.ZAYIN,
                    new BbModelData("old_lady", "old_lady")
            ),
            new AbnormalityDefinition(
                    "fragment_of_universe",
                    "Fragment of the Universe",
                    AbnormalityRiskLevel.HE,
                    new BbModelData("fragment_of_universe", "fragment_of_universe")
            ),
            new AbnormalityDefinition(
                    "spider_bud",
                    "Spider Bud",
                    AbnormalityRiskLevel.HE,
                    new BbModelData("spider_bud", "spider_bud")
            ),
            new AbnormalityDefinition(
                    "queen_bee",
                    "Queen Bee",
                    AbnormalityRiskLevel.WAW,
                    new BbModelData("queen_bee", "queen_bee")
            ),
            new AbnormalityDefinition(
                    "funeral_of_dead_butterflies",
                    "Funeral of the Dead Butterflies",
                    AbnormalityRiskLevel.WAW,
                    new BbModelData("funeral_of_dead_butterflies", "funeral_of_dead_butterflies")
            ),
            new AbnormalityDefinition(
                    "laetitia",
                    "Laetitia",
                    AbnormalityRiskLevel.HE,
                    new BbModelData("laetitia", "laetitia")
            ),
            new AbnormalityDefinition(
                    "silent_orchestra",
                    "Silent Orchestra",
                    AbnormalityRiskLevel.ALEPH,
                    new BbModelData("silent_orchestra", "silent_orchestra")
            )
    );

    private static final Map<String, AbnormalityDefinition> BY_ID = ALL.stream()
            .collect(Collectors.toUnmodifiableMap(AbnormalityDefinition::id, value -> value));
    private static final Map<String, AbnormalityDefinition> BY_BBMODEL_KEY = createBbModelKeyMap();

    private AbnormalityCatalog() {
    }

    public static List<AbnormalityDefinition> pickCandidates(Set<String> alreadySelected, int count, Random random) {
        if (count <= 0) {
            return List.of();
        }

        List<AbnormalityDefinition> available = ALL.stream()
                .filter(abnormality -> !alreadySelected.contains(abnormality.id()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (available.isEmpty()) {
            available = new ArrayList<>(ALL);
        }

        Collections.shuffle(available, random);
        int limit = Math.min(count, available.size());
        return List.copyOf(available.subList(0, limit));
    }

    public static AbnormalityDefinition byId(String id) {
        if (id == null) {
            return null;
        }
        return BY_ID.get(id.toLowerCase(Locale.ROOT));
    }

    public static AbnormalityDefinition byBbModelKey(String bbModelKey) {
        if (bbModelKey == null) {
            return null;
        }
        return BY_BBMODEL_KEY.get(bbModelKey.toLowerCase(Locale.ROOT));
    }

    public static AbnormalityDefinition fallback() {
        return ALL.getFirst();
    }

    public static Set<String> allIds() {
        return BY_ID.keySet();
    }

    public static List<AbnormalityDefinition> all() {
        return ALL;
    }

    public static Set<String> allBbModelKeys() {
        return BY_BBMODEL_KEY.keySet();
    }

    public static List<DepartmentType> compatibleDepartments(AbnormalityDefinition abnormality) {
        return Arrays.stream(DepartmentType.values())
                .filter(department -> department.accepts(abnormality.riskLevel()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Map<String, AbnormalityDefinition> createBbModelKeyMap() {
        Map<String, AbnormalityDefinition> mappings = new HashMap<>();
        for (AbnormalityDefinition abnormality : ALL) {
            String bbModelKey = abnormality.bbModelData().key().toLowerCase(Locale.ROOT);
            mappings.putIfAbsent(bbModelKey, abnormality);
        }
        return Map.copyOf(mappings);
    }
}
