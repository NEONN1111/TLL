package neon.tll.data.scripts.campaign;

import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import neon.tll.data.plugins.TLLModPlugin;

public class TLL_EconListenerScript implements EconomyTickListener {

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        TLLModPlugin.addTLLColonyCrisis();
    }
}