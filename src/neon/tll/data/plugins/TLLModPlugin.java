package neon.tll.data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import neon.tll.data.scripts.tll_gen;
import neon.tll.data.scripts.tll_gen2;
import neon.tll.data.scripts.tll_people;
import org.dark.shaders.util.ShaderLib;
import org.apache.log4j.Logger;

public class TLLModPlugin extends BaseModPlugin {
    public static boolean hasMagicLib = false;
    public Logger log = Logger.getLogger(this.getClass());
    public static boolean HAS_GRAPHICSLIB = false;

    @Override
    public void onGameLoad(boolean newGame) {
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        new tll_people().createTLLPeople();
    }

    @Override
    public void onNewGameAfterProcGen() {
        new tll_gen().generate(Global.getSector());
        new tll_gen2().generate(Global.getSector());
    }

    @Override
    public void onApplicationLoad() throws Exception {
        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            log.info("TLL shaders active");
        }
        hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");

        log.info("Welcome to TLL! I'm in your hulls...");
    }

    @Override
    public void onNewGame() {
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            String factionId = faction.getId();

            switch (factionId) {
                case Factions.HEGEMONY:
                    faction.setRelationship("tll", RepLevel.INHOSPITABLE);
                    break;
                case Factions.LUDDIC_PATH:
                    faction.setRelationship("tll", RepLevel.VENGEFUL);
                    break;
                case Factions.PIRATES:
                    faction.setRelationship("tll", RepLevel.HOSTILE);
                    break;
                case Factions.LUDDIC_CHURCH:
                    faction.setRelationship("tll", RepLevel.HOSTILE);
                    break;
                case Factions.DIKTAT:
                    faction.setRelationship("tll", RepLevel.INHOSPITABLE);
                    break;
                case Factions.PERSEAN:
                    faction.setRelationship("tll", RepLevel.FAVORABLE);
                    break;
                case Factions.TRITACHYON:
                    faction.setRelationship("tll", RepLevel.SUSPICIOUS);
                    break;
                case Factions.INDEPENDENT:
                    faction.setRelationship("tll", RepLevel.NEUTRAL);
                    break;
                case Factions.PLAYER:
                    faction.setRelationship("tll", RepLevel.SUSPICIOUS);
                    break;
            }
        }
    }

    @Override
    public void onAboutToStartGeneratingCodex() {
        // This runs before codex generation
    }

}