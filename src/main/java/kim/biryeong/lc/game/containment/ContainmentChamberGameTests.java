package kim.biryeong.lc.game.containment;

import kim.biryeong.lc.game.data.DepartmentType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContainmentChamberGameTests {
    @GameTest
    public void chamberTracksBoundsDepartmentAndEscapeState(TestContext context) {
        ContainmentChamber chamber = new ContainmentChamber(
                java.util.UUID.randomUUID(),
                context.getWorld().getRegistryKey(),
                new BlockPos(0, 64, 0),
                2,
                3,
                DepartmentType.CONTROL
        );

        context.assertTrue(chamber.department() == DepartmentType.CONTROL, Text.literal("Department should be CONTROL"));
        context.assertTrue(chamber.contains(new BlockPos(2, 67, 2)), Text.literal("Position on chamber boundary should be inside"));
        context.assertTrue(!chamber.contains(new BlockPos(3, 64, 0)), Text.literal("Position outside chamber bounds should be excluded"));
        context.assertTrue(!chamber.isAbnormalityEscaping(), Text.literal("Default escape state should be false"));

        chamber.setAbnormalityEscaping(true);
        context.assertTrue(!chamber.isAbnormalityEscaping(), Text.literal("Escape state requires a bound abnormality entity"));
        context.complete();
    }

    @GameTest
    public void managerCreatesAndFindsChambers(TestContext context) {
        ServerWorld world = context.getWorld();
        ContainmentChamberManager.clear();

        ContainmentChamber chamber = ContainmentChamberManager.create(
                world,
                new BlockPos(8, 64, 8),
                2,
                2,
                DepartmentType.SAFETY
        );

        ContainmentChamber containing = ContainmentChamberManager.findContaining(world, new BlockPos(8, 64, 8));
        context.assertTrue(containing != null, Text.literal("Containing chamber should be found"));
        context.assertTrue(chamber.id().equals(containing.id()), Text.literal("Containing chamber should match created chamber"));

        ContainmentChamber nearest = ContainmentChamberManager.findNearest(world, new BlockPos(10, 64, 8));
        context.assertTrue(nearest != null, Text.literal("Nearest chamber should be found"));
        context.assertTrue(chamber.id().equals(nearest.id()), Text.literal("Nearest chamber should match created chamber"));

        ContainmentChamberManager.clear();
        context.complete();
    }
}

