package com.jgcb.dto;

import java.util.List;

public class SettleRequest {
    private List<SettleItem> items;

    public List<SettleItem> getItems() { return items; }
    public void setItems(List<SettleItem> items) { this.items = items; }
}
