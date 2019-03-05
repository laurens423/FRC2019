package com.team195.frc2019.subsystems;

import com.team195.frc2019.Constants;
import com.team195.frc2019.loops.ILooper;
import com.team195.frc2019.loops.Loop;
import com.team195.frc2019.subsystems.positions.ElevatorPositions;
import com.team195.frc2019.subsystems.positions.HatchArmPositions;
import com.team195.lib.drivers.motorcontrol.CKTalonSRX;
import com.team195.lib.drivers.motorcontrol.MCControlMode;
import com.team195.lib.drivers.motorcontrol.PDPBreaker;
import com.team195.lib.drivers.motorcontrol.TuneablePIDOSC;
import com.team195.lib.util.InterferenceSystem;
import com.team195.lib.util.MotionInterferenceChecker;

public class HatchIntakeArm extends Subsystem implements InterferenceSystem {

	private static HatchIntakeArm mInstance = new HatchIntakeArm();

	private final CKTalonSRX mHatchArmRotationMotor;
	private final CKTalonSRX mHatchArmRollerMotor;

	private final MotionInterferenceChecker hatchArmAnyPositionCheck;
	private final MotionInterferenceChecker hatchArmPauseDownCheck;
	private final MotionInterferenceChecker hatchArmPauseUpCheck;

	private HatchArmControlMode mHatchArmControlMode = HatchArmControlMode.POSITION;

	private double mHatchArmSetpoint = 0;
	private double mHatchRollerSetpoint = 0;

	private HatchIntakeArm() {
		mHatchArmRotationMotor = new CKTalonSRX(Constants.kHatchIntakeRotationMotorId, false, PDPBreaker.B30A);
		mHatchArmRotationMotor.setInverted(true);
		mHatchArmRotationMotor.setSensorPhase(true);
		mHatchArmRotationMotor.setPIDF(Constants.kHatchArmPositionKp, Constants.kHatchArmPositionKi, Constants.kHatchArmPositionKd, Constants.kHatchArmPositionKf);
		mHatchArmRotationMotor.setMotionParameters(Constants.kHatchArmPositionCruiseVel, Constants.kHatchArmPositionMMAccel);
		zeroSensors();
		mHatchArmRotationMotor.configForwardSoftLimitThreshold(Constants.kHatchArmForwardSoftLimit);
		mHatchArmRotationMotor.configForwardSoftLimitEnable(true);
		mHatchArmRotationMotor.configReverseSoftLimitThreshold(Constants.kHatchArmReverseSoftLimit);
		mHatchArmRotationMotor.configReverseSoftLimitEnable(true);
		mHatchArmRotationMotor.setControlMode(MCControlMode.MotionMagic);

//		TuneablePIDOSC x;
//		try {
//			x = new TuneablePIDOSC("Hatch Arm", 5804, true, mHatchArmRotationMotor);
//		} catch (Exception ignored) {
//
//		}

		mHatchArmRollerMotor = new CKTalonSRX(Constants.kHatchIntakeRollerMotorId, false, PDPBreaker.B30A);
		mHatchArmRollerMotor.setInverted(true);


		hatchArmAnyPositionCheck = new MotionInterferenceChecker(MotionInterferenceChecker.LogicOperation.AND,
				(t) -> (Elevator.getInstance().getPosition() > ElevatorPositions.CollisionThresholdHatchArm)
		);

		hatchArmPauseDownCheck = new MotionInterferenceChecker(MotionInterferenceChecker.LogicOperation.AND,
				(t) -> (mHatchArmSetpoint < HatchArmPositions.CollisionThreshold),
				(t) -> (getPosition() > HatchArmPositions.CollisionThreshold + HatchArmPositions.PositionDelta)
		);

		hatchArmPauseUpCheck = new MotionInterferenceChecker(MotionInterferenceChecker.LogicOperation.AND,
				(t) -> (mHatchArmSetpoint >= HatchArmPositions.Inside),
				(t) -> (getPosition() < HatchArmPositions.PositionDelta)
		);
	}

