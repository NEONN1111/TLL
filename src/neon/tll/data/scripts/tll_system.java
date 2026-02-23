package neon.tll.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import org.lwjgl.util.vector.Vector2f;
import neon.tll.data.scripts.util.TLL_factions;

import java.awt.*;

public class tll_system implements SectorGeneratorPlugin {
	public static String NOT_RANDOM_MISSION_TARGET = "$not_random_mission_target";

	@Override
	public void generate(SectorAPI sector) {
		Vector2f location = new Vector2f(-29697, 13959);

		StarSystemAPI system = Global.getSector().createStarSystem("Alphard");
		system.setName("Alphard");
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
		PlanetAPI alphard = system.initStar("tll_alphard",
				"star_blue_supergiant",
				1500f,
				700,
				10f,
				0.7f,
				3.0f);

		system.setLightColor(new Color(20, 190, 200));
		system.getLocation().set(location);

		// Asteroid belts
		system.addAsteroidBelt(alphard, 300, 20000, 1000, 160, 220);
		system.addRingBand(alphard, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 19000, 200f, null, null);
		system.addRingBand(alphard, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 19000, 200f, null, null);

		// Pyyrhus (Gas Giant with colony)
		PlanetAPI pyyhrus = system.addPlanet("tll_pyyrhus", alphard, "Pyyrhus", "gas_giant", 50, 300, 9000, 400);
		pyyhrus.setCustomDescriptionId("tll_pyyhrus");
		pyyhrus.setFaction("tll");
		pyyhrus.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		pyyhrus.getMemoryWithoutUpdate().set("$tll_pyyrhustag", true);

		// Prevent random admin from being assigned
		pyyhrus.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		// Magnetic field for Pyyrhus
		SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(150f,
						320,
						pyyhrus,
						309f,
						710f,
						new Color(130, 60, 150, 130),
						4f,
						new Color(130, 60, 150, 130),
						new Color(150, 30, 120, 150),
						new Color(10, 190, 200, 190),
						new Color(100, 200, 150, 240),
						new Color(100, 200, 130, 255),
						new Color(75, 90, 160, 255),
						new Color(100, 200, 200, 240)));
		field.setCircularOrbit(pyyhrus, 0, 0, 75);

		// Create market for Pyyrhus
		MarketAPI pyyhrusMarket = Global.getFactory().createMarket("tll_pyyrhus", pyyhrus.getName(), 4);
		pyyhrusMarket.setFactionId("tll");
		pyyhrusMarket.setPrimaryEntity(pyyhrus);
		pyyhrusMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		pyyhrusMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		pyyhrusMarket.setPlanetConditionMarketOnly(false);
		pyyhrusMarket.addCondition(Conditions.POPULATION_4);
		pyyhrusMarket.addCondition(Conditions.HIGH_GRAVITY);
		pyyhrusMarket.addCondition(Conditions.DENSE_ATMOSPHERE);
		pyyhrusMarket.addCondition(Conditions.EXTREME_WEATHER);
		pyyhrusMarket.addCondition(Conditions.VOLATILES_DIFFUSE);

		// Add industries
		pyyhrusMarket.addIndustry(Industries.POPULATION);
		pyyhrusMarket.addIndustry(Industries.FUELPROD);
		pyyhrusMarket.addIndustry(Industries.MINING);
		pyyhrusMarket.addIndustry(Industries.SPACEPORT);

