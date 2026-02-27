package neon.tll.data.shipsystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.EnumSet;
import java.util.Random;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.LidarArrayStats;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.vector.Vector2f;

public class LidarArrayStatsTLL extends BaseShipSystemScript {

	public static String LIDAR_WINDUP = "lidar_windup";

	public static Color WEAPON_GLOW = new Color(255,50,50,155);

	// Malfunction configuration - inspired by raider drive
	public static float MALFUNCTION_CHANCE = 0.33f; // 33% chance per weapon to malfunction
	public static float MALFUNCTION_DURATION = 2f; // 2 seconds malfunction duration

	public static float RANGE_BONUS = 100; // Changed from +100% to -50% range when active
	public static float PASSIVE_RANGE_BONUS = 15f;
	public static float ROF_BONUS = 2f;
	public static float RECOIL_BONUS = 75f;
	public static float PROJECTILE_SPEED_BONUS = 50f;
	public static float SPEED_BONUS = 150f;
	public static float TURN_BONUS = 50f;
	public static float FLUX_REDUCTION = 50f;

	// Self-damage configuration - from VentingOverdrive
	public static float SELF_DAMAGE_FLUX_MULT = 0.20f; // Multiplier for flux capacity as damage
	public static float SELF_DAMAGE_MIN_HULL = 0.2f; // Minimum hull level to take damage (20%)

	// Key for visual effects
	public static Object KEY_SHIP = new Object();

	public static class LidarDishData {
		public float turnDir;
		public float turnRate;
		public float angle;
		public float phase;
		public float count;
		public WeaponAPI w;
	}

	// Malfunction timer data class - similar to raider drive
	public static class WeaponMalfunctionData {
		IntervalUtil interval = new IntervalUtil(100f, 100f);
		public WeaponMalfunctionData(float interval) {
			this.interval = new IntervalUtil(interval, interval);
		}
	}

	protected List<LidarDishData> dishData = new ArrayList<>();
	protected boolean needsUnapply = false;
	protected boolean playedWindup = false;
	protected boolean hasAppliedMalfunction = false; // Track if we've already applied malfunction for this deactivation
	protected Random random = new Random();

	protected boolean inited = false;

	float massBeforeActivation = 0f;
	boolean masscheck = true;