	public static HatchIntakeArm getInstance() {
		return mInstance;
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isSystemFaulted() {
		boolean systemFaulted = !mHatchArmRotationMotor.isEncoderPresent();
		if (systemFaulted)
			setHatchArmControlMode(HatchArmControlMode.OPEN_LOOP);
		return systemFaulted;
	}

	@Override
	public boolean runDiagnostics() {
		return false;
	}

	@Override
	public String generateReport() {
		String retVal = "";
		retVal += "HatchArmPos:" + mHatchArmRotationMotor.getVelocity() + ";";
		retVal += "HatchArmVel:" + mHatchArmRotationMotor.getVelocity() + ";";
		retVal += "HatchArmOutput:" + mHatchArmSetpoint + ";";
		retVal += "HatchArmCurrent:" + mHatchArmRotationMotor.getMCOutputCurrent() + ";";
		retVal += "HatchArmOutputDutyCycle:" + mHatchArmRotationMotor.getMCOutputPercent() + ";";
		retVal += "HatchArmOutputVoltage:" + mHatchArmRotationMotor.getMCOutputPercent()*mHatchArmRotationMotor.getMCInputVoltage() + ";";
		retVal += "HatchArmSupplyVoltage:" + mHatchArmRotationMotor.getMCInputVoltage() + ";";
		retVal += "HatchArmControlMode:" + mHatchArmControlMode.toString() + ";";
		retVal += "HatchArmIntakeCurrent:" + mHatchArmRollerMotor.getMCOutputCurrent() + ";";
		retVal += "HatchArmIntakeOutputDutyCycle:" + mHatchArmRollerMotor.getMCOutputPercent() + ";";
		retVal += "HatchArmIntakeOutputVoltage:" + mHatchArmRollerMotor.getMCOutputPercent()*mHatchArmRollerMotor.getMCInputVoltage() + ";";
		retVal += "HatchArmIntakeSupplyVoltage:" + mHatchArmRollerMotor.getMCInputVoltage() + ";";
		return retVal;
	}

	@Override
	public void zeroSensors() {
		mHatchArmRotationMotor.setEncoderPosition(0);
		if (mHatchArmControlMode == HatchArmControlMode.POSITION)
			mHatchArmRotationMotor.set(MCControlMode.MotionMagic, 0, 0, 0);
	}

	@Override
	public void registerEnabledLoops(ILooper in) {
		in.register(mLoop);
	}

	private final Loop mLoop = new Loop() {
		@Override
		public void onFirstStart(double timestamp) {
			synchronized (HatchIntakeArm.this) {
				zeroSensors();
			}
		}

		@Override
		public void onStart(double timestamp) {
			synchronized (HatchIntakeArm.this) {

			}
		}

		@SuppressWarnings("Duplicates")
		@Override
		public void onLoop(double timestamp) {
			synchronized (HatchIntakeArm.this) {
				switch (mHatchArmControlMode) {
					case POSITION:
						if (hatchArmAnyPositionCheck.hasPassedConditions())
							mHatchArmRotationMotor.set(MCControlMode.MotionMagic, mHatchArmSetpoint, 0, 0);
						else if (hatchArmPauseDownCheck.hasPassedConditions())
							mHatchArmRotationMotor.set(MCControlMode.MotionMagic, Math.max(mHatchArmSetpoint, HatchArmPositions.CollisionThreshold + HatchArmPositions.PositionDelta), 0, 0);
						else if (hatchArmPauseUpCheck.hasPassedConditions())
							mHatchArmRotationMotor.set(MCControlMode.MotionMagic, Math.min(mHatchArmSetpoint, HatchArmPositions.Inside), 0, 0);
						break;
					case OPEN_LOOP:
						mHatchArmRotationMotor.set(MCControlMode.PercentOut, Math.min(Math.max(mHatchArmSetpoint, -1), 1), 0, 0);
						break;
					default:
						break;
				}

				mHatchArmRollerMotor.set(MCControlMode.PercentOut, Math.min(Math.max(mHatchRollerSetpoint, -1), 1), 0, 0);
			}
		}

		@Override
		public void onStop(double timestamp) {
			stop();
		}
	};

	@Override
	public double getPosition() {
		return mHatchArmRotationMotor.getPosition();
	}

	@Override
	public double getSetpoint() {
		return mHatchArmSetpoint;
	}

	public synchronized void setHatchArmPosition(double armPosition) {
		mHatchArmSetpoint = armPosition;
	}

	public synchronized void setHatchRollerSpeed(double rollerSpeed) {
		mHatchRollerSetpoint = rollerSpeed;
	}

	private synchronized void setHatchArmControlMode(HatchArmControlMode hatchArmControlMode) {
		mHatchArmControlMode = hatchArmControlMode;
	}

	public enum HatchArmControlMode {
		POSITION,
		OPEN_LOOP,
		DISABLED;
	}
}