package com.minecraftarchipelago;

import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.AdvancementLocationHandler;
import com.minecraftarchipelago.aplocations.BossKillListener;
import com.minecraftarchipelago.aplocations.LootableCheckState;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import com.minecraftarchipelago.facades.ArchipelagoClientFacade;
import com.minecraftarchipelago.facades.MinecraftRuntimeFacade;
import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import com.minecraftarchipelago.item.ModItems;
import com.minecraftarchipelago.loot.AssignLootableCheckFunction;
import com.minecraftarchipelago.loot.APLootSourceItemFactory;
import com.minecraftarchipelago.loot.APVillagerLootTrades;
import com.minecraftarchipelago.loot.LootableItemNameCache;
import io.github.archipelagomw.ClientStatus;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class MinecraftArchipelagoGameTest implements FabricGameTest {

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void appliesBaseRulesOnlyOnce(TestContext context) {
        resetHooks(null, null);
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        resetServerState(server);

        Identifier baseRules = Identifier.of("minecraftarchipelago", "base_rules");
        MinecraftArchipelago.applyBaseRulesOnFirstJoin(server, player);
        int firstSize = StageUnlockState.get(server).getUnlocked(player.getUuid()).size();
        MinecraftArchipelago.applyBaseRulesOnFirstJoin(server, player);
        var unlocked = StageUnlockState.get(server).getUnlocked(player.getUuid());

        context.assertTrue(unlocked.contains(baseRules), "base_rules should be unlocked on first join.");
        context.assertEquals(firstSize, unlocked.size(), "base_rules should not be duplicated on repeat join handling.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void advancementCompletionChecksLocationAndAwardsVictory(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, player));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 1,
                "lootable_checks", 0,
                "required_boss_kills", List.of(),
                "required_item_collections", List.of()
        )));
        CheckedLocationsState.get(server).checkLocation(42002L);

        AdvancementLocationHandler.handleCompletedAdvancement(
                server,
                player,
                Identifier.ofVanilla("story/mine_stone")
        );

        context.assertTrue(CheckedLocationsState.get(server).isLocationChecked(42001L), "Completing mine_stone should mark its AP location as checked.");
        context.assertTrue(CheckedLocationsState.get(server).isGoalAchieved(), "Completing the final required advancement should award victory.");
        context.assertTrue(client.checkedLocations.contains(42001L), "Advancement completion should notify Archipelago about the checked location.");
        context.assertTrue(client.gameStates.contains(ClientStatus.CLIENT_GOAL), "Advancement victory should send CLIENT_GOAL.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void bossKillChecksLocationAndAwardsVictory(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, player));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 0,
                "lootable_checks", 0,
                "required_boss_kills", List.of("warden"),
                "required_item_collections", List.of()
        )));

        BossKillListener.handleBossKill(server, SlotData.getBossLocationId("warden"));

        context.assertTrue(CheckedLocationsState.get(server).isLocationChecked(42122L), "A required boss kill should mark its AP location as checked.");
        context.assertTrue(CheckedLocationsState.get(server).isGoalAchieved(), "Killing the final required boss should award victory.");
        context.assertTrue(client.checkedLocations.contains(42122L), "Boss kills should notify Archipelago about the checked location.");
        context.assertTrue(client.gameStates.contains(ClientStatus.CLIENT_GOAL), "Boss-kill victory should send CLIENT_GOAL.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void lootableClaimAssignsConsumesAndAwardsVictory(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, null));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 0,
                "lootable_checks", 2,
                "required_lootable_checks", 1,
                "required_boss_kills", List.of(),
                "required_item_collections", List.of()
        )));

        ItemStack stack = new ItemStack(ModItems.ARCHIPELAGO_CHECK);
        player.setStackInHand(Hand.MAIN_HAND, stack);

        var nbt = ArchipelagoCheckItem.getCustomData(stack);
        long locationId = SlotData.LOOTABLE_CHECK_BASE_ID;

        context.assertTrue(!nbt.getBoolean(ArchipelagoCheckItem.NBT_ASSIGNED), "Loot item should remain unassigned until it is claimed.");
        context.assertEquals(0, LootableCheckState.get(server).getAssignedCount(), "Creating the item should not consume a lootable slot.");
        context.assertEquals(0, client.scoutRequests.size(), "Creating the item should not request a scout.");

        ((ArchipelagoCheckItem) stack.getItem()).use(context.getWorld(), player, Hand.MAIN_HAND);

        context.assertTrue(CheckedLocationsState.get(server).isLocationChecked(locationId), "Claiming the item should check the assigned location.");
        context.assertTrue(CheckedLocationsState.get(server).isGoalAchieved(), "Claiming the final required lootable check should award victory.");
        context.assertTrue(player.getMainHandStack().isEmpty(), "Claiming the loot item should consume it.");
        context.assertEquals(1, LootableCheckState.get(server).getAssignedCount(), "Claiming the item should consume exactly one lootable slot.");
        context.assertTrue(client.checkedLocations.contains(locationId), "Claiming should notify Archipelago about the checked location.");
        context.assertTrue(client.gameStates.contains(ClientStatus.CLIENT_GOAL), "Claiming the final required check should send CLIENT_GOAL.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void generatedLootDoesNotAssignUntilClaim(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, player));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 0,
                "lootable_checks", 2,
                "required_lootable_checks", 0,
                "required_boss_kills", List.of(),
                "required_item_collections", List.of()
        )));

        ItemStack stack = new ItemStack(ModItems.ARCHIPELAGO_CHECK);
        player.getInventory().setStack(0, stack);

        AssignLootableCheckFunction.assignLootableCheck(stack, server);
        stack.getItem().inventoryTick(stack, context.getWorld(), player, 0, false);

        var nbt = ArchipelagoCheckItem.getCustomData(player.getInventory().getStack(0));
        context.assertTrue(!nbt.getBoolean(ArchipelagoCheckItem.NBT_ASSIGNED), "Generated loot should stay unassigned until the player claims it.");
        context.assertEquals(0, LootableCheckState.get(server).getAssignedCount(), "Generating loot should not advance the lootable slot cursor.");
        context.assertEquals(0, client.scoutRequests.size(), "Generating loot should not request scouting.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void secondLootableClaimMarksSurplusWhenPoolIsExhausted(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, null));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 0,
                "lootable_checks", 1,
                "required_lootable_checks", 0,
                "required_boss_kills", List.of(),
                "required_item_collections", List.of()
        )));

        ItemStack first = new ItemStack(ModItems.ARCHIPELAGO_CHECK);
        player.setStackInHand(Hand.MAIN_HAND, first);
        ((ArchipelagoCheckItem) first.getItem()).use(context.getWorld(), player, Hand.MAIN_HAND);

        ItemStack second = new ItemStack(ModItems.ARCHIPELAGO_CHECK);
        player.setStackInHand(Hand.MAIN_HAND, second);
        ((ArchipelagoCheckItem) second.getItem()).use(context.getWorld(), player, Hand.MAIN_HAND);

        context.assertEquals(1, LootableCheckState.get(server).getAssignedCount(), "A surplus claim should not advance the lootable slot cursor.");
        context.assertEquals(1, CheckedLocationsState.get(server).countCheckedInRange(
                SlotData.LOOTABLE_CHECK_BASE_ID,
                SlotData.LOOTABLE_CHECK_BASE_ID + 1
        ), "A surplus claim should not create an extra checked location.");
        context.assertTrue(player.getMainHandStack().isEmpty(), "A surplus loot item should still be consumed when used.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void lootSourceMetadataIsWrittenToGeneratedItems(TestContext context) {
        ItemStack stack = APLootSourceItemFactory.create("fishing", "Fishing");
        var nbt = ArchipelagoCheckItem.getCustomData(stack);

        context.assertTrue("fishing".equals(nbt.getString(ArchipelagoCheckItem.NBT_LOOT_SOURCE)), "AP loot source id should be written to item custom data.");
        context.assertTrue("Fishing".equals(nbt.getString(ArchipelagoCheckItem.NBT_LOOT_SOURCE_NAME)), "AP loot source display name should be written to item custom data.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void villagerLootTradeIsSingleUseAndKeepsSourceMetadata(TestContext context) {
        var offer = APVillagerLootTrades.createOffer("villager_master", "Villager Master Trade", Items.EMERALD, 24);
        var sellItem = offer.getSellItem();
        var nbt = ArchipelagoCheckItem.getCustomData(sellItem);

        context.assertEquals(1, offer.getMaxUses(), "Villager AP trade should be single use.");
        context.assertTrue(sellItem.getItem() instanceof ArchipelagoCheckItem, "Villager AP trade should sell an AP check item.");
        context.assertTrue("villager_master".equals(nbt.getString(ArchipelagoCheckItem.NBT_LOOT_SOURCE)), "Villager trade source id should be written.");
        context.assertTrue("Villager Master Trade".equals(nbt.getString(ArchipelagoCheckItem.NBT_LOOT_SOURCE_NAME)), "Villager trade source display name should be written.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void receivedDeathLinkKillsPlayerWithoutSendingOneBack(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, player));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 0,
                "death_link", true,
                "lootable_checks", 0,
                "required_boss_kills", List.of(),
                "required_item_collections", List.of()
        )));
        APSession.setPendingCredentials("host", "38281", "LocalSlot", null);

        DeathLinkHandler.applyReceivedDeathLink(player, "RemoteSlot");

        context.assertTrue(player.isDead(), "Receiving a Death Link should kill the player.");
        context.assertTrue(!DeathLinkHandler.isReceivingDeathLink(), "The receiving-death-link guard should be reset after damage is applied.");
        context.assertTrue(client.sentDeathLinks.isEmpty(), "A received Death Link should not send a new Death Link back out.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void sendingDeathLinkUsesPendingSlotWhenEnabled(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        RecordingClientFacade client = new RecordingClientFacade();
        resetHooks(client, new ImmediateRuntimeFacade(server, player));
        resetServerState(server);
        APSession.setSlotData(SlotData.parse(Map.of(
                "advancement_goal", 0,
                "death_link", true,
                "lootable_checks", 0,
                "required_boss_kills", List.of(),
                "required_item_collections", List.of()
        )));
        APSession.setPendingCredentials("host", "38281", "LocalSlot", null);

        boolean sent = DeathLinkHandler.maybeSendDeathLink(player);

        context.assertTrue(sent, "Death Link should be sent when the slot has Death Link enabled and the client is connected.");
        context.assertTrue(client.sentDeathLinks.size() == 1, "Sending a Death Link should produce exactly one outbound Death Link message.");
        context.assertTrue(client.sentDeathLinks.getFirst().startsWith("LocalSlot|"), "The outbound Death Link should use the pending slot name.");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void progressiveItemsUnlockEachTierAndThenStop(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        resetHooks(new RecordingClientFacade(), new ImmediateRuntimeFacade(server, null));
        resetServerState(server);

        APEvents.handleReceivedItem(server, player, 43000L, 100);
        APEvents.handleReceivedItem(server, player, 43000L, 101);
        APEvents.handleReceivedItem(server, player, 43000L, 102);
        APEvents.handleReceivedItem(server, player, 43000L, 103);
        int sizeAfterFourthReceipt = StageUnlockState.get(server).getUnlocked(player.getUuid()).size();
        APEvents.handleReceivedItem(server, player, 43000L, 104);

        var unlocked = StageUnlockState.get(server).getUnlocked(player.getUuid());
        context.assertTrue(unlocked.contains(Identifier.of("minecraftarchipelago", "tools/stone_tools")), "Stone tools should unlock first.");
        context.assertTrue(unlocked.contains(Identifier.of("minecraftarchipelago", "tools/iron_tools")), "Iron tools should unlock second.");
        context.assertTrue(unlocked.contains(Identifier.of("minecraftarchipelago", "tools/diamond_tools")), "Diamond tools should unlock third.");
        context.assertTrue(unlocked.contains(Identifier.of("minecraftarchipelago", "tools/netherite_tools")), "Netherite tools should unlock fourth.");
        context.assertTrue(unlocked.size() == sizeAfterFourthReceipt, "A fifth progressive receipt should not unlock an extra tier.");
        context.complete();
    }

    private static void resetHooks(RecordingClientFacade client, ImmediateRuntimeFacade runtime) {
        APSession.resetForTests();
        LootableItemNameCache.clear();
        if (client != null) {
            APSession.setClientFacadeForTests(client);
        }
        if (runtime != null) {
            APSession.setRuntimeFacadeForTests(runtime);
        }
    }

    private static void resetServerState(MinecraftServer server) {
        CheckedLocationsState.get(server).resetForTests();
        LootableCheckState.get(server).resetForTests();
    }

    private static final class RecordingClientFacade implements ArchipelagoClientFacade {
        final List<Long> checkedLocations = new ArrayList<>();
        final List<List<Long>> scoutRequests = new ArrayList<>();
        final List<String> sentDeathLinks = new ArrayList<>();
        final List<ClientStatus> gameStates = new ArrayList<>();

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public String getConnectedAddress() {
            return "gametest";
        }

        @Override
        public void checkLocation(long locationId) {
            checkedLocations.add(locationId);
        }

        @Override
        public void scoutLocations(List<Long> locations) {
            scoutRequests.add(new ArrayList<>(locations));
        }

        @Override
        public void sendDeathLink(String slotName, String cause) {
            sentDeathLinks.add(slotName + "|" + cause);
        }

        @Override
        public void setGameState(ClientStatus status) {
            gameStates.add(status);
        }

        @Override
        public void setDeathLinkEnabled(boolean enabled) {
        }
    }

    private record ImmediateRuntimeFacade(MinecraftServer server, net.minecraft.entity.player.PlayerEntity player)
            implements MinecraftRuntimeFacade {
        @Override
        public void executeOnClient(Runnable action) {
            action.run();
        }

        @Override
        public MinecraftServer getCurrentServer() {
            return server;
        }

        @Override
        public net.minecraft.entity.player.PlayerEntity getCurrentPlayer() {
            return player;
        }
    }
}
