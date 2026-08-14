package com.minecraftarchipelago.dashboard;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelagoClient;
import com.minecraftarchipelago.dashboard.tabs.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class ArchipelagoDashboardScreen extends Screen {
    public static final int WINDOW_WIDTH = 420;
    public static final int WINDOW_HEIGHT = 280;

    private static final int HEADER_HEIGHT = 28;
    private static final int TAB_BAR_HEIGHT = 24;
    private static final int TAB_WIDTH = 68;

    private enum Page {
        OVERVIEW("Overview"),
        GOALS("Goals"),
        PROGRESS("Progress"),
        UNLOCKS("Unlocks"),
        ITEMS("Items"),
        SETTINGS("Settings");

        private final String label;

        Page(String label) {
            this.label = label;
        }

        private Text text() {
            return Text.literal(label);
        }
    }

    private final DashboardTab overviewTab = new OverviewTab();
    private final DashboardTab goalsTab = new GoalTab();
    private final DashboardTab progressTab = new ProgressTab();
    private final DashboardTab unlocksTab = new UnlocksTab();
    private final DashboardTab itemsTab = new ItemsTab();
    private final DashboardTab settingsTab = new SettingsTab();

    private int left;
    private int top;
    private boolean initializedConnected;
    private Page selectedPage = Page.OVERVIEW;

    private TextFieldWidget hostField;
    private TextFieldWidget portField;
    private TextFieldWidget slotField;
    private TextFieldWidget passwordField;
    private String connectionMessage = "";

    public ArchipelagoDashboardScreen() {
        super(Text.literal("Archipelago Dashboard"));
    }

    @Override
    protected void init() {
        left = (width - WINDOW_WIDTH) / 2;
        top = (height - WINDOW_HEIGHT) / 2;
        initializedConnected = APSession.client().isConnected();
        selectedPage = loadSelectedPage();

        addDrawableChild(ButtonWidget.builder(
                Text.literal("×"),
                button -> close()
        ).dimensions(left + WINDOW_WIDTH - 22, top + 4, 18, 18).build());

        if (initializedConnected) {
            initConnectedControls();
        } else {
            initConnectionControls();
        }
    }

    @Override
    public void tick() {
        if (initializedConnected != APSession.client().isConnected() && client != null) {
            client.setScreen(new ArchipelagoDashboardScreen());
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(null);
        }
    }

    @Override
    public void renderBackground(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        boolean connected = APSession.client().isConnected();
        int contentTop = top + HEADER_HEIGHT + (connected ? TAB_BAR_HEIGHT : 0);
        int contentLeft = left + 12;
        int contentWidth = WINDOW_WIDTH - 24;
        int contentHeight = WINDOW_HEIGHT - (contentTop - top) - 22;

        context.fill(0, 0, width, height, DashboardTab.COLOR_SCREEN_DIM);

        DashboardTab.drawBeveledPanel(
                context,
                left,
                top,
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                DashboardTab.COLOR_PANEL
        );

        context.fill(
                left + 2,
                top + 2,
                left + WINDOW_WIDTH - 2,
                top + HEADER_HEIGHT,
                DashboardTab.COLOR_HEADER
        );

        if (connected) {
            context.fill(
                    left + 2,
                    top + HEADER_HEIGHT,
                    left + WINDOW_WIDTH - 2,
                    contentTop,
                    DashboardTab.COLOR_TAB_BACKGROUND
            );

            context.fill(
                    left + 2,
                    contentTop - 2,
                    left + WINDOW_WIDTH - 2,
                    contentTop - 1,
                    DashboardTab.COLOR_SEPARATOR_DARK
            );

            context.fill(
                    left + 2,
                    contentTop - 1,
                    left + WINDOW_WIDTH - 2,
                    contentTop,
                    DashboardTab.COLOR_SEPARATOR_LIGHT
            );

            drawSelectedTabHighlight(context);
        }

        context.fill(
                left + 2,
                contentTop,
                left + WINDOW_WIDTH - 2,
                top + WINDOW_HEIGHT - 2,
                DashboardTab.COLOR_CONTENT_BACKGROUND
        );

        context.drawTextWithShadow(
                textRenderer,
                "⚡ Archipelago Dashboard",
                left + 10,
                top + 10,
                DashboardTab.COLOR_TITLE
        );

        if (connected) {
            DashboardTab selectedTab = getSelectedTab();

            if (selectedTab != null) {
                selectedTab.render(
                        context,
                        textRenderer,
                        contentLeft,
                        contentTop + 10,
                        contentWidth,
                        contentHeight,
                        mouseX,
                        mouseY,
                        delta
                );
            } else {
                renderPlaceholder(context, contentLeft, contentTop + 10);
            }
        } else {
            renderConnectionForm(context, contentLeft, contentTop);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DashboardTab selectedTab = getSelectedTab();

        if (APSession.client().isConnected()
                && selectedTab != null
                && selectedTab.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        DashboardTab selectedTab = getSelectedTab();

        if (APSession.client().isConnected()
                && selectedTab != null
                && selectedTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void initConnectedControls() {
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Disconnect"),
                button -> confirmDisconnect()
        ).dimensions(left + WINDOW_WIDTH - 98, top + 5, 72, 18).build());

        int tabX = left + 6;
        int tabY = top + HEADER_HEIGHT + 2;

        for (Page page : Page.values()) {
            Page currentPage = page;

            addDrawableChild(ButtonWidget.builder(
                    page.text(),
                    button -> selectPage(currentPage)
            ).dimensions(tabX, tabY, TAB_WIDTH, 20).build());

            tabX += TAB_WIDTH;
        }
    }

    private void initConnectionControls() {
        int formX = left + 126;
        int formY = top + HEADER_HEIGHT + 47;
        int fieldWidth = 180;

        hostField = addDrawableChild(new TextFieldWidget(
                textRenderer,
                formX,
                formY,
                fieldWidth,
                18,
                Text.literal("Server address")
        ));
        hostField.setMaxLength(255);
        hostField.setText("localhost");

        portField = addDrawableChild(new TextFieldWidget(
                textRenderer,
                formX,
                formY + 30,
                fieldWidth,
                18,
                Text.literal("Port")
        ));
        portField.setMaxLength(5);
        portField.setText("38281");

        slotField = addDrawableChild(new TextFieldWidget(
                textRenderer,
                formX,
                formY + 60,
                fieldWidth,
                18,
                Text.literal("Slot")
        ));
        slotField.setMaxLength(64);

        passwordField = addDrawableChild(new TextFieldWidget(
                textRenderer,
                formX,
                formY + 90,
                fieldWidth,
                18,
                Text.literal("Password")
        ));
        passwordField.setMaxLength(255);

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Connect"),
                button -> connectFromForm()
        ).dimensions(formX + 48, formY + 128, 84, 20).build());

        setInitialFocus(hostField);
    }

    private void renderConnectionForm(
            DrawContext context,
            int x,
            int contentTop
    ) {
        int labelX = left + 54;
        int labelY = contentTop + 50;

        context.drawTextWithShadow(
                textRenderer,
                "Connect to Archipelago",
                x,
                contentTop + 18,
                DashboardTab.COLOR_TITLE
        );

        context.drawTextWithShadow(
                textRenderer,
                "Server",
                labelX,
                labelY,
                DashboardTab.COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                "Port",
                labelX,
                labelY + 30,
                DashboardTab.COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                "Slot",
                labelX,
                labelY + 60,
                DashboardTab.COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                "Password",
                labelX,
                labelY + 90,
                DashboardTab.COLOR_TEXT
        );

        if (!connectionMessage.isBlank()) {
            context.drawTextWithShadow(
                    textRenderer,
                    connectionMessage,
                    x,
                    contentTop + 194,
                    DashboardTab.COLOR_DIM_TEXT
            );
        }
    }

    private void connectFromForm() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String slot = slotField.getText().trim();
        String password = passwordField.getText();

        if (host.isBlank() || port.isBlank() || slot.isBlank()) {
            connectionMessage = "Server, port, and slot are required.";
            return;
        }

        connectionMessage = "Connecting...";

        MinecraftArchipelagoClient.connectFromDashboard(
                client,
                host,
                port,
                slot,
                password
        );
    }

    private void confirmDisconnect() {
        if (client == null) {
            return;
        }

        client.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        MinecraftArchipelagoClient.disconnectFromDashboard(client);
                    }

                    client.setScreen(new ArchipelagoDashboardScreen());
                },
                Text.literal("Disconnect from Archipelago?"),
                Text.empty()
        ));
    }

    private DashboardTab getSelectedTab() {
        return switch (selectedPage) {
            case OVERVIEW -> overviewTab;
            case GOALS -> goalsTab;
            case PROGRESS -> progressTab;
            case UNLOCKS -> unlocksTab;
            case ITEMS -> itemsTab;
            case SETTINGS -> settingsTab;
        };
    }

    private Page loadSelectedPage() {
        try {
            return Page.valueOf(DashboardPreferences.get().selectedDashboardPage);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return Page.OVERVIEW;
        }
    }

    private void selectPage(Page page) {
        selectedPage = page;
        DashboardPreferences preferences = DashboardPreferences.get();
        preferences.selectedDashboardPage = page.name();
        preferences.save();
    }

    private void drawSelectedTabHighlight(DrawContext context) {
        int x = left + 6 + selectedPage.ordinal() * TAB_WIDTH;

        context.fill(
                x,
                top + HEADER_HEIGHT + 20,
                x + TAB_WIDTH,
                top + HEADER_HEIGHT + 22,
                DashboardTab.COLOR_COMPLETE
        );

        context.fill(
                x,
                top + HEADER_HEIGHT + 2,
                x + TAB_WIDTH,
                top + HEADER_HEIGHT + 3,
                DashboardTab.COLOR_TAB_SELECTED
        );
    }

    private void renderPlaceholder(DrawContext context, int x, int y) {
        context.drawTextWithShadow(
                textRenderer,
                selectedPage.label,
                x,
                y,
                DashboardTab.COLOR_TITLE
        );

        context.drawTextWithShadow(
                textRenderer,
                "Content will be added in a later step.",
                x,
                y + 18,
                DashboardTab.COLOR_DIM_TEXT
        );
    }
}
