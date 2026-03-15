package com.minecraftarchipelago.apstages.item;

import com.minecraftarchipelago.apstages.StageRegistry;
import com.minecraftarchipelago.apstages.model.ItemStageRules;
import com.minecraftarchipelago.apstages.model.StageDef;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

public class ItemAccessHelper
{
    private static boolean matchesItemIds(ItemStack stack, Set<Identifier> ids){
        for (Identifier id : ids){
            Item item = Registries.ITEM.get(id);
            if (stack.isOf(item)) return true;
        }
        
        return false;
    }
    
    private static boolean matchesItemTags(ItemStack stack, Set<Identifier> tagIds){
        for (Identifier tagId : tagIds){
            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagId);
            if (stack.isIn(tag)) return true;
        }
        
        return false;
    }
    
    private static boolean matchesAnyLock(ItemStack stack, ItemStageRules rules){
        return  matchesItemIds(stack, rules.lockItemIds()) ||
                matchesItemTags(stack, rules.lockItemTags());
    }
    
    private static boolean matchesAnyUnlock(ItemStack stack, ItemStageRules rules){
        return  matchesItemIds(stack, rules.unlockItemIds()) ||
                matchesItemTags(stack, rules.unlockItemTags());
    }
    
    public static boolean isLocked(ServerPlayerEntity player, ItemStack stack){
        Set<Identifier> unlockedStages = StageUnlockState.get(player.getServer()).getUnlocked(player.getUuid());
        
        for (Identifier stageId : unlockedStages){
            StageDef stage = StageRegistry.getStage(stageId);
            if (stage == null) continue;
            
            if (matchesAnyUnlock(stack, stage.itemRules())) return false;
        }
        
        for (Identifier stageId : unlockedStages){
            StageDef stage = StageRegistry.getStage(stageId);
            if (stage == null) continue;
            
            if (matchesAnyLock(stack, stage.itemRules())) return true;
        }
        
        return false;
    }
}
