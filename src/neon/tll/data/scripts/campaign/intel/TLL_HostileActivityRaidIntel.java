package neon.tll.data.scripts.campaign.intel;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;

public class TLL_HostileActivityRaidIntel extends GenericRaidFGI {
    public TLL_HostileActivityRaidIntel(GenericRaidParams params) {
        super(params);
    }
    @Override
    public boolean hasCustomRaidAction(){
        return false; //for now
    }
    @Override
    public void doCustomRaidAction(CampaignFleetAPI fleet, MarketAPI market, float raidStr) {

    }
}
