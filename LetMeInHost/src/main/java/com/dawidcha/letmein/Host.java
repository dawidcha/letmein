package com.dawidcha.letmein;

import java.util.HashMap;
import java.util.Map;

public class Host {
    private static Host instance = new Host();
    private final Map<String, ActionCommands> controllers = new HashMap<>();

    public static Host getInstance() {
        return instance;
    }
}
