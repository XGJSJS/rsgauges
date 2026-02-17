package wile.rsgauges.fabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import wile.rsgauges.fabric.config.ModConfigs;

public class RsGaugesModMenuIntegration implements ModMenuApi {
    @Override
    @SuppressWarnings("all")
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            /*Map<String, String> configs = new HashMap<>();
            try {
                File data = new File(FabricLoader.getInstance().getConfigDir().toFile(), RsGaugesMod.MODID + ".toml");
                FileReader reader = new FileReader(data);
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null) {
                    String[] temp = line.split("=");
                    configs.put(temp[0], temp[1]);
                }
                br.close();
                reader.close();
            } catch (IOException e) {
                RsGaugesFabric.LOGGER.error(e.getMessage(), e);
            }
            if (configs.containsKey(PATTERN_EXCLUDES.getId())) {
                PATTERN_EXCLUDES.setValue(configs.get(PATTERN_EXCLUDES.getId()).replaceAll("'", ""));
            }
            ConfigBuilder config = ConfigBuilder.create();
            config.setTitle(Component.translatable("rsgauges.config.title"));
            ConfigCategory opt = config.getOrCreateCategory(Component.literal("Opt-out settings"));
            opt.addEntry(new StringListEntry(Component.literal("pattern_excludes"), PATTERN_EXCLUDES.getValue(), RESET, () -> "", v -> PATTERN_EXCLUDES.setValue(v)));
            opt.addEntry(new StringListEntry(Component.literal("pattern_includes"), "", RESET, () -> "", v -> {}));
            opt.addEntry(new BooleanListEntry(Component.literal("without_switch_linking"), false, RESET, () -> false, v -> {}));
            ConfigCategory miscellaneous = config.getOrCreateCategory(Component.literal("miscellaneous"));
            miscellaneous.addEntry(new BooleanListEntry(Component.literal("with_experimental"), false, RESET, () -> false, v -> {}));
            config.setSavingRunnable(() -> {
                try {
                    File data = new File(FabricLoader.getInstance().getConfigDir().toFile(), RsGaugesMod.MODID + ".toml");
                    FileWriter data_writer = new FileWriter(data);
                    BufferedWriter bw = new BufferedWriter(data_writer);
                    bw.write(PATTERN_EXCLUDES.getId() + "='" + PATTERN_EXCLUDES.getValue() + "'");
                    bw.newLine();
                    bw.close();
                    data_writer.close();
                } catch (IOException e) {
                    RsGaugesFabric.LOGGER.error(e.getMessage(), e);
                }
            });
            return config.setParentScreen(parent).build();*/
            return AutoConfig.getConfigScreen(ModConfigs.class, parent).get();
        };
    }
}