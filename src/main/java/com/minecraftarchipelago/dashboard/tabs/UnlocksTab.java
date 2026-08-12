package com.minecraftarchipelago.dashboard.tabs;

import com.minecraftarchipelago.dashboard.ReceiptHistoryTab;
import com.minecraftarchipelago.dashboard.ReceiptKind;
import com.minecraftarchipelago.dashboard.ReceiptRecord;

import java.util.List;

public class UnlocksTab extends ReceiptHistoryTab {
    @Override
    protected ReceiptKind receiptKind() {
        return ReceiptKind.UNLOCK;
    }

    @Override
    protected String title() {
        return "Unlocks";
    }

    @Override
    protected String unavailableMessage() {
        return "Load a singleplayer world to view received unlocks.";
    }

    @Override
    protected String emptyMessage() {
        return "No unlocks have been received for this slot.";
    }

    @Override
    protected String totalText(List<ReceiptRecord> receipts) {
        return receipts.size() + " received";
    }
}
