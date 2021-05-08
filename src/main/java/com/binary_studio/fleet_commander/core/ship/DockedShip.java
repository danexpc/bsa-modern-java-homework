package com.binary_studio.fleet_commander.core.ship;

import com.binary_studio.fleet_commander.core.common.PositiveInteger;
import com.binary_studio.fleet_commander.core.exceptions.InsufficientPowergridException;
import com.binary_studio.fleet_commander.core.exceptions.NotAllSubsystemsFitted;
import com.binary_studio.fleet_commander.core.ship.contract.ModularVessel;
import com.binary_studio.fleet_commander.core.subsystems.contract.AttackSubsystem;
import com.binary_studio.fleet_commander.core.subsystems.contract.DefenciveSubsystem;

public final class DockedShip implements ModularVessel {

	private final String name;

	private final PositiveInteger shieldHP;

	private final PositiveInteger hullHP;

	private PositiveInteger powergridOutput;

	private final PositiveInteger capacitorAmount;

	private final PositiveInteger capacitorRechargeRate;

	private final PositiveInteger speed;

	private final PositiveInteger size;

	private AttackSubsystem attackSubsystem;

	private DefenciveSubsystem defenciveSubsystem;

	private DockedShip(String name, PositiveInteger shieldHP, PositiveInteger hullHP, PositiveInteger powergridOutput,
			PositiveInteger capacitorAmount, PositiveInteger capacitorRechargeRate, PositiveInteger speed,
			PositiveInteger size) {
		this.name = name;
		this.shieldHP = shieldHP;
		this.hullHP = hullHP;
		this.powergridOutput = powergridOutput;
		this.capacitorAmount = capacitorAmount;
		this.capacitorRechargeRate = capacitorRechargeRate;
		this.speed = speed;
		this.size = size;
	}

	public static DockedShip construct(String name, PositiveInteger shieldHP, PositiveInteger hullHP,
			PositiveInteger powergridOutput, PositiveInteger capacitorAmount, PositiveInteger capacitorRechargeRate,
			PositiveInteger speed, PositiveInteger size) {
		if (name == null || "".equals(name.strip())) {
			throw new IllegalArgumentException("Name should be not null and not empty");
		}
		return new DockedShip(name, shieldHP, hullHP, powergridOutput, capacitorAmount, capacitorRechargeRate, speed,
				size);
	}

	@Override
	public void fitAttackSubsystem(AttackSubsystem subsystem) throws InsufficientPowergridException {
		if (subsystem == null) {
			if (this.attackSubsystem != null) {
				unfitAttackSubsystem();
			}
		}
		else if (this.powergridOutput.value() < subsystem.getPowerGridConsumption().value()) {
			throw new InsufficientPowergridException(
					subsystem.getPowerGridConsumption().value() - this.powergridOutput.value());
		}
		else {
			this.attackSubsystem = subsystem;
			subtractPowergridOutput(subsystem.getPowerGridConsumption());
		}
	}

	@Override
	public void fitDefensiveSubsystem(DefenciveSubsystem subsystem) throws InsufficientPowergridException {
		if (subsystem == null) {
			if (this.defenciveSubsystem != null) {
				unfitDefensiveSubsystem();
			}
		}
		else if (this.powergridOutput.value() < subsystem.getPowerGridConsumption().value()) {
			throw new InsufficientPowergridException(
					subsystem.getPowerGridConsumption().value() - this.powergridOutput.value());
		}
		else {
			this.defenciveSubsystem = subsystem;
			subtractPowergridOutput(subsystem.getPowerGridConsumption());
		}
	}

	private void unfitDefensiveSubsystem() {
		this.powergridOutput = PositiveInteger
				.of(this.powergridOutput.value() + this.defenciveSubsystem.getPowerGridConsumption().value());
		this.defenciveSubsystem = null;
	}

	private void unfitAttackSubsystem() {
		this.powergridOutput = PositiveInteger
				.of(this.powergridOutput.value() + this.attackSubsystem.getPowerGridConsumption().value());
		this.attackSubsystem = null;
	}

	private void subtractPowergridOutput(PositiveInteger diff) {
		this.powergridOutput = PositiveInteger.of(this.powergridOutput.value() - diff.value());
	}

	public CombatReadyShip undock() throws NotAllSubsystemsFitted {
		if (this.attackSubsystem == null && this.defenciveSubsystem == null) {
			throw NotAllSubsystemsFitted.bothMissing();
		}
		else if (this.attackSubsystem == null) {
			throw NotAllSubsystemsFitted.attackMissing();
		}
		else if (this.defenciveSubsystem == null) {
			throw NotAllSubsystemsFitted.defenciveMissing();
		}

		return new CombatReadyShip(this.name, this.shieldHP, this.hullHP, this.capacitorAmount,
				this.capacitorRechargeRate, this.speed, this.size, this.attackSubsystem, this.defenciveSubsystem);
	}

}
