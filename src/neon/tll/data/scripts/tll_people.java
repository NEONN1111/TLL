package neon.tll.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

public class tll_people {

    public static void create() {
        createTLLPeople();
    }

    public static PersonAPI createTLLPeople() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        // First, check if Providence already exists to avoid duplicates
        PersonAPI providence = ip.getPerson("tll_providence");
        if (providence == null) {
            // Create Providence only once
            providence = Global.getFactory().createPerson();
            providence.setId("tll_providence");
            providence.setFaction("tll");
            providence.setGender(FullName.Gender.MALE);
            providence.setPostId(Ranks.POST_FACTION_LEADER);
            providence.setRankId(Ranks.SPACE_ADMIRAL);
            providence.setImportance(PersonImportance.VERY_HIGH);
            providence.getName().setFirst("Providence");
            providence.getName().setLast("Redemptor");
            providence.setPortraitSprite("graphics/portraits/portrait_ai2b.png");
            providence.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            providence.getStats().setSkillLevel(Skills.HYPERCOGNITION, 1);
            ip.addPerson(providence);
        }

        // Create the admiral
        PersonAPI admiral1 = Global.getSector().getFaction("tll").createRandomPerson(FullName.Gender.ANY);
        admiral1.setId("tll_admiral1");
        admiral1.setPostId(Ranks.POST_OFFICER);
        admiral1.setRankId(Ranks.SPACE_ADMIRAL);
        admiral1.setPersonality(Personalities.AGGRESSIVE);
        admiral1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        admiral1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        admiral1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        admiral1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        admiral1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        admiral1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        admiral1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        admiral1.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
        admiral1.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
        admiral1.getStats().setLevel(9);
        ip.addPerson(admiral1);

        // Now assign Providence as admin to all TLL colonies
        // Tellas
        MarketAPI market1 = Global.getSector().getEconomy().getMarket("tll_tellas");
        if (market1 != null) {
            market1.setAdmin(providence);
            market1.getCommDirectory().addPerson(providence, 0);
            market1.addPerson(providence);
        }

        // Vokha
        MarketAPI market2 = Global.getSector().getEconomy().getMarket("tll_vokha");
        if (market2 != null) {
            market2.setAdmin(providence);
            market2.getCommDirectory().addPerson(providence, 0);
            market2.addPerson(providence);
        }

        // Nisse
        MarketAPI market3 = Global.getSector().getEconomy().getMarket("tll_nisse");
        if (market3 != null) {
            market3.setAdmin(providence);
            market3.getCommDirectory().addPerson(providence, 0);
            market3.addPerson(providence);
        }

        // Pyyrhus - Note: The market ID is "tll_pyyrhus" (from tll_system.java), not "tll_pyyhrus"
        MarketAPI market4 = Global.getSector().getEconomy().getMarket("tll_pyyrhus");
        if (market4 != null) {
            market4.setAdmin(providence);
            market4.getCommDirectory().addPerson(providence, 0);
            market4.addPerson(providence);
        } else {
            // Try alternative ID just in case
            MarketAPI market4_alt = Global.getSector().getEconomy().getMarket("tll_pyyhrus");
            if (market4_alt != null) {
                market4_alt.setAdmin(providence);
                market4_alt.getCommDirectory().addPerson(providence, 0);
                market4_alt.addPerson(providence);
            }
        }

        // Brigid
        MarketAPI market5 = Global.getSector().getEconomy().getMarket("tll_brigid");
        if (market5 != null) {
            market5.setAdmin(providence);
            market5.getCommDirectory().addPerson(providence, 0);
            market5.addPerson(providence);
        }

        // Vosta
        MarketAPI market6 = Global.getSector().getEconomy().getMarket("tll_vosta");
        if (market6 != null) {
            market6.setAdmin(providence);
            market6.getCommDirectory().addPerson(providence, 0);
            market6.addPerson(providence);
        }

        // Amphitrite
        MarketAPI market7 = Global.getSector().getEconomy().getMarket("tll_amphitrite");
        if (market7 != null) {
            market7.setAdmin(providence);
            market7.getCommDirectory().addPerson(providence, 0);
            market7.addPerson(providence);
        }

        return providence;
    }
}