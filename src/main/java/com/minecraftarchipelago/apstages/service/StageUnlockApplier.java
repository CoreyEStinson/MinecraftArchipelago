package com.minecraftarchipelago.apstages.service;

import com.minecraftarchipelago.apstages.StageRegistry;
import com.minecraftarchipelago.apstages.model.ItemGrant;
import com.minecraftarchipelago.apstages.model.PackageDef;
import com.minecraftarchipelago.apstages.model.StageDef;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StageUnlockApplier
{
    public static void apply(ServerPlayerEntity player, Identifier stageID){
        StageDef stage = StageRegistry.getStage(stageID);
        if (stage == null) {
            player.sendMessage(Text.literal("Invalid stageId: " + stageID));
            return;
        }
        
        applyGamerules(player, stage);
        grantPackages(player, stage);
    }
    
    private static void applyGamerules(ServerPlayerEntity player, StageDef stage){
        for (var entry : stage.gamerules().entrySet()){
            String ruleName = entry.getKey();
            String value = entry.getValue();
            
            applySingleGameRule(player, ruleName, value);
        }
    }
    
    private static void applySingleGameRule(ServerPlayerEntity player, String ruleName, String value){
        MinecraftServer server = player.getServer();
        
        if (server == null) return;
        if (ruleName == null || ruleName.isBlank()) return;
        if (value == null || value.isBlank()) return;
        
        String command = "gamerule " + ruleName + " " + value;
        server.getCommandManager().executeWithPrefix(server.getCommandSource(), command);
        
        player.sendMessage(Text.literal("Applied gamerule: " + ruleName + "=" + value), false);
    }

    private static void grantPackages(ServerPlayerEntity player, StageDef stage){
        for (Identifier packageID : stage.grantPackages()){
            PackageDef pack = StageRegistry.getPackage(packageID);
            if (pack == null) continue;

            grantSinglePackage(player, packageID, pack);
        }
    }

    private  static void grantSinglePackage(ServerPlayerEntity player, Identifier packageID, PackageDef pack){
        for (ItemGrant entry : pack.items()) {
            giveItem(player, entry);
        }
        player.sendMessage(Text.literal("Granted package: " + packageID));
    }
    
    private static void giveItem(ServerPlayerEntity player, ItemGrant entry){
        Item item = Registries.ITEM.get(entry.itemId());
        ItemStack stack = new ItemStack(item, entry.count());
        
        boolean inserted = player.getInventory().insertStack(stack);
        if (!inserted) {
            player.dropItem(stack, false);
        }
        
    }
}
