package com.binary_studio.fleet_commander.core.subsystems;

import com.binary_studio.fleet_commander.core.common.Attackable;
import com.binary_studio.fleet_commander.core.common.PositiveInteger;
import com.binary_studio.fleet_commander.core.subsystems.contract.AttackSubsystem;

import static java.lang.Math.min;

public final class AttackSubsystemImpl implements AttackSubsystem {

	private final String name;
	private final PositiveInteger powergridRequirements;
	private final PositiveInteger capacitorConsumption;
	private final PositiveInteger optimalSpeed;
	private final PositiveInteger optimalSize;
	private final PositiveInteger baseDamage;

	private AttackSubsystemImpl(String name, PositiveInteger powergridRequirements, PositiveInteger capacitorConsumption,
								PositiveInteger optimalSpeed, PositiveInteger optimalSize, PositiveInteger baseDamage) {
		this.name = name;
		this.powergridRequirements = powergridRequirements;
		this.capacitorConsumption = capacitorConsumption;
		this.optimalSpeed = optimalSpeed;
		this.optimalSize = optimalSize;
		this.baseDamage = baseDamage;
	}

	public static AttackSubsystemImpl construct(String name, PositiveInteger powergridRequirements,
			PositiveInteger capacitorConsumption, PositiveInteger optimalSpeed, PositiveInteger optimalSize,
			PositiveInteger baseDamage) throws IllegalArgumentException {
		if (name == null || "".equals(name.strip())) {
			throw new IllegalArgumentException("Name should be not null and not empty");
		}

		return new AttackSubsystemImpl(name, powergridRequirements, capacitorConsumption, optimalSpeed,
				optimalSize, baseDamage);
	}

	@Override
	public PositiveInteger getPowerGridConsumption() {
		return this.powergridRequirements;
	}

	@Override
	public PositiveInteger getCapacitorConsumption() {
		return this.capacitorConsumption;
	}

	@Override
	public PositiveInteger attack(Attackable target) {
		double sizeReductionModifier = target.getSize().value() >= optimalSize.value() ? 1
				: (double)target.getSize().value() / this.optimalSize.value();

		double speedReductionModifier = target.getCurrentSpeed().value() <= optimalSpeed.value() ? 1
				: (double) optimalSpeed.value() / (2 * target.getCurrentSpeed().value());

		return PositiveInteger.of((int) Math.ceil(baseDamage.value() *
				min(sizeReductionModifier, speedReductionModifier)));
	}

	@Override
	public String getName() {
		return this.name;
	}

}
