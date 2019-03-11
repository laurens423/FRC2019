package com.team195.frc2019.auto.autonomy;

import com.team195.frc2019.Constants;
import com.team195.frc2019.auto.AutoConstants;
import com.team195.frc2019.auto.actions.*;
import com.team195.frc2019.subsystems.BallIntakeArm;
import com.team195.frc2019.subsystems.Elevator;
import com.team195.frc2019.subsystems.HatchIntakeArm;
import com.team195.frc2019.subsystems.Turret;
import com.team195.frc2019.subsystems.positions.BallIntakeArmPositions;
import com.team195.frc2019.subsystems.positions.ElevatorPositions;
import com.team195.frc2019.subsystems.positions.HatchArmPositions;
import com.team195.frc2019.subsystems.positions.TurretPositions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class AutomatedActions {
	private static boolean unfolded = false;

	public static boolean isUnfolded() {
		return unfolded;
	}

	public static AutomatedAction homeElevator() {
		ArrayList<Action> actionList = new ArrayList<>();

		actionList.add(new SetElevatorOpenLoopAction(0));
		actionList.add(new WaitAction(3.5));
		actionList.add(new SetElevatorHomeAction());

		return AutomatedAction.fromAction(new SeriesAction(actionList), Constants.kActionTimeoutS, Elevator.getInstance());
	}

	public static AutomatedAction reverseHatchPickup() {
		ArrayList<Action> actionList = new ArrayList<>();

		actionList.add(new SetElevatorHeightAction(ElevatorPositions.HatchPickupStation));
		// actionList.add(new SetBallArmRotationAction(BallIntakeArmPositions.Down));
		// actionList.add(new WaitAction(0.3));
		actionList.add(new SetTurretPositionAction(TurretPositions.Back180));
		actionList.add(new WaitAction(0.1));
		actionList.add(new SetBallArmRotationAction(BallIntakeArmPositions.Up));

		return AutomatedAction.fromAction(new SeriesAction(actionList), Constants.kActionTimeoutS, Elevator.getInstance(), Turret.getInstance(), BallIntakeArm.getInstance());
	}

	public static AutomatedAction lowerIntakeAndResetTurret() {
		ArrayList<Action> actionList = new ArrayList<>();

		actionList.add(new SetBallArmRotationAction(BallIntakeArmPositions.Down));
		actionList.add(new SetTurretPositionAction(TurretPositions.Home));

		return AutomatedAction.fromAction(new SeriesAction(actionList), Constants.kActionTimeoutS, BallIntakeArm.getInstance(), Turret.getInstance());
	}

	public static AutomatedAction unfold() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new SetBallArmRotationAction(BallIntakeArmPositions.Down));
		actionArrayList.add(new WaitAction(AutoConstants.kWaitForArmFall));
		actionArrayList.add(new SetElevatorHeightAction(ElevatorPositions.RocketHatchLow));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, BallIntakeArm.getInstance(), Elevator.getInstance());
	}

	public static AutomatedAction pickupHatchFeederStation() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.HatchPickupStation),
							new SetBeakAction(false))));
		actionArrayList.add(new SetHatchPushAction(true));
		actionArrayList.add(new WaitForHatchOrTimeoutAction());
		actionArrayList.add(new SetHatchPushAction(false));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Elevator.getInstance(), Turret.getInstance());
	}

	public static AutomatedAction hatchHandoff() {
		ArrayList<Action> actionArrayList = new ArrayList<>();
		actionArrayList.add(new SetHandoffCollisionAvoidanceAction(false));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.HatchHandoff), new SetBeakAction(false))));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetHatchArmRollerAction(0.3), new SetHatchArmRotationAction(HatchArmPositions.Handoff, 0.15))));
		actionArrayList.add(new SetHatchPushAction(true));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.HatchHandoff + ElevatorPositions.HatchLiftOffset),
				                                             new SetHatchArmRollerAction(-0.2))));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetHatchArmRollerAction(0),
															 new SetHatchArmRotationAction(HatchArmPositions.Inside, 0.15),
															 new SetHatchPushAction(false))));
		actionArrayList.add(new SetHandoffCollisionAvoidanceAction(true));
		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Elevator.getInstance(), Turret.getInstance(), HatchIntakeArm.getInstance());
	}

	public static AutomatedAction placeHatch(double elevatorPosition, Function<Void, Boolean> buttonValueGetter) {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new SetElevatorHeightAction(elevatorPosition));
		actionArrayList.add(new WaitForFallingEdgeButtonAction(buttonValueGetter, 100));
		actionArrayList.add(new SetHatchPushAction(true));
		actionArrayList.add(new WaitAction(AutoConstants.kWaitForHatchPush));
		actionArrayList.add(new SetBeakAction(false));
		actionArrayList.add(new SetHatchPushAction(false));
		actionArrayList.add(new WaitAction(AutoConstants.kWaitForHatchPush * 2));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.Resting),
				new SetTurretPositionAction(TurretPositions.Home))));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Elevator.getInstance(), Turret.getInstance());
	}

	public static AutomatedAction placeHatch() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new SetHatchPushAction(true));
		actionArrayList.add(new WaitAction(AutoConstants.kWaitForHatchPush));
		actionArrayList.add(new SetBeakAction(false));
		actionArrayList.add(new SetHatchPushAction(false));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Turret.getInstance());
	}

	public static AutomatedAction shootBall(double elevatorPosition, Function<Void, Boolean> buttonValueGetter) {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new SetElevatorHeightAction(elevatorPosition));
		actionArrayList.add(new WaitForFallingEdgeButtonAction(buttonValueGetter, 100));
		actionArrayList.add(new SetBallShooterOpenLoopAction(TurretPositions.BallShootSpeedNormal));
		actionArrayList.add(new SetBallPushAction(true));
		actionArrayList.add(new WaitAction(AutoConstants.kWaitForBallPush));
		actionArrayList.add(new SetBallPushAction(false));
		actionArrayList.add(new SetBallShooterOpenLoopAction(0));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.Resting),
				new SetTurretPositionAction(TurretPositions.Home))));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Elevator.getInstance(), Turret.getInstance());
	}

	public static AutomatedAction shootBall() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new SetBallShooterOpenLoopAction(TurretPositions.BallShootSpeedNormal));
		actionArrayList.add(new SetBallPushAction(true));
		actionArrayList.add(new WaitAction(AutoConstants.kWaitForBallPush));
		actionArrayList.add(new SetBallPushAction(false));
		actionArrayList.add(new SetBallShooterOpenLoopAction(0));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Turret.getInstance());
	}

	public static AutomatedAction elevatorDown() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.Resting),
				new SetTurretPositionAction(TurretPositions.Home))));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Elevator.getInstance());
	}

	public static AutomatedAction elevatorSet(double elevatorHeight) {
		return AutomatedAction.fromAction(new SetElevatorHeightAction(elevatorHeight), Constants.kActionTimeoutS, Elevator.getInstance());
	}

	public static AutomatedAction ballArmSet(double armPosition) {
		return AutomatedAction.fromAction(new SetBallArmRotationAction(armPosition), Constants.kActionTimeoutS, BallIntakeArm.getInstance());
	}

	public static AutomatedAction intakeBallOn(Function<Void, Boolean> buttonValueGetter) {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetElevatorHeightAction(ElevatorPositions.Down),
				new SetTurretPositionAction(TurretPositions.Home))));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetBallShooterOpenLoopAction(TurretPositions.BallShootSpeedIntake),
															 new SetBallIntakeAction(BallIntakeArmPositions.RollerIntake))));
		actionArrayList.add(new WaitForFallingEdgeButtonAction(buttonValueGetter, 20));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetBallShooterOpenLoopAction(TurretPositions.BallShootSpeedOff),
				new SetBallIntakeAction(BallIntakeArmPositions.RollerOff))));
		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Elevator.getInstance(), BallIntakeArm.getInstance(), Turret.getInstance());
	}

	public static AutomatedAction intakeBallOff() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetBallShooterOpenLoopAction(0),
				new SetBallIntakeAction(0))));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, BallIntakeArm.getInstance());
	}

	public static AutomatedAction prepareClimb() {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetBallArmRotationAction(BallIntakeArmPositions.Up),
				new DropBallArmClimbBarAction())));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, BallIntakeArm.getInstance());
	}

	public static AutomatedAction setTurretOpenLoop(Function<Void, Boolean> buttonGetterMethod, Function<Void, Double> axisGetterMethod) {
		return AutomatedAction.fromAction(new SetTurretOpenLoopAction(buttonGetterMethod, axisGetterMethod), Constants.kActionTimeoutS * 3, BallIntakeArm.getInstance());
	}

	public static AutomatedAction setTurretPosition(double turretPosition) {
		return AutomatedAction.fromAction(new SetTurretPositionAction(turretPosition), Constants.kActionTimeoutS, BallIntakeArm.getInstance());
	}

	public static AutomatedAction rollerHatchFloorIntake(Function<Void, Boolean> buttonValueGetter) {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetHatchArmRollerAction(HatchArmPositions.RollerIntake),
				new SetHatchArmRotationAction(HatchArmPositions.Outside))));
		actionArrayList.add(new WaitForFallingEdgeButtonAction(buttonValueGetter, 10));
		actionArrayList.add(hatchHandoff());

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, HatchIntakeArm.getInstance());
	}

	public static AutomatedAction hatchArmOutIntake(Function<Void, Boolean> buttonValueGetter) {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetHatchArmRollerAction(HatchArmPositions.RollerOuttake),
				new SetHatchArmRotationAction(HatchArmPositions.Outside))));
		actionArrayList.add(new WaitForFallingEdgeButtonAction(buttonValueGetter, 10));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetHatchArmRollerAction(HatchArmPositions.RollerOff),
				new SetHatchArmRotationAction(HatchArmPositions.Inside))));


		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, HatchIntakeArm.getInstance());
	}

	public static AutomatedAction ballOuttake(Function<Void, Boolean> buttonValueGetter) {
		ArrayList<Action> actionArrayList = new ArrayList<>();

		actionArrayList.add(new ParallelAction(Arrays.asList(new SetBallIntakeAction(BallIntakeArmPositions.RollerOuttake),
				new SetBallShooterOpenLoopAction(TurretPositions.BallShootSpeedNormal))));
		actionArrayList.add(new WaitForFallingEdgeButtonAction(buttonValueGetter, 10));
		actionArrayList.add(new ParallelAction(Arrays.asList(new SetBallIntakeAction(BallIntakeArmPositions.RollerOff),
				new SetBallShooterOpenLoopAction(TurretPositions.BallShootSpeedOff))));

		return AutomatedAction.fromAction(new SeriesAction(actionArrayList), Constants.kActionTimeoutS, Turret.getInstance(), BallIntakeArm.getInstance());
	}

}