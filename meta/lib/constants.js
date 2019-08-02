"use strict";
(function(){
  var c = {};
  c.modid = "rsgauges";
  c.mod_registry_name = function() { return "rsgauges" }
  c.local_assets_root = function() { return "src/main/resources/assets/" + c.mod_registry_name(); }
  c.local_data_root   = function() { return "src/main/resources/data/" + c.mod_registry_name(); }
  c.reference_repository = function() { return "git@github.com:stfwi/rsgauges.git"; }
  c.gradle_property_modversion = function() { return "version_rsgauges"; }
  c.gradle_property_version_minecraft = function() { return "version_minecraft"; }
  c.gradle_property_version_forge = function() { return "version_forge"; }
  c.project_download_inet_page = function() { return "https://www.curseforge.com/minecraft/mc-mods/redstone-gauges-and-switches/"; }
  c.options = {}
  c.languages = {
    "en_us": { code:"en_us", name:"English", region:"United States" },
    "de_de": { code:"de_de", name:"German", region:"Germany" },
    "ru_ru": { code:"ru_ru", name:"Russian", region:"Russia" },
    "zh_cn": { code:"zh_cn", name:"Chinese", region:"China" }
  }

  c.registryname_map_112_114 = {
    "arrowtarget":"arrow_target",
    "automaticswitch1":"industrial_entity_detector",
    "automaticswitch2":"industrial_linear_entity_detector",
    "automaticswitch3":"industrial_light_sensor",
    "automaticswitch4":"industrial_day_timer",
    "automaticswitch5":"industrial_rain_sensor",
    "automaticswitch6":"industrial_lightning_sensor",
    "automaticswitch7":"industrial_interval_timer",
    "bistableswitch_glass1":"glass_rotary_switch",
    "bistableswitch_glass2":"glass_touch_switch",
    "bistableswitch_oldfancy1":"oldfancy_bistableswitch1",
    "bistableswitch_oldfancy2":"oldfancy_bistableswitch2",
    "bistableswitch_oldfancy3":"oldfancy_bistableswitch3",
    "bistableswitch_oldfancy4":"oldfancy_bistableswitch4",
    "bistableswitch_oldfancy5":"oldfancy_bistableswitch5",
    "bistableswitch_oldfancy6":"oldfancy_bistableswitch6",
    "bistableswitch_oldfancy7":"oldfancy_bistableswitch7",
    "bistableswitch_rustic1":"rustic_lever",
    "bistableswitch_rustic2":"rustic_two_hinge_lever",
    "bistableswitch_rustic3":"rustic_angular_lever",
    "bistableswitch_rustic4":"rustic_weight_balanced_lever",
    "bistableswitch_rustic5":"rustic_bistable_handle",
    "bistableswitch_rustic6":"rustic_bistable_slide",
    "bistableswitch_rustic7":"rustic_nail_lever",
    "bistableswitch1":"industrial_rotary_machine_switch",
    "bistableswitch2":"industrial_small_lever",
    "bistableswitch3":"industrial_machine_switch",
    "bistableswitch4":"light_switch",
    "bistableswitch5":"industrial_estop_switch",
    "bistableswitch6":"industrial_hopper_switch",
    "bistableswitch7":"industrial_rotary_lever",
    "bistableswitch8":"industrial_lever",
    "contactmat_glass1":"glass_door_contact_mat",
    "contactmat_glass2":"glass_contact_mat",
    "contactmat_rustic1":"rustic_door_contact_plate",
    "contactmat_rustic2":"rustic_contact_plate",
    "contactmat_rustic3":"rustic_shock_sensitive_plate",
    "contactmat1":"industrial_door_contact_mat",
    "contactmat2":"industrial_contact_mat",
    "contactmat3":"industrial_shock_sensitive_contact_mat",
    "daytimeswitch_glass1":"glass_day_timer",
    "detectorswitch_glass1":"glass_entity_detector",
    "detectorswitch_glass2":"glass_linear_entity_detector",
    "dimmerswitch1":"industrial_dimmer",
    "flatgauge1":"industrial_analog_angular_gauge",
    "flatgauge2":"industrial_vertical_bar_gauge",
    "flatgauge3":"industrial_small_digital_gauge",
    "flatgauge4":"glass_vertical_bar_gauge",
    "flatgauge5":"industrial_tube_gauge",
    "flatgauge6":"industrial_analog_horizontal_gauge",
    "gauge_rustic2":"rustic_circular_gauge",
    "indicator_led_white":"industrial_white_led",
    "indicator_led_white_blink":"industrial_white_blinking_led",
    "indicator_rustic_flag":"rustic_semaphore",
    "indicator1":"industrial_green_led",
    "indicator1blink1":"industrial_green_blinking_led",
    "indicator2":"industrial_yellow_led",
    "indicator2blink1":"industrial_yellow_blinking_led",
    "indicator3":"industrial_red_led",
    "indicator3blink1":"industrial_red_blinking_led",
    "indicator4":"industrial_alarm_lamp",
    "observerswitch1":"industrial_block_detector",
    "powerplant_red":"red_power_plant",
    "powerplant_yellow":"yellow_power_plant",
    "pulseswitch_glass1":"glass_button",
    "pulseswitch_glass2":"glass_small_button",
    "pulseswitch_glass3":"glass_touch_button",
    "pulseswitch_oldfancy1":"oldfancy_button",
    "pulseswitch_oldfancy2":"oldfancy_spring_reset_chain",
    "pulseswitch_oldfancy3":"oldfancy_spring_reset_handle",
    "pulseswitch_oldfancy4":"oldfancy_small_button",
    "pulseswitch_rustic1":"rustic_button",
    "pulseswitch_rustic2":"rustic_small_button",
    "pulseswitch_rustic3":"rustic_spring_reset_chain",
    "pulseswitch_rustic4":"rustic_weight_reset_chain",
    "pulseswitch_rustic5":"rustic_spring_reset_pull_handle",
    "pulseswitch_rustic6":"rustic_spring_reset_push_handle",
    "pulseswitch_rustic7":"rustic_nail_button",
    "pulseswitch1":"industrial_button",
    "pulseswitch2":"industrial_fenced_button",
    "pulseswitch3":"industrial_pull_handle",
    "pulseswitch5":"industrial_foot_button",
    "pulseswitch6":"industrial_double_pole_button",
    "qube":"qube",
    "relay_bistableswitchrx1":"industrial_switchlink_receiver",
    "relay_bistableswitchrx2":"industrial_switchlink_cased_receiver",
    "relay_bistableswitchtx1":"industrial_switchlink_relay",
    "relay_pulseswitchrx1":"industrial_switchlink_pulse_receiver",
    "relay_pulseswitchrx2":"industrial_switchlink_cased_pulse_receiver",
    "relay_pulseswitchtx1":"industrial_switchlink_pulse_relay",
    "sensitiveglass":"sensitive_glass_block",
    "sensitiveglass_black":"black_sensitiveglass",
    "sensitiveglass_blue":"blue_sensitiveglass",
    "sensitiveglass_brown":"brown_sensitiveglass",
    "sensitiveglass_cyan":"cyan_sensitiveglass",
    "sensitiveglass_gray":"gray_sensitiveglass",
    "sensitiveglass_green":"green_sensitiveglass",
    "sensitiveglass_inverted":"inverted_sensitiveglass",
    "sensitiveglass_lightblue":"lightblue_sensitiveglass",
    "sensitiveglass_lightgray":"lightgray_sensitiveglass",
    "sensitiveglass_lime":"lime_sensitiveglass",
    "sensitiveglass_magenta":"magenta_sensitiveglass",
    "sensitiveglass_orange":"orange_sensitiveglass",
    "sensitiveglass_pink":"pink_sensitiveglass",
    "sensitiveglass_purple":"purple_sensitiveglass",
    "sensitiveglass_red":"red_sensitiveglass",
    "sensitiveglass_white":"white_sensitiveglass",
    "sensitiveglass_yellow":"yellow_sensitiveglass",
    "soundindicator1":"industrial_alarm_siren",
    "timerswitch_glass1":"glass_interval_timer",
    "trapdoorswitch_rustic1":"rustic_shock_sensitive_trapdoor",
    "trapdoorswitch_rustic2":"rustic_high_sensitive_trapdoor",
    "trapdoorswitch_rustic3":"rustic_fallthrough_detector",
    "trapdoorswitch1":"industrial_shock_sensitive_trapdoor",
    "trapdoorswitch2":"industrial_high_sensitive_trapdoor",
    "trapdoorswitch3":"industrial_fallthrough_detector"
  };

  Object.freeze(c.registryname_map_112_114);
  Object.freeze(c.languages);
  Object.freeze(c);
  return c;
});
