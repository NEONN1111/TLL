package neon.tll.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import org.lwjgl.util.vector.Vector2f;
import neon.tll.data.scripts.util.TLL_factions;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class tll_system2 implements SectorGeneratorPlugin {
	public static String NOT_RANDOM_MISSION_TARGET = "$not_random_mission_target";

	@Override
	public void generate(SectorAPI sector) {
		Vector2f location = new Vector2f(-27697, 11959);

		StarSystemAPI system = Global.getSector().createStarSystem("Alaugon");
		system.setName("Alaugon");
		system.getLocation().set(location);
		system.initNonStarCenter();
		system.generateAnchorIfNeeded();
		system.addTag(Tags.THEME_REMNANT);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);

		system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");

		// Clear nebula
		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyperTerrain);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 200, 0, 360f);

		// Star
		PlanetAPI alaugon = system.initStar("tll_alaugon",
				"star_yellow",
				900f,
				500,
				6f,
				0.3f,
				2.0f);

		system.setLightColor(new Color(230, 230, 190));
		system.getLocation().set(location);

		// Asteroid belts and rings
		system.addAsteroidBelt(alaugon, 300, 2600, 1000, 160, 220);
		system.addRingBand(alaugon, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 14000, 200f, null, null);
		system.addRingBand(alaugon, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 14500, 200f, null, null);

		// Warning Beacon
		SectorEntityToken anchor = system.getHyperspaceAnchor();
		CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("tll_warning_beacon",
				"TLL Warning Beacon", "warning_beacon", TLL_factions.TELLASIAN_LEAGUE);
		beacon.setCircularOrbitPointingDown(anchor, 100, 300, 65f);
		Color glowColor = new Color(250, 55, 0, 255);
		Color pingColor = new Color(250, 55, 0, 255);
		Misc.setWarningBeaconColors(beacon, glowColor, pingColor);
		beacon.getMemoryWithoutUpdate().set("$tll_beacontag", true);
		beacon.setCustomDescriptionId("tll_warningbeacon");
		beacon.getMemoryWithoutUpdate().set("$tll_warningbeacontag", true);
		Misc.setDefenderOverride(beacon, new DefenderDataOverride("tll", 0.5f, 40, 60));

		// Relay and Buoy
		SectorEntityToken AlaugonRelay = system.addCustomEntity(null, "Comm Relay", "comm_relay", "tll");
		AlaugonRelay.setCircularOrbit(alaugon, 190, 7000, 280);

		SectorEntityToken AlaugonBuoy = system.addCustomEntity(null, "Nav Buoy", "nav_buoy", "tll");
		AlaugonBuoy.setCircularOrbit(alaugon, 310, 7000, 280);

		// Vosta (Main colony - Alpine planet)
		PlanetAPI vosta = system.addPlanet("tll_vosta", alaugon, "Vosta", "TLL_alpine", 50, 100, 9200, 330);
		vosta.setCustomDescriptionId("tll_vosta");
		vosta.setFaction("tll");
		vosta.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		vosta.getMemoryWithoutUpdate().set("$tll_vostatag", true);

		// Prevent random admin
		vosta.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		// Orbital Ring for Vosta
		SectorEntityToken VostaRing = system.addCustomEntity("vosta_ring", "Orbital Ring", "tll_vosta_ring", "tll");
		VostaRing.setCircularOrbit(vosta, 310, 0, 40);

		// Create market for Vosta
		MarketAPI vostaMarket = Global.getFactory().createMarket("tll_vosta", vosta.getName(), 6);
		vostaMarket.setFactionId("tll");
		vostaMarket.setPrimaryEntity(vosta);
		vostaMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		vostaMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		vostaMarket.setPlanetConditionMarketOnly(false);
		vostaMarket.addCondition(Conditions.POPULATION_6);
		vostaMarket.addCondition(Conditions.COLD);
		vostaMarket.addCondition(Conditions.HABITABLE);
		vostaMarket.addCondition(Conditions.ORE_ABUNDANT);
		vostaMarket.addCondition(Conditions.RARE_ORE_MODERATE);

		// Add industries
		vostaMarket.addIndustry(Industries.POPULATION);
		vostaMarket.addIndustry(Industries.MEGAPORT);
		vostaMarket.addIndustry(Industries.MINING);
		vostaMarket.addIndustry(Industries.REFINING);
		vostaMarket.addIndustry(Industries.WAYSTATION);
		vostaMarket.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
		vostaMarket.addIndustry(Industries.MILITARYBASE);
		vostaMarket.addIndustry(Industries.BATTLESTATION);

		// Set AI cores
		vostaMarket.getIndustry(Industries.ORBITALWORKS).setAICoreId(Commodities.ALPHA_CORE);
		vostaMarket.getIndustry(Industries.MILITARYBASE).setAICoreId(Commodities.BETA_CORE);
		vostaMarket.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);

		// Add submarkets
		vostaMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		vostaMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		vostaMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		vostaMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		vosta.setMarket(vostaMarket);
		Global.getSector().getEconomy().addMarket(vostaMarket, true);

		// Hades (Ice Giant - NOT A COLONY, just a planet with rings)
		PlanetAPI hades = system.addPlanet("tll_hades", alaugon, "Hades", "ice_giant", 50, 340, 6000, 450);
		hades.setCustomDescriptionId("tll_hades");
		hades.setFaction("tll");
		hades.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		hades.getMemoryWithoutUpdate().set("$tll_hades", true);
		// NO MARKET for Hades - it's an uninhabited ice giant

		// Rings for Hades
		system.addAsteroidBelt(hades, 300, 750, 300, 160, 220);
		system.addRingBand(hades, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 14000, 200f, null, null);
		system.addRingBand(hades, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 14500, 200f, null, null);

		// Amphitrite (Water moon of Hades - colony)
		PlanetAPI amphitrite = system.addPlanet("tll_amphitrite", hades, "Amphitrite", "water", 50, 75, 1200, 230);
		amphitrite.setCustomDescriptionId("tll_amphitrite");
		amphitrite.setFaction(TLL_factions.TELLASIAN_LEAGUE);
		amphitrite.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		amphitrite.getMemoryWithoutUpdate().set("$tll_amphitrite", true);

		// Prevent random admin
		amphitrite.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		// Create market for Amphitrite
		MarketAPI amphitriteMarket = Global.getFactory().createMarket("tll_amphitrite", amphitrite.getName(), 4);
		amphitriteMarket.setFactionId(TLL_factions.TELLASIAN_LEAGUE);
		amphitriteMarket.setPrimaryEntity(amphitrite);
		amphitriteMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		amphitriteMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		amphitriteMarket.setPlanetConditionMarketOnly(false);
		amphitriteMarket.addCondition(Conditions.POPULATION_4);
		amphitriteMarket.addCondition(Conditions.WATER_SURFACE);
		amphitriteMarket.addCondition(Conditions.HABITABLE);
		amphitriteMarket.addCondition(Conditions.RUINS_SCATTERED);
		amphitriteMarket.addCondition(Conditions.MILD_CLIMATE);

		// Add industries
		amphitriteMarket.addIndustry(Industries.POPULATION);
		amphitriteMarket.addIndustry(Industries.SPACEPORT);
		amphitriteMarket.addIndustry(Industries.AQUACULTURE);
		amphitriteMarket.addIndustry(Industries.TECHMINING);

		// Set AI cores
		amphitriteMarket.getIndustry(Industries.AQUACULTURE).setAICoreId(Commodities.BETA_CORE);
		amphitriteMarket.getIndustry(Industries.TECHMINING).setAICoreId(Commodities.GAMMA_CORE);

		// Add submarkets
		amphitriteMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		amphitriteMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		amphitriteMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		amphitriteMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		amphitrite.setMarket(amphitriteMarket);
		Global.getSector().getEconomy().addMarket(amphitriteMarket, true);

		// Amoxyl (Barren moon of Hades - NO COLONY, just a barren planet)
		PlanetAPI vbarren1 = system.addPlanet("tll_vbarren1", hades, "Amoxyl", "barren", 50, 60, 1600, 520);
		vbarren1.setCustomDescriptionId("tll_vbarren1");
		vbarren1.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		vbarren1.getMemoryWithoutUpdate().set("$tll_vbarren1", true);
		// NO MARKET for Amoxyl - it's uninhabited

		// Pyrite (Moon of Amoxyl - NO COLONY)
		PlanetAPI vbarren2 = system.addPlanet("tll_vbarren2", vbarren1, "Pyrite", "barren", 50, 45, 300, 100);
		vbarren2.setCustomDescriptionId("tll_vbarren2");
		vbarren2.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		vbarren2.getMemoryWithoutUpdate().set("$tll_vbarren2", true);
		// NO MARKET for Pyrite

		// Uside (Barren-bombarded - NO COLONY)
		PlanetAPI vbarren3 = system.addPlanet("tll_vbarren3", alaugon, "Uside", "barren-bombarded", 50, 40, 2600, 200);
		vbarren3.setCustomDescriptionId("tll_vbarren3");
		vbarren3.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		vbarren3.getMemoryWithoutUpdate().set("$tll_vbarren3", true);
		// NO MARKET for Uside

		// Inactive Gate
		SectorEntityToken deadgate2 = system.addCustomEntity("tll_deadgate", "Inactive Gate",
				"inactive_gate", "neutral");
		deadgate2.setCircularOrbitPointingDown(alaugon, 270, 4900, 175);
		deadgate2.setCustomDescriptionId("tll_deadgate2");
		deadgate2.setInteractionImage("illustrations", "dead_gate");
		deadgate2.getMemoryWithoutUpdate().set("$tll_deadgatetag2", true);

		// Debris Field 1 with derelict ships
		DebrisFieldTerrainPlugin.DebrisFieldParams params_alaugon_main = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				350f, 1.5f, 10000000f, 0f);
		params_alaugon_main.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
		params_alaugon_main.baseSalvageXP = 500;

		SectorEntityToken debrisalaugon_main1 = Misc.addDebrisField(system, params_alaugon_main, StarSystemGenerator.random);
		debrisalaugon_main1.setSensorProfile(1000f);
		debrisalaugon_main1.setDiscoverable(true);
		debrisalaugon_main1.setCircularOrbit(alaugon, 120f, 9800, 365f);
		debrisalaugon_main1.setId("alaugon_main_debrisBelt");

		// Add derelict ships to debris field
		addDerelict(system, debrisalaugon_main1, "onslaught_xiv_Elite", ShipRecoverySpecial.ShipCondition.WRECKED, 100f, false);
		addDerelict(system, debrisalaugon_main1, "tll_eagle_standard", ShipRecoverySpecial.ShipCondition.WRECKED, 130f, true);
		addDerelict(system, debrisalaugon_main1, "cerberus_d_pirates_Standard", ShipRecoverySpecial.ShipCondition.WRECKED, 300f, false);
		addDerelict(system, debrisalaugon_main1, "kite_pirates_Raider", ShipRecoverySpecial.ShipCondition.WRECKED, 270f, false);
		addDerelict(system, debrisalaugon_main1, "dominator_XIV_Elite", ShipRecoverySpecial.ShipCondition.WRECKED, 380f, false);

		// Debris Field 2
		DebrisFieldTerrainPlugin.DebrisFieldParams params_alaugon_main2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				350f, 1.5f, 10000000f, 0f);
		params_alaugon_main2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
		params_alaugon_main2.baseSalvageXP = 500;

		SectorEntityToken debrisalaugon_main2 = Misc.addDebrisField(system, params_alaugon_main2, StarSystemGenerator.random);
		debrisalaugon_main2.setSensorProfile(1000f);
		debrisalaugon_main2.setDiscoverable(true);
		debrisalaugon_main2.setCircularOrbit(alaugon, 120f, 2700, 365f);
		debrisalaugon_main2.setId("alaugon_main_debrisBelt2");
		addDerelict(system, debrisalaugon_main2, "hound_Starting", ShipRecoverySpecial.ShipCondition.WRECKED, 100f, true);

		// Generate jump points
		system.autogenerateHyperspaceJumpPoints(true, true, true);
	}

	void cleanup(StarSystemAPI system) {
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;
		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
	}

	private void addDerelict(StarSystemAPI system,
							 SectorEntityToken focus,
							 String variantId,
							 ShipRecoverySpecial.ShipCondition condition,
							 float orbitRadius,
							 boolean recoverable) {
		DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(
				new ShipRecoverySpecial.PerShipData(variantId, condition), false);
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);

		float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
		ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

		if (recoverable) {
			SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
	}
}
