		pyyhrusMarket.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.BETA_CORE);
		pyyhrusMarket.getIndustry(Industries.POPULATION).setAICoreId(Commodities.ALPHA_CORE);
		pyyhrusMarket.getIndustry(Industries.FUELPROD).setAICoreId(Commodities.BETA_CORE);
		// Add submarkets
		pyyhrusMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		pyyhrusMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		pyyhrusMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		pyyhrusMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		pyyhrus.setMarket(pyyhrusMarket);
		Global.getSector().getEconomy().addMarket(pyyhrusMarket, true);

		// Vokha (Moon of Pyyrhus with colony)
		PlanetAPI vokha = system.addPlanet("tll_vokha", pyyhrus, "Vokha", "barren-desert", 50, 60, 720, 90);
		vokha.setCustomDescriptionId("tll_vokha");
		vokha.setFaction("tll");
		vokha.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		vokha.getMemoryWithoutUpdate().set("$tll_vokhatag", true);

		// Prevent random admin from being assigned
		vokha.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		// Magnetic field for Vokha
		SectorEntityToken field2 = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(150f,
						320,
						vokha,
						61f,
						200f,
						new Color(130, 60, 150, 130),
						2f,
						new Color(130, 60, 150, 130),
						new Color(150, 30, 120, 150),
						new Color(10, 190, 200, 190),
						new Color(100, 200, 150, 240),
						new Color(100, 200, 130, 255),
						new Color(75, 90, 160, 255),
						new Color(100, 200, 200, 240)));
		field2.setCircularOrbit(pyyhrus, 0, 0, 75);

		// Create market for Vokha
		MarketAPI vokhaMarket = Global.getFactory().createMarket("tll_vokha", vokha.getName(), 5);
		vokhaMarket.setFactionId("tll");
		vokhaMarket.setPrimaryEntity(vokha);
		vokhaMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		vokhaMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		vokhaMarket.setPlanetConditionMarketOnly(false);
		vokhaMarket.addCondition(Conditions.POPULATION_5);
		vokhaMarket.addCondition(Conditions.THIN_ATMOSPHERE);
		vokhaMarket.addCondition(Conditions.HOT);
		vokhaMarket.addCondition(Conditions.DARK);
		vokhaMarket.addCondition(Conditions.RUINS_SCATTERED);
		vokhaMarket.addCondition(Conditions.RARE_ORE_SPARSE);
		vokhaMarket.addCondition(Conditions.ORE_MODERATE);

		// Add industries
		vokhaMarket.addIndustry(Industries.POPULATION);
		vokhaMarket.addIndustry(Industries.REFINING);
		vokhaMarket.addIndustry(Industries.HEAVYINDUSTRY);
		vokhaMarket.addIndustry(Industries.MINING);
		vokhaMarket.addIndustry(Industries.SPACEPORT);

		// Add submarkets
		vokhaMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		vokhaMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		vokhaMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		vokhaMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		vokha.setMarket(vokhaMarket);
		Global.getSector().getEconomy().addMarket(vokhaMarket, true);

		// Warning Beacon
		SectorEntityToken anchor = system.getHyperspaceAnchor();
		CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("tll_warning_beacon",
				"TLL Warning Beacon", "warning_beacon", TLL_factions.TELLASIAN_LEAGUE);
		beacon.setCircularOrbitPointingDown(anchor, 100, 300, 65f);
		beacon.setCustomDescriptionId("tll_warningbeacon");
		Misc.setWarningBeaconColors(beacon, new Color(250,55,0,255), new Color(250,55,0,255));
		beacon.getMemoryWithoutUpdate().set("$tll_beacontag", true);
		beacon.getMemoryWithoutUpdate().set("$tll_warningbeacontag", true);
		Misc.setDefenderOverride(beacon, new DefenderDataOverride("tll", 0.5f, 40, 60));

		// Relay and Buoy
		SectorEntityToken VigilRelay = system.addCustomEntity(null, "Comm Relay", "comm_relay", "tll");
		VigilRelay.setCircularOrbit(alphard, 190, 7000, 280);

		SectorEntityToken VigilBuoy = system.addCustomEntity(null, "Nav Buoy", "nav_buoy", "tll");
		VigilBuoy.setCircularOrbit(alphard, 310, 7000, 280);

		// Brigid (Barren planet with colony)
		PlanetAPI brigid = system.addPlanet("tll_brigid", alphard, "Brigid", "barren-bombarded", 50, 80, 3800, 90);
		brigid.setCustomDescriptionId("tll_brigid");
		brigid.setFaction("tll");
		brigid.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		brigid.getMemoryWithoutUpdate().set("$tll_brigidtag", true);

		// Prevent random admin from being assigned
		brigid.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		Misc.setDefenderOverride(brigid, new DefenderDataOverride("tll", 1f, 150, 300));

		// Create market for Brigid
		MarketAPI brigidMarket = Global.getFactory().createMarket("tll_brigid", brigid.getName(), 4);
		brigidMarket.setFactionId("tll");
		brigidMarket.setPrimaryEntity(brigid);
		brigidMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		brigidMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		brigidMarket.setPlanetConditionMarketOnly(false);
		brigidMarket.addCondition(Conditions.POPULATION_4);
		brigidMarket.addCondition(Conditions.NO_ATMOSPHERE);
		brigidMarket.addCondition(Conditions.HIGH_GRAVITY);
		brigidMarket.addCondition(Conditions.COLD);
		brigidMarket.addCondition(Conditions.RARE_ORE_RICH);
		brigidMarket.addCondition(Conditions.ORE_RICH);

		// Add industries
		brigidMarket.addIndustry(Industries.POPULATION);
		brigidMarket.addIndustry(Industries.MINING);
		brigidMarket.addIndustry(Industries.REFINING);
		brigidMarket.addIndustry(Industries.SPACEPORT);

		// Add submarkets
		brigidMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		brigidMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		brigidMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		brigidMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		brigid.setMarket(brigidMarket);
		Global.getSector().getEconomy().addMarket(brigidMarket, true);

		// Tellas (Main colony)
		PlanetAPI habplanet = system.addPlanet("tll_tellas", alphard, "Tellas", "terran", 50, 75, 5900, 90);
		habplanet.setCustomDescriptionId("tll_tellas");
		habplanet.setFaction("tll");
		habplanet.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		habplanet.getMemoryWithoutUpdate().set("$tll_tellastag", true);

		// Prevent random admin from being assigned
		habplanet.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		// Stellar Mirrors
		SectorEntityToken tellas_mirror1 = system.addCustomEntity("tellas_mirror1", "Tellas Stellar Mirror",
				"stellar_mirror", "tll");
		tellas_mirror1.setCircularOrbitPointingDown(habplanet, 0, 220, 40);
		tellas_mirror1.setCustomDescriptionId("stellar_mirror");

		SectorEntityToken tellas_mirror2 = system.addCustomEntity("tellas_mirror2", "Tellas Stellar Mirror",
				"stellar_mirror", "tll");
		tellas_mirror2.setCircularOrbitPointingDown(habplanet, 120, 220, 40);
		tellas_mirror2.setCustomDescriptionId("stellar_mirror");

		SectorEntityToken tellas_mirror3 = system.addCustomEntity("tellas_mirror3", "Tellas Stellar Mirror",
				"stellar_mirror", "tll");
		tellas_mirror3.setCircularOrbitPointingDown(habplanet, 240, 220, 40);
		tellas_mirror3.setCustomDescriptionId("stellar_mirror");

		// Market for Tellas
		MarketAPI tellasMarket = Global.getFactory().createMarket("tll_tellas", habplanet.getName(), 7);
		tellasMarket.setFactionId("tll");
		tellasMarket.setPrimaryEntity(habplanet);
		tellasMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		tellasMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		tellasMarket.setPlanetConditionMarketOnly(false);
		tellasMarket.addCondition(Conditions.POPULATION_7);
		tellasMarket.addCondition(Conditions.MILD_CLIMATE);
		tellasMarket.addCondition(Conditions.HABITABLE);
		tellasMarket.addCondition(Conditions.ORE_SPARSE);
		tellasMarket.addCondition(Conditions.SOLAR_ARRAY);
		tellasMarket.addCondition(Conditions.FARMLAND_BOUNTIFUL);

		// Add industries
		tellasMarket.addIndustry(Industries.POPULATION);
		tellasMarket.addIndustry(Industries.FARMING);
		tellasMarket.addIndustry(Industries.LIGHTINDUSTRY);
		tellasMarket.addIndustry(Industries.WAYSTATION);
		tellasMarket.addIndustry(Industries.COMMERCE);
		tellasMarket.addIndustry(Industries.STARFORTRESS_HIGH);
		tellasMarket.addIndustry(Industries.HIGHCOMMAND);
		tellasMarket.addIndustry(Industries.MEGAPORT);

		tellasMarket.getIndustry(Industries.FARMING).setAICoreId(Commodities.ALPHA_CORE);
		tellasMarket.getIndustry(Industries.HIGHCOMMAND).setAICoreId(Commodities.ALPHA_CORE);
		tellasMarket.getIndustry(Industries.LIGHTINDUSTRY).setAICoreId(Commodities.BETA_CORE);
		tellasMarket.getIndustry(Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);
		tellasMarket.getIndustry(Industries.WAYSTATION).setAICoreId(Commodities.BETA_CORE);
		tellasMarket.getIndustry(Industries.MEGAPORT).setAICoreId(Commodities.BETA_CORE);
		tellasMarket.getIndustry(Industries.COMMERCE).setAICoreId(Commodities.ALPHA_CORE);



		// Add submarkets
		tellasMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		tellasMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		tellasMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		tellasMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		habplanet.setMarket(tellasMarket);
		Global.getSector().getEconomy().addMarket(tellasMarket, true);

		// Nisse (Cryovolcanic with colony)
		PlanetAPI nisse = system.addPlanet("tll_nisse", alphard, "Nisse", "cryovolcanic", 50, 50, 14000, 450);
		nisse.setCustomDescriptionId("tll_nisse");
		nisse.setFaction("tll");
		nisse.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		nisse.getMemoryWithoutUpdate().set("$tll_nissetag", true);

		// Prevent random admin from being assigned
		nisse.getMemoryWithoutUpdate().set("$no_automatic_admin", true);

		Misc.setDefenderOverride(nisse, new DefenderDataOverride("tll", 1f, 150, 300));

		// Create market for Nisse
		MarketAPI nisseMarket = Global.getFactory().createMarket("tll_nisse", nisse.getName(), 4);
		nisseMarket.setFactionId("tll");
		nisseMarket.setPrimaryEntity(nisse);
		nisseMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		nisseMarket.getTariff().modifyFlat("generator", 0.3f);

		// Add conditions
		nisseMarket.setPlanetConditionMarketOnly(false);
		nisseMarket.addCondition(Conditions.POPULATION_4);
		nisseMarket.addCondition(Conditions.VERY_COLD);
		nisseMarket.addCondition(Conditions.THIN_ATMOSPHERE);
		nisseMarket.addCondition(Conditions.VOLATILES_DIFFUSE);
		nisseMarket.addCondition(Conditions.TECTONIC_ACTIVITY);
		nisseMarket.addCondition(Conditions.RARE_ORE_SPARSE);
		nisseMarket.addCondition(Conditions.RUINS_SCATTERED);

		// Add industries
		nisseMarket.addIndustry(Industries.POPULATION);
		nisseMarket.addIndustry(Industries.SPACEPORT);
		nisseMarket.addIndustry(Industries.MINING);
		nisseMarket.addIndustry(Industries.REFINING);

		// Add submarkets
		nisseMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
		nisseMarket.addSubmarket(Submarkets.GENERIC_MILITARY);
		nisseMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
		nisseMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);

		nisse.setMarket(nisseMarket);
		Global.getSector().getEconomy().addMarket(nisseMarket, true);

		// Inactive Gate
		SectorEntityToken deadgate = system.addCustomEntity("tll_deadgate", "Inactive Gate",
				"inactive_gate", "neutral");
		deadgate.setCircularOrbitPointingDown(alphard, 270, 4900, 175);
		deadgate.setInteractionImage("illustrations", "dead_gate");
		deadgate.getMemoryWithoutUpdate().set("$tll_deadgatetag", true);

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
}








