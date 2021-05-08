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

	private final PositiveInteger MAX_SHIELD_HP;

	private final PositiveInteger MAX_HULL_HP;

	private final PositiveInteger MAX_CAPACITOR_AMOUNT;

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
		this.MAX_SHIELD_HP = shieldHP;
		this.hullHP = hullHP;
		this.MAX_HULL_HP = hullHP;
		this.capacitorAmount = capacitorAmount;
		this.MAX_CAPACITOR_AMOUNT = capacitorAmount;
		this.capacitorRechargeRate = capacitorRechargeRate;
		this.speed = speed;
		this.size = size;
		this.attackSubsystem = attackSubsystem;
		this.defenciveSubsystem = defenciveSubsystem;
	}

	@Override
	public void endTurn() {
		this.capacitorAmount = PositiveInteger.of(
				(this.capacitorAmount.value() + this.capacitorRechargeRate.value()) > this.MAX_CAPACITOR_AMOUNT.value()
						? this.MAX_CAPACITOR_AMOUNT.value()
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

		this.capacitorAmount = PositiveInteger
				.of(this.capacitorAmount.value() - this.attackSubsystem.getCapacitorConsumption().value());
		AttackAction attackAction = new AttackAction(this.attackSubsystem.attack(target), this, target,
				this.attackSubsystem);
		return Optional.of(attackAction);
	}

	@Override
	public AttackResult applyAttack(AttackAction attack) {
		attack = this.defenciveSubsystem.reduceDamage(attack);

		if (this.shieldHP.value() > attack.damage.value()) {
			this.shieldHP = PositiveInteger.of(this.shieldHP.value() - attack.damage.value());
			return new AttackResult.DamageRecived(attack.weapon, attack.damage, this);
		}
		else if ((this.shieldHP.value() + this.hullHP.value()) > attack.damage.value()) {
			this.hullHP = PositiveInteger.of(attack.damage.value() - this.shieldHP.value());
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

		this.capacitorAmount = PositiveInteger
				.of(this.capacitorAmount.value() - this.defenciveSubsystem.getCapacitorConsumption().value());
		RegenerateAction regenerateAction = defenciveSubsystem.regenerate();

		regenerateAction = computeRegeneration(regenerateAction);

		return Optional.of(regenerateAction);
	}

	private RegenerateAction computeRegeneration(RegenerateAction regenerateAction) {
		int possibleRegeneratedHullHP = this.MAX_HULL_HP.value() - this.hullHP.value();
		int possibleRegeneratedShieldHP = this.MAX_SHIELD_HP.value() - this.shieldHP.value();

		if (regenerateAction.hullHPRegenerated.value() > possibleRegeneratedHullHP
				&& regenerateAction.shieldHPRegenerated.value() > possibleRegeneratedShieldHP) {
			regenerateAction = new RegenerateAction(PositiveInteger.of(possibleRegeneratedShieldHP),
					PositiveInteger.of(possibleRegeneratedHullHP));
			this.hullHP = this.MAX_HULL_HP;
			this.shieldHP = this.MAX_SHIELD_HP;
		}
		else if (regenerateAction.shieldHPRegenerated.value() > possibleRegeneratedShieldHP) {
			this.shieldHP = this.MAX_SHIELD_HP;
			this.hullHP = PositiveInteger.of(this.hullHP.value() + regenerateAction.hullHPRegenerated.value());
			regenerateAction = new RegenerateAction(PositiveInteger.of(possibleRegeneratedShieldHP),
					regenerateAction.hullHPRegenerated);
		}
		else if (regenerateAction.hullHPRegenerated.value() > possibleRegeneratedHullHP) {
			this.hullHP = this.MAX_HULL_HP;
			this.shieldHP = PositiveInteger.of(shieldHP.value() + regenerateAction.shieldHPRegenerated.value());
			regenerateAction = new RegenerateAction(regenerateAction.shieldHPRegenerated,
					PositiveInteger.of(possibleRegeneratedHullHP));
		}
		else {
			this.hullHP = PositiveInteger.of(this.hullHP.value() + regenerateAction.hullHPRegenerated.value());
			this.shieldHP = PositiveInteger.of(this.shieldHP.value() + regenerateAction.shieldHPRegenerated.value());
		}

		return regenerateAction;
	}

}
