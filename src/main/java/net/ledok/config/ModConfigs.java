package net.ledok.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.ledok.YggdrasilLdMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfigs {
    // --- Puffish Skills Main skill tree NOT USED ---
    public String puffish_skills_tree_id = "puffish_skills:minestar";

    // --- Runtime toggle for the partial inventory save feature ---
    public transient boolean partial_inventory_save_enabled = true;

    // --- PRIME ---
    public boolean prime_role_sync_enabled = true;

    // --- ELYTRA BOOST BLACKLISTED DIMENSIONS
    public List<String> elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld", "minecraft:the_nether"));
    public boolean elytra_armor_threshold_enabled = true;
    public int elytra_armor_threshold = 20;

    // --- PARTIAL INVENTORY SAVE
    public double keep_inventory_drop_percentage = 50.0;


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), YggdrasilLdMod.MOD_ID + ".json");

    public static ModConfigs load() {
        ModConfigs config;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfigs.class);
                if (config == null) { config = new ModConfigs(); }
                if (config.elytra_boost_disabled_dimensions == null) {
                    config.elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld", "minecraft:the_nether"));
                }
                try {
                    boolean checkEnabled = config.elytra_armor_threshold_enabled;
                    int checkThreshold = config.elytra_armor_threshold;
                } catch (NullPointerException | NoSuchFieldError e) {
                    config.elytra_armor_threshold_enabled = true;
                    config.elytra_armor_threshold = 20;
                }

            }catch (IOException e) {
                YggdrasilLdMod.LOGGER.error("Failed to load config, using defaults.", e);
                config = new ModConfigs();
            }
        } else {
            config = new ModConfigs();
        }
        config.partial_inventory_save_enabled = true;
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            YggdrasilLdMod.LOGGER.error("Failed to save config.", e);
        }
    }
}
