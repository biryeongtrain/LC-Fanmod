package dev.qf.sidebar;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.TagParser;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.lines.SidebarLine;
import eu.pb4.sidebars.api.lines.SuppliedSidebarLine;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.Text;

public class TestEmployeeSidebar extends Sidebar {
    private int i = 0;
    public TestEmployeeSidebar() {
        super(Priority.HIGH);
        this.setTitle(Text.translatable("sidebar.lc.employee.title"));
            this.setDefaultNumberFormat(BlankNumberFormat.INSTANCE);
        this.setLine(new SuppliedSidebarLine(0, serverPlayerEntity -> {
            if (serverPlayerEntity == null) {
                return Text.of("Empty player");
            }
            return getParsedText("<red>용기</red> : %lobotomy-corp:stat fortitude%", PlaceholderContext.of(serverPlayerEntity));
        }, player -> BlankNumberFormat.INSTANCE));

    }

    private Text getParsedText(String s, PlaceholderContext context) {
        return Placeholders.parseText(parseNode(s), context);
    }

    private TextNode parseNode(String s) {
        return TagParser.QUICK_TEXT_SAFE.parseNode(s);
    }
}
