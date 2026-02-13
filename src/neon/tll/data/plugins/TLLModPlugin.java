package neon.tll.data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import neon.tll.data.scripts.tll_gen;
import neon.tll.data.scripts.tll_gen2;
import neon.tll.data.scripts.tll_people;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.apache.log4j.Logger;

import java.util.ArrayList;


public class TLLModPlugin extends BaseModPlugin {
    public static boolean hasMagicLib = false;
    public Logger log = Logger.getLogger(this.getClass());
    public static boolean HAS_GRAPHICSLIB = false;

    @Override
    public void onGameLoad(boolean newGame) {
        // This method was empty in the original
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        new tll_people().createTLLPeople();
    }

    @Override
    public void onNewGameAfterProcGen() {
        // NSP content
        SectorAPI sector = Global.getSector();
        // ThemeGenerator NSPThemeGenerator;
       // ArrayList<String> systemBL = new ArrayList<>();
       // ArrayList<String> tagBL = new ArrayList<>();
       // tagBL.add(Tags.THEME_HIDDEN);
       // tagBL.add(Tags.SYSTEM_ALREADY_USED_FOR_STORY);
       // tagBL.add(Tags.SYSTEM_ABYSSAL);
       // tagBL.add(Tags.STAR_HIDDEN_ON_MAP);
       // tagBL.add("theme_d"); // if people are still running DME, i guess?

        new tll_gen().generate(Global.getSector());
        new tll_gen2().generate(Global.getSector());
        //  new nsp_dump_system().generate(Global.getSector());
    }

    @Override
    public void onApplicationLoad() throws Exception {
        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            // LightData.readLightDataCSV((String) "data/config/example_lights_data.csv");
            log.info("TLL shaders active");
        }
        hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");

        log.info("Welcome to TLL! I'm in your hulls...");
    }

    // TLL Relations method from original NSP mod plugin
    public static void Tll_Relations() {
        FactionAPI tll = Global.getSector().getFaction("tll");

        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (tll.equals(faction) || (faction.isPlayerFaction() && tll.getId().equals(Misc.getCommissionFactionId())))
                continue;
            tll.setRelationship(faction.getId(), -0.2f);
        }

        tll.setRelationship(Factions.LUDDIC_CHURCH, -0.2f);
        tll.setRelationship(Factions.LUDDIC_PATH, -0.9f);
        tll.setRelationship(Factions.TRITACHYON, -0.4f);
        tll.setRelationship(Factions.PERSEAN, -0.3f);
        tll.setRelationship(Factions.PIRATES, -0.6f);
        tll.setRelationship(Factions.INDEPENDENT, 0.7f);
        tll.setRelationship(Factions.DIKTAT, -0.5f);
        tll.setRelationship(Factions.LIONS_GUARD, -0.5f);
        tll.setRelationship(Factions.HEGEMONY, -0.6f);
        tll.setRelationship(Factions.REMNANTS, 0.4f);
        tll.setRelationship(Factions.PLAYER, -0.4f);

        if (Global.getSettings().getModManager().isModEnabled("blade_breakers")) {
            tll.setRelationship("blade_breakers", -0.7f);
        }
        if (Global.getSettings().getModManager().isModEnabled("exipirated")) {
            tll.setRelationship("exipirated", -0.6f);
        }
        if (Global.getSettings().getModManager().isModEnabled("gmda")) {
            tll.setRelationship("gmda", -0.6f);
        }
        if (Global.getSettings().getModManager().isModEnabled("gmda_patrol")) {
            tll.setRelationship("gmda_patrol", -0.6f);
        }
        if (Global.getSettings().getModManager().isModEnabled("draco")) {
            tll.setRelationship("draco", -0.6f);
        }
        if (Global.getSettings().getModManager().isModEnabled("fang")) {
            tll.setRelationship("fang", -0.6f);
        }
        if (Global.getSettings().getModManager().isModEnabled("HMI")) {
            tll.setRelationship("mess", -0.8f);
        }
        if (Global.getSettings().getModManager().isModEnabled("pigeonpun_projectsolace")) {
            tll.setRelationship("projectsolace", 0.5f);
        }
        if (Global.getSettings().getModManager().isModEnabled("tahlan")) {
            tll.setRelationship("tahlan_legioinfernalis", -0.8f);
        }
        if (Global.getSettings().getModManager().isModEnabled("diableavionics")) {
            tll.setRelationship("diableavionics", -0.7f);
        }
        if (Global.getSettings().getModManager().isModEnabled("scalartech")) {
            tll.setRelationship("scalartech", 0f);
        }
        if (Global.getSettings().getModManager().isModEnabled("HIVER")) {
            tll.setRelationship("HIVER", -0.7f);
        }
    }
}