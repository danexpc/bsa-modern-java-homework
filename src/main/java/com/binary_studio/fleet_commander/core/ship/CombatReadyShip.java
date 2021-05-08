package com.binary_studio.fleet_commander.core.ship;

import java.util.Optional;

import com.binary_studio.fleet_commander.core.actions.attack.AttackAction;
import com.binary_studio.fleet_commander.core.actions.defence.AttackResult;
import com.binary_studio.fleet_commander.core.actions.defence.RegenerateAction;
import com.binary_studio.fleet_commander.core.common.Attackable;
import com.binary_studio.fleet_commander.core.common.PositiveInteger;
import com.binary_studio.fleet_commander.core.ship.contract.CombatReadyVessel;
import com.binary_studio.fleet_commander.core.subsystems.contract.AttackSubsystem;
import com.binary_studio.fleet_commander.core.subsystems.contract.DefenciveSubsystem;

public final class CombatReadyShip implements CombatReadyVessel {

	private final PositiveInteger maxShieldHP;

	private final PositiveInteger maxHullHP;

	private final PositiveInteger maxCapacitorAmount;

	private final String name;

	private PositiveInteger shieldHP;

	private PositiveInteger hullHP;

	private PositiveInteger capacitorAmount;

	private final PositiveInteger capacitorRechargeRate;

	private final PositiveInteger speed;

	private final PositiveInteger size;

	private final AttackSubsystem attackSubsystem;

	private final DefenciveSubsystem defenciveSubsystem;

	public CombatReadyShip(String name, PositiveInteger shieldHP, PositiveInteger hullHP,
			PositiveInteger capacitorAmount, PositiveInteger capacitorRechargeRate, PositiveInteger speed,
			PositiveInteger size, AttackSubsystem attackSubsystem, DefenciveSubsystem defenciveSubsystem) {
		this.name = name;
		this.shieldHP = shieldHP;
		this.maxShieldHP = shieldHP;
		this.hullHP = hullHP;
		this.maxHullHP = hullHP;
		this.capacitorAmount = capacitorAmount;
		this.maxCapacitorAmount = capacitorAmount;
		this.capacitorRechargeRate = capacitorRechargeRate;
		this.speed = speed;
		this.size = size;
		this.attackSubsystem = attackSubsystem;
		this.defenciveSubsystem = defenciveSubsystem;
	}

	@Override
	public void endTurn() {
		this.capacitorAmount = PositiveInteger.of(
				(this.capacitorAmount.value() + this.capacitorRechargeRate.value()) > this.maxCapacitorAmount.value()
						? this.maxCapacitorAmount.value()
						: (this.capacitorAmount.value() + this.capacitorRechargeRate.value()));
	}

	@Override
	public void startTurn() {

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public PositiveInteger getSize() {
		return this.size;
	}

	@Override
	public PositiveInteger getCurrentSpeed() {
		return this.speed;
	}

	@Override
	public Optional<AttackAction> attack(Attackable target) {
		if (this.capacitorAmount.value() < this.attackSubsystem.getCapacitorConsumption().value()) {
			return Optional.empty();
		}

		this.capacitorAmount = PositiveInteger.difference(this.capacitorAmount,
				this.attackSubsystem.getCapacitorConsumption());
		AttackAction attackAction = new AttackAction(this.attackSubsystem.attack(target), this, target,
				this.attackSubsystem);
		return Optional.of(attackAction);
	}

	@Override
	public AttackResult applyAttack(AttackAction attack) {
		attack = this.defenciveSubsystem.reduceDamage(attack);

		if (this.shieldHP.value() > attack.damage.value()) {
			this.shieldHP = PositiveInteger.difference(this.shieldHP, attack.damage);
			return new AttackResult.DamageRecived(attack.weapon, attack.damage, this);
		}
		else if ((this.shieldHP.value() + this.hullHP.value()) > attack.damage.value()) {
			this.hullHP = PositiveInteger.difference(attack.damage, this.shieldHP);
			this.shieldHP = PositiveInteger.of(0);
			return new AttackResult.DamageRecived(attack.weapon, attack.damage, this);
		}

		return new AttackResult.Destroyed();
	}

	@Override
	public Optional<RegenerateAction> regenerate() {
		if (this.capacitorAmount.value() < this.defenciveSubsystem.getCapacitorConsumption().value()) {
			return Optional.empty();
		}

		this.capacitorAmount = PositiveInteger.difference(this.capacitorAmount,
				this.defenciveSubsystem.getCapacitorConsumption());
		RegenerateAction regenerateAction = this.defenciveSubsystem.regenerate();

		regenerateAction = computeRegeneration(regenerateAction);

		return Optional.of(regenerateAction);
	}

	private RegenerateAction computeRegeneration(RegenerateAction regenerateAction) {
		PositiveInteger possibleRegeneratedHullHP = PositiveInteger.difference(this.maxHullHP, this.hullHP);
		PositiveInteger possibleRegeneratedShieldHP = PositiveInteger.difference(this.maxShieldHP, this.shieldHP);

		if (regenerateAction.hullHPRegenerated.value() > possibleRegeneratedHullHP.value()
				&& regenerateAction.shieldHPRegenerated.value() > possibleRegeneratedShieldHP.value()) {
			regenerateAction = new RegenerateAction(possibleRegeneratedShieldHP, possibleRegeneratedHullHP);
			this.hullHP = this.maxHullHP;
			this.shieldHP = this.maxShieldHP;
		}
		else if (regenerateAction.shieldHPRegenerated.value() > possibleRegeneratedShieldHP.value()) {
			this.shieldHP = this.maxShieldHP;
			this.hullHP = PositiveInteger.sum(this.hullHP, regenerateAction.hullHPRegenerated);
			regenerateAction = new RegenerateAction(possibleRegeneratedShieldHP, regenerateAction.hullHPRegenerated);
		}
		else if (regenerateAction.hullHPRegenerated.value() > possibleRegeneratedHullHP.value()) {
			this.hullHP = this.maxHullHP;
			this.shieldHP = PositiveInteger.sum(this.shieldHP, regenerateAction.shieldHPRegenerated);
			regenerateAction = new RegenerateAction(regenerateAction.shieldHPRegenerated, possibleRegeneratedHullHP);
		}
		else {
			this.hullHP = PositiveInteger.sum(this.hullHP, regenerateAction.hullHPRegenerated);
			this.shieldHP = PositiveInteger.sum(this.shieldHP, regenerateAction.shieldHPRegenerated);
		}

		return regenerateAction;
	}

}
