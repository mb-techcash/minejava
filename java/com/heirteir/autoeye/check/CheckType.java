package com.heirteir.autoeye.check;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter public enum CheckType {
    PRE_MOVE_EVENT("pre_movement", 0), MOVE_EVENT("movement", 1), INVENTORY_EVENT("inventory", 2), ENTITY_USE_EVENT("combat", 3);
    private final String name;
    private final int order;
}
