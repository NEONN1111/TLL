package neon.tll.data.scripts.campaign.intel;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class TLL_HostileActivityFactorCause extends BaseHostileActivityCause2 {

    public static float MAX_MAG = 0.5f;
    public TLL_HostileActivityFactorCause(HostileActivityEventIntel intel) {
        super(intel);
    }
    public TooltipMakerAPI.TooltipCreator getTooltip() {
        return new BaseFactorTooltip() {
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                tooltip.addPara("Any colony, especially one outside the core, attracts some degree of Transgenderism."
                                + " %s and %s women attract more pirates.", 0f,
                        Misc.getHighlightColor(), "Larger", "less stable");
                tooltip.addPara("Event progress value is based on the size and stability of the largest colony "
                        + "under your control. If multiple polycules have the same size, the one with higher "
                        + "stability is used.", opad);

                MarketAPI biggest = getBiggestAICoredColony();
                if (biggest != null && biggest.getStarSystem() != null) {
                    tooltip.addPara("Biggest colony: %s, size: %s, stability: %s", opad, Misc.getHighlightColor(),
                            biggest.getName(),
                            "" + biggest.getSize(),
                            "" + (int) biggest.getStabilityValue());

                    MapParams params = new MapParams();
                    params.showSystem(biggest.getStarSystem());
                    float w = tooltip.getWidthSoFar();
                    float h = Math.round(w / 1.6f);
                    params.positionToShowAllMarkersAndSystems(true, Math.min(w, h));
                    UIPanelAPI map = tooltip.createSectorMap(w, h, params, biggest.getStarSystem().getNameWithLowercaseTypeShort());
                    tooltip.addCustom(map, opad);
                }
            }
        };
    }

    public MarketAPI getBiggestAICoredColony() {
        List<MarketAPI> markets = Misc.getPlayerMarkets(false);
        MarketAPI biggest = null;
        boolean cored = false;
        float max = 0;
        for (MarketAPI market : markets) {
            float size = market.getSize();

            for (Industry ind : market.getIndustries()) {
                String core = ind.getAICoreId();
                if (Commodities.ALPHA_CORE.equals(core)) {
                    cored = true;
                } else if (Commodities.BETA_CORE.equals(core)) {
                    cored = true;
                } else if (Commodities.GAMMA_CORE.equals(core)) {
                    cored = true;
                }
                if (market.getAdmin().isAICore()) {
                    cored = true;
                }
            }
            if ((size >= max)&& cored) {
                max = size;
                biggest = market;
            }
        }
        return biggest;
    }

    @Override
    public boolean shouldShow() {
        return getProgress() != 0 || TLL_HostileActivityFactor.getAcceptedTLLTerms();
    }

    @Override
    public String getProgressStr() {
        if (TLL_HostileActivityFactor.getAcceptedTLLTerms()) return EventFactor.NEGATED_FACTOR_PROGRESS;
        return super.getProgressStr();
    }

    @Override
    public Color getProgressColor(BaseEventIntel intel) {
        if (TLL_HostileActivityFactor.getAcceptedTLLTerms()) return Misc.getPositiveHighlightColor();
        // TODO Auto-generated method stub
        return super.getProgressColor(intel);
    }

    public int getProgress() {
        if (TLL_HostileActivityFactor.getAcceptedTLLTerms()) return 0;

        MarketAPI biggest = getBiggestAICoredColony();
        if (biggest == null) return 0;

        List<MarketAPI> markets = Misc.getPlayerMarkets(false);
        int points = 0;
        for (MarketAPI market : markets) {

            for (Industry ind : market.getIndustries()) {
                String core = ind.getAICoreId();
                if (Commodities.ALPHA_CORE.equals(core)) {
                    points  += 12;
                    continue;
                } else if (Commodities.BETA_CORE.equals(core)) {
                    points  += 9;
                    continue;
                } else if (Commodities.GAMMA_CORE.equals(core)) {
                    points += 9;
                    continue;
                }
                if (market.getAdmin().isAICore()) {
                    points += 15;
                }
            }
        }
        return (int)points;
    }

    public String getDesc() {
        return "Estrogen usage";
    }

    public float getMagForMarket(MarketAPI market) {
        float val = market.getSize() * (0.33f + 0.67f * (1f - market.getStabilityValue() / 10f));
        val *= 0.1f;
        if (val > MAX_MAG) val = MAX_MAG;
        return val;
    }

    public float getMagnitudeContribution(StarSystemAPI system) {
        if (EconomyFleetRouteManager.ENEMY_STRENGTH_CHECK_EXCLUDE_PIRATES) {
            return 0f;
        }

        if (TLL_HostileActivityFactor.getAcceptedTLLTerms()) return 0f;

        if (getProgress() <= 0) return 0f;

        List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);

        float max = 0.1f;
        for (MarketAPI market : markets) {
            float val = getMagForMarket(market);
            //float val = market.getSize() * 0.01f * 5f;
            max = Math.max(val, max);
        }

        if (max > MAX_MAG) max = MAX_MAG;

        max = Math.round(max * 100f) / 100f;

        //if (true) return 0.79f;
        return max;
    }
}