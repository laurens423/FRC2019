package com.team195.frc2019.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.team195.frc2019.Constants;
import com.team195.frc2019.loops.ILooper;
import com.team195.frc2019.loops.Loop;
import com.team195.lib.drivers.motorcontrol.CKTalonSRX;
import com.team195.lib.drivers.motorcontrol.MCControlMode;
import com.team195.lib.drivers.motorcontrol.PDPBreaker;
import com.team195.lib.drivers.motorcontrol.TuneablePIDOSC;
import com.team195.lib.util.InterferenceSystem;
import com.team195.lib.util.MotionInterferenceChecker;

public class Elevator extends Subsystem implements InterferenceSystem {

	private static Elevator mInstance = new Elevator();

	private final CKTalonSRX mElevatorMaster;
	private final CKTalonSRX mElevatorSlaveA;
	private final CKTalonSRX mElevatorSlaveB;

	private final MotionInterferenceChecker elevatorDownCheck;

	private ElevatorControlMode mElevatorControlMode = ElevatorControlMode.POSITION;

	private double mElevatorSetpoint = 0;

	private Elevator() {
		mElevatorMaster = new CKTalonSRX(Constants.kElevatorMasterLeftId, false, PDPBreaker.B40A);
		mElevatorMaster.setSensorPhase(true);
		mElevatorMaster.setInverted(false);
		mElevatorMaster.setPIDF(Constants.kElevatorPositionKp, Constants.kElevatorPositionKi, Constants.kElevatorPositionKd, Constants.kElevatorPositionKf);
		mElevatorMaster.setMotionParameters(Constants.kElevatorPositionCruiseVel, Constants.kElevatorPositionMMAccel);
		zeroSensors();
		mElevatorMaster.setControlMode(MCControlMode.MotionMagic);
		mElevatorMaster.configForwardSoftLimitThreshold(Constants.kElevatorPositionForwardSoftLimit);
		mElevatorMaster.configForwardSoftLimitEnable(true);
		mElevatorMaster.configReverseSoftLimitThreshold(Constants.kElevatorPositionReverseSoftLimit);
		mElevatorMaster.configReverseSoftLimitEnable(true);

//		TuneablePIDOSC x;
//		try {
//			x = new TuneablePIDOSC("Elevator", 5804, true, mElevatorMaster);
//		} catch (Exception ignored) {
//
//		}

		mElevatorSlaveA = new CKTalonSRX(Constants.kElevatorSlaveALeftId, mElevatorMaster, PDPBreaker.B30A, false);

		mElevatorSlaveB = new CKTalonSRX(Constants.kElevatorSlaveBRightId, mElevatorMaster, PDPBreaker.B40A, true);

		elevatorDownCheck = new MotionInterferenceChecker(
				(t) -> BallIntakeArm.getInstance().getPosition() < Constants.kBallIntakeArmPosToElevator,
				(t) -> HatchIntakeArm.getInstance().getPosition() < Constants.kHatchIntakeArmPosToElevator
		);
	}

	public static Elevator getInstance() {
		return mInstance;
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isSystemFaulted() {
		boolean systemFaulted = !mElevatorMaster.isEncoderPresent();
		if (systemFaulted)
			setElevatorControlMode(ElevatorControlMode.OPEN_LOOP);
		return systemFaulted;
	}

	@Override
	public boolean runDiagnostics() {
		return false;
	}

	@Override
	public String generateReport() {
		String retVal = "";
		retVal += "ElevatorPos:" + mElevatorMaster.getVelocity() + ";";
		retVal += "ElevatorVel:" + mElevatorMaster.getVelocity() + ";";
		retVal += "ElevatorOutput:" + mElevatorSetpoint + ";";
		retVal += "Elevator1Current:" + mElevatorMaster.getMCOutputCurrent() + ";";
		retVal += "Elevator2Current:" + mElevatorSlaveA.getMCOutputCurrent() + ";";
		retVal += "Elevator3Current:" + mElevatorSlaveB.getMCOutputCurrent() + ";";
		retVal += "ElevatorOutputDutyCycle:" + mElevatorMaster.getMCOutputPercent() + ";";
		retVal += "ElevatorOutputVoltage:" + mElevatorMaster.getMCOutputPercent()*mElevatorMaster.getMCInputVoltage() + ";";
		retVal += "ElevatorSupplyVoltage:" + mElevatorMaster.getMCInputVoltage() + ";";
		retVal += "ElevatorControlMode:" + mElevatorControlMode.toString() + ";";
		return retVal;
	}

	@Override
	public void zeroSensors() {
		mElevatorMaster.setEncoderPosition(0);
		if (mElevatorControlMode == ElevatorControlMode.POSITION)
			mElevatorMaster.set(MCControlMode.MotionMagic, 0, 0, 0);
	}

	@Override
	public void registerEnabledLoops(ILooper in) {
		in.register(mLoop);
	}

	private final Loop mLoop = new Loop() {
		@Override
		public void onFirstStart(double timestamp) {
			synchronized (Elevator.this) {
				zeroSensors();
			}
		}

		@Override
		public void onStart(double timestamp) {
			synchronized (Elevator.this) {

			}
		}

		@SuppressWarnings("Duplicates")
		@Override
		public void onLoop(double timestamp) {
			synchronized (Elevator.this) {
				switch (mElevatorControlMode) {
					case POSITION:
//						if ((mElevatorSetpoint < Constants.kElevatorPosToBallIntakeArm || mElevatorSetpoint < Constants.kElevatorPosToHatchIntakeArm)
//								&& !elevatorDownCheck.hasPassedConditions())
//							mElevatorMaster.set(MCControlMode.MotionMagic, Math.max(Constants.kElevatorPosToBallIntakeArm, Constants.kElevatorPosToHatchIntakeArm), 0, 0);
//						else
							mElevatorMaster.set(MCControlMode.MotionMagic, mElevatorSetpoint, 0, 0);
						break;
					case OPEN_LOOP:
//						mElevatorMaster.set(MCControlMode.PercentOut, Math.min(Math.max(mElevatorSetpoint, -1), 1), 0, 0);
						break;
					default:
						break;
				}
			}
		}

		@Override
		public void onStop(double timestamp) {
			stop();
		}
	};

	@Override
	public double getPosition() {
		return mElevatorMaster.getPosition();
	}

	public synchronized void setElevatorPosition(double elevatorPosition) {
		mElevatorSetpoint = elevatorPosition;
	}

	public boolean isElevatorAtSetpoint(double posDelta) {
		return Math.abs(mElevatorSetpoint - mElevatorMaster.getPosition()) < Math.abs(posDelta);
	}

	private synchronized void setElevatorControlMode (ElevatorControlMode elevatorControlMode) {
		mElevatorControlMode = elevatorControlMode;
	}

	public enum ElevatorControlMode {
		POSITION,
		OPEN_LOOP;
	}
}