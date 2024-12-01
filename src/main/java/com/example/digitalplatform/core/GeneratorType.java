package com.example.digitalplatform.core;

public enum GeneratorType {

    BACKPACK_BELLMAN("Метод Беллмана");

    private final String desc;

    GeneratorType(String desc) {
        this.desc = desc;
    }
    public String getDesc() {
        return desc;
    }

    public static GeneratorType getOrDefault(String name) {
        GeneratorType result = null;
        for (GeneratorType value : GeneratorType.values()) {
            if (value.name().equals(name)) {
                result = value;
            }
        }
        return result == null? BACKPACK_BELLMAN: result;
    }
}
