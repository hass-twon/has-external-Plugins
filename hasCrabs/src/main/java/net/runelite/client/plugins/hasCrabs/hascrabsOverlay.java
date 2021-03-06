package net.runelite.client.plugins.hasCrabs;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
public class hascrabsOverlay extends OverlayPanel {

    private final Client client;
    private final hascrabsPlugin plugin;
    private final hascrabsConfiguration config;

    String timeFormat;
    private String infoStatus = "Starting...";

    @Inject
    public hascrabsOverlay(Client client, hascrabsPlugin plugin, hascrabsConfiguration config) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "crab overlay"));

    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        tableComponent.addRow("Current status:", plugin.status);
        tableComponent.addRow("Time until reset:", String.valueOf(plugin.timeRuns));

        if (plugin.startBot) {
            Duration duration = Duration.between(plugin.totalTimer, Instant.now());
            timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
            tableComponent.addRow("Time running:", formatDuration(duration.toMillis(), timeFormat));
        } else {
            tableComponent.addRow("Time running:", "00:00");
        }

        if (!tableComponent.isEmpty())
        {
            panelComponent.setBackgroundColor(ColorUtil.fromHex("#B3121212")); //Material Dark default
            panelComponent.setPreferredSize(new Dimension(200, 200));
            panelComponent.setBorder(new Rectangle(5, 5, 5, 5));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("hass's crab fighter")
                    .color(ColorUtil.fromHex("#40C4FF"))
                    .build());
            if (plugin.startBot) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Active:")
                        .right(String.valueOf(plugin.startBot))
                        .rightColor(Color.GREEN)
                        .build());
            } else {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Active:")
                        .right(String.valueOf(plugin.startBot))
                        .rightColor(Color.RED)
                        .build());
            }
            panelComponent.getChildren().add(tableComponent);
        }
        return super.render(graphics);
    }
}
