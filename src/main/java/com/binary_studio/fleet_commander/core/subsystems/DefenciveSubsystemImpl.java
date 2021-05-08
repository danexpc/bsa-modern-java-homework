package com.binary_studio.fleet_commander.core.subsystems;

import com.binary_studio.fleet_commander.core.actions.attack.AttackAction;
import com.binary_studio.fleet_commander.core.actions.defence.RegenerateAction;
import com.binary_studio.fleet_commander.core.common.PositiveInteger;
import com.binary_studio.fleet_commander.core.subsystems.contract.DefenciveSubsystem;

public final class DefenciveSubsystemImpl implements DefenciveSubsystem {

	private final static Integer MAX_REDUCTION_PERCENT = 95;

	private final String name;

	private final PositiveInteger powergridConsumption;

	private final PositiveInteger capacitorConsumption;

	private final PositiveInteger impactReductionPercent;

	private final PositiveInteger shieldRegeneration;

	private final PositiveInteger hullRegeneration;

	private DefenciveSubsystemImpl(String name, PositiveInteger powergridConsumption,
			PositiveInteger capacitorConsumption, PositiveInteger impactReductionPercent,
			PositiveInteger shieldRegeneration, PositiveInteger hullRegeneration) {
		this.name = name;
		this.powergridConsumption = powergridConsumption;
		this.capacitorConsumption = capacitorConsumption;
		this.impactReductionPercent = impactReductionPercent;
		this.shieldRegeneration = shieldRegeneration;
		this.hullRegeneration = hullRegeneration;

	}

	public static DefenciveSubsystemImpl construct(String name, PositiveInteger powergridConsumption,
			PositiveInteger capacitorConsumption, PositiveInteger impactReductionPercent,
			PositiveInteger shieldRegeneration, PositiveInteger hullRegeneration) throws IllegalArgumentException {
		if (name == null || "".equals(name.strip())) {
			throw new IllegalArgumentException("Name should be not null and not empty");
		}

		impactReductionPercent = PositiveInteger.of(impactReductionPercent.value() > MAX_REDUCTION_PERCENT
				? MAX_REDUCTION_PERCENT : impactReductionPercent.value());

		return new DefenciveSubsystemImpl(name, powergridConsumption, capacitorConsumption, impactReductionPercent,
				shieldRegeneration, hullRegeneration);
	}

	@Override
	public PositiveInteger getPowerGridConsumption() {
		return this.powergridConsumption;
	}

	@Override
	public PositiveInteger getCapacitorConsumption() {
		return this.capacitorConsumption;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public AttackAction reduceDamage(AttackAction incomingDamage) {
		return new AttackAction(computeReducedDamage(incomingDamage.damage), incomingDamage.attacker,
				incomingDamage.target, incomingDamage.weapon);
	}

	@Override
	public RegenerateAction regenerate() {
		return new RegenerateAction(this.shieldRegeneration, this.hullRegeneration);
	}

	private PositiveInteger computeReducedDamage(PositiveInteger incomingDamage) {
		return PositiveInteger.of(
				(int) Math.ceil(incomingDamage.value() * (1 - ((double) this.impactReductionPercent.value() / 100))));
	}

}