	public void init(ShipAPI ship) {
		if (inited) return;
		inited = true;

		needsUnapply = true;

		int turnDir = 1;
		float index = 0f;
		float count = 0f;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR)) {
				count++;
			}
		}
		List<WeaponAPI> lidar = new ArrayList<WeaponAPI>();
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR)) {
				lidar.add(w);
			}
		}
		Collections.sort(lidar, new Comparator<WeaponAPI>() {
			public int compare(WeaponAPI o1, WeaponAPI o2) {
				return (int) Math.signum(o1.getSlot().getLocation().x - o2.getSlot().getLocation().x);
			}
		});
		for (WeaponAPI w : lidar) {
			if (w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR)) {
				w.setSuspendAutomaticTurning(true);
				LidarDishData data = new LidarDishData();
				data.turnDir = Math.signum(turnDir);
				data.turnRate = 0.5f;
				data.turnRate = 0.1f;
				data.w = w;
				data.angle = 0f;
				data.phase = index / count;
				data.count = count;
				dishData.add(data);
				turnDir = -turnDir;
				index++;
			}
		}
	}

	public void rotateLidarDishes(boolean active, float effectLevel) {
		float amount = Global.getCombatEngine().getElapsedInLastFrame();

		float turnRateMult = 1f;
		if (active) {
			turnRateMult = 20f;
		}
		for (LidarDishData data : dishData) {
			float arc = data.w.getArc();
			float useTurnDir = data.turnDir;
			if (active) {
				useTurnDir = Misc.getClosestTurnDirection(data.angle, 0f);
			}
			float delta = useTurnDir * amount * data.turnRate * turnRateMult * arc;
			if (active && effectLevel > 0f && Math.abs(data.angle) < Math.abs(delta * 1.5f)) {
				data.angle = 0f;
			} else {
				data.angle += delta;
				data.phase += 1f * amount;
				if (arc < 360f) {
					if (data.angle > arc/2f && data.turnDir > 0f) {
						data.angle = arc/2f;
						data.turnDir = -1f;
					}
					if (data.angle < -arc/2f && data.turnDir < 0f) {
						data.angle = -arc/2f;
						data.turnDir = 1f;
					}
				} else {
					data.angle = data.angle % 360f;
				}
			}

			float facing = data.angle + data.w.getArcFacing() + data.w.getShip().getFacing();
			data.w.setFacing(facing);
			data.w.updateBeamFromPoints();
		}
	}

	// Apply self-damage during system activation - from VentingOverdrive
	protected void applySelfDamage(ShipAPI ship, float amount, float effectLevel) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return;

		// Only apply damage if hull is above the threshold (from VentingOverdrive)
		if (ship.getHullLevel() >= SELF_DAMAGE_MIN_HULL) {
			// Scale damage by effect level so it ramps up with system strength
			float scaledDamage = amount * SELF_DAMAGE_FLUX_MULT * ship.getVariant().getHullSpec().getFluxCapacity() * effectLevel;

			// Find a random point on the ship's bounds (from VentingOverdrive)
			Vector2f point = new Vector2f(ship.getLocation());
			point.x += ship.getCollisionRadius() * ((float) Math.random() * 2f - 1f);
			point.y += ship.getCollisionRadius() * ((float) Math.random() * 2f - 1f);

			if (!CollisionUtils.isPointWithinBounds(point, ship)) {
				point = CollisionUtils.getNearestPointOnBounds(point, ship);
			}

			// Apply damage (OTHER type for non-standard damage, matching VentingOverdrive)
			engine.applyDamage(ship, point, scaledDamage, DamageType.OTHER, 0f, true, false, null);
		}
	}

	// Update malfunction timers - similar to raider drive approach
	protected void updateMalfunctionTimers(ShipAPI ship, float amount) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return;

		String key = "lidar_malfunction" + "_" + ship.getId();
		WeaponMalfunctionData data = (WeaponMalfunctionData) engine.getCustomData().get(key);

		if (data == null) return;

		data.interval.advance(amount);

		// Check if malfunction period has elapsed
		if (data.interval.intervalElapsed()) {
			// Repair all weapons
			for (WeaponAPI weapon : ship.getAllWeapons()) {
				if (weapon.getSlot().isSystemSlot()) continue;
				weapon.repair();
			}
			// Remove the data
			engine.getCustomData().remove(key);
			ship.syncWeaponDecalsWithArmorDamage();
		}
		// No visual effects during malfunction - glow effect removed
	}

	// Apply malfunction on system deactivation - inspired by raider drive
	protected void applyMalfunction(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return;

		String key = "lidar_malfunction" + "_" + ship.getId();
		WeaponMalfunctionData data = (WeaponMalfunctionData) engine.getCustomData().get(key);

		if (data == null) {
			data = new WeaponMalfunctionData(MALFUNCTION_DURATION);
			engine.getCustomData().put(key, data);
		}

		// Apply malfunction to eligible weapons
		for (WeaponAPI weapon : ship.getAllWeapons()) {
			// Skip system slots, decorative weapons, and missiles (optional)
			if (weapon.getSlot().isSystemSlot()) continue;
			if (weapon.isDecorative()) continue;
			if (weapon.getType() == WeaponType.MISSILE) continue; // Decide if missiles can malfunction

			// Random chance to malfunction
			if (random.nextFloat() < MALFUNCTION_CHANCE) {
				weapon.disable();
				// No glow effect applied
			}
		}

		// Reset the interval
		data.interval.setElapsed(0f);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI)stats.getEntity();
		if (ship == null || ship.isHulk()) {
			if (needsUnapply) {
				unmodify(id, stats);
				// Clear all weapon glows when ship is destroyed
				for (WeaponAPI w : ship.getAllWeapons()) {
					w.setGlowAmount(0, null);
				}
				needsUnapply = false;
			}
			return;
		}

		init(ship);

		// Store mass before activation
		if (masscheck && state != State.IDLE) {
			massBeforeActivation = ship.getMass();
			masscheck = false;
		}

		boolean active = state == State.IN || state == State.ACTIVE;

		rotateLidarDishes(active, effectLevel);

		// Handle OUT state separately
		if (state == State.OUT) {
			stats.getMaxSpeed().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
			stats.getAcceleration().unmodifyPercent(id);
			stats.getDeceleration().unmodifyPercent(id);
			stats.getTurnAcceleration().unmodifyFlat(id);
			stats.getTurnAcceleration().unmodifyPercent(id);
			ship.setMass(massBeforeActivation);

			// Apply malfunction when system is deactivating - only once per deactivation
			if (!hasAppliedMalfunction) {
				applyMalfunction(ship);
				hasAppliedMalfunction = true;
			}
		}
		// Only apply movement bonuses when IN or ACTIVE
		else if (active) {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 15f * effectLevel);
			stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
			stats.getAcceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
			stats.getDeceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f * effectLevel);

			// Reset malfunction flag when system becomes active again
			hasAppliedMalfunction = false;
		}

		if (active) {
			modify(id, stats, effectLevel);
			needsUnapply = true;

			// Apply active visual effects
			float effectStrength = effectLevel * 0.5f;

			// Subtle weapon glow for all weapons
			ship.setWeaponGlow(effectStrength * 0.7f, WEAPON_GLOW, EnumSet.of(WeaponType.BALLISTIC, WeaponType.ENERGY));

			// Subtle engine effects
			ship.getEngineController().fadeToOtherColor(KEY_SHIP, new Color(255,100,100,150), new Color(0,0,0,0), effectStrength * 0.5f, 0.5f * effectStrength);
			ship.getEngineController().extendFlame(KEY_SHIP, 1f * effectStrength, 0f, 0f);

			// Set per-weapon glow for active weapons (for range calculation and extra visual)
			float lidarRange = 300;
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (!w.isDecorative() && w.getSlot().isHardpoint() && !w.isBeam() && w.getSlot().isTurret() &&
						(w.getType() == WeaponType.BALLISTIC || w.getType() == WeaponType.ENERGY || w.getType() == WeaponType.MISSILE)) {
					lidarRange = Math.max(lidarRange, w.getRange());
					w.setGlowAmount(effectLevel * 0.8f, WEAPON_GLOW);
				}
			}
			lidarRange += 50f;
			stats.getBeamWeaponRangeBonus().modifyFlat("lidararray_tll", lidarRange);

			// Apply self-damage during active and IN states
			float amount = Global.getCombatEngine().getElapsedInLastFrame();
			applySelfDamage(ship, amount, effectLevel);

		} else {
			if (needsUnapply) {
				unmodify(id, stats);
				// Clear all weapon glows when system is not active
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.getSlot().isSystemSlot()) continue;
					w.setGlowAmount(0, null);
				}
				needsUnapply = false;
			}

			// Update malfunction timers during OUT and inactive states
			float amount = Global.getCombatEngine().getElapsedInLastFrame();
			updateMalfunctionTimers(ship, amount);
		}

		if (!active) return;

		// Force no fire for non-lidar weapons during windup
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getSlot().isSystemSlot()) continue;
			if (w.getType() == WeaponType.MISSILE) continue;
			if (state == State.IN) {
				if (!(w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR))) {
					w.setForceNoFireOneFrame(true);
				}
			} else {
				if (!w.isDecorative() && w.getSlot().isHardpoint() && !w.isBeam() && w.getSlot().isTurret() &&
						(w.getType() == WeaponType.BALLISTIC || w.getType() == WeaponType.ENERGY || w.getType() == WeaponType.MISSILE)) {
					w.setForceNoFireOneFrame(true);
				}
			}
		}

		// Lidar dish firing logic
		float fireThreshold = 0.25f / 3.25f;
		fireThreshold += 0.02f;
		for (LidarDishData data : dishData) {
			boolean skip = data.phase % 1f > 1f / data.count;
			skip = false;
			if (skip) continue;
			if (data.w.isDecorative() && data.w.getSpec().hasTag(Tags.LIDAR)) {
				if (state == State.IN && Math.abs(data.angle) < 5f && effectLevel >= fireThreshold) {
					data.w.setForceFireOneFrame(true);
				}
			}
		}

		// Play windup sound
		if (((state == State.IN && effectLevel > 0.67f) || state == State.ACTIVE) && !playedWindup) {
			Global.getSoundPlayer().playSound(LIDAR_WINDUP, 1f, 1f, ship.getLocation(), ship.getVelocity());
			playedWindup = true;
		}
	}


	protected void modify(String id, MutableShipStatsAPI stats, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		// Apply range reduction instead of bonus (negative percent)
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getEnergyRoFMult().modifyMult(id, mult);

		// Add flux reduction for weapons
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION / 100f) * effectLevel);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION / 100f) * effectLevel);

		stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilDecayMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));

		stats.getBallisticProjectileSpeedMult().modifyPercent(id, PROJECTILE_SPEED_BONUS);
		stats.getEnergyProjectileSpeedMult().modifyPercent(id, PROJECTILE_SPEED_BONUS);
	}

	protected void unmodify(String id, MutableShipStatsAPI stats) {
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, PASSIVE_RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, PASSIVE_RANGE_BONUS);

		stats.getBallisticRoFMult().unmodifyMult(id);
		stats.getEnergyRoFMult().unmodifyMult(id);

		// Unapply flux reduction
		stats.getBallisticWeaponFluxCostMod().unmodifyMult(id);
		stats.getEnergyWeaponFluxCostMod().unmodifyMult(id);

		stats.getMaxRecoilMult().unmodifyMult(id);
		stats.getRecoilPerShotMult().unmodifyMult(id);
		stats.getRecoilDecayMult().unmodifyMult(id);

		stats.getBallisticProjectileSpeedMult().unmodifyPercent(id);
		stats.getEnergyProjectileSpeedMult().unmodifyPercent(id);

		// Unapply speed and maneuverability bonuses
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);

		playedWindup = false;
		masscheck = true;
		hasAppliedMalfunction = false;

		// Clean up malfunction data when fully unapplied
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine != null) {
			ShipAPI ship = (ShipAPI)stats.getEntity();
			if (ship != null) {
				String key = "lidar_malfunction" + "_" + ship.getId();
				engine.getCustomData().remove(key);
			}
		}
	}


	public void unapply(MutableShipStatsAPI stats, String id) {
		// never called due to runScriptWhileIdle:true in the .system file
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (state == State.IDLE || state == State.COOLDOWN) {
			if (index == 3) {
				return new StatusData("weapon range +" + (int) PASSIVE_RANGE_BONUS + "%", false);
			}
		}
		if (effectLevel <= 0f) return null;

		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 3) {
			// Changed to show range reduction instead of bonus
			return new StatusData("weapon range " + (int) Math.abs(RANGE_BONUS) + "%", false);
		}
		if (index == 2) {
			return new StatusData("rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("weapon recoil -" + (int) RECOIL_BONUS + "%", false);
		}
		if (index == 0 && PROJECTILE_SPEED_BONUS > 0) {
			return new StatusData("projectile speed +" + (int) PROJECTILE_SPEED_BONUS + "%", false);
		}
		return null;
	}

	// Add status data for self-damage warning
	@Override
	public String getDisplayNameOverride(State state, float effectLevel) {
		if (state == State.IDLE || state == State.COOLDOWN) {
			return "lidar array - passive";
		}
		if (state == State.IN || state == State.ACTIVE) {
			return "lidar array - active (self-damage)";
		}
		return null;
	}

	public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) (SELF_DAMAGE_FLUX_MULT * 100) + "%";
		if (index == 1) return "" + (int) (SELF_DAMAGE_MIN_HULL * 100) + "%";
		return null;
	}
}