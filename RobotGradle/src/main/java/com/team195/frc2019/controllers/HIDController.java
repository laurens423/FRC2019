package com.team195.frc2019.controllers;

import com.team195.frc2019.Constants;
import com.team195.frc2019.auto.AutoModeExecutor;
import com.team195.frc2019.subsystems.*;
import com.team195.lib.drivers.dashjoy.CKDashJoystick;
import com.team195.lib.util.ThreadRateControl;
import com.team254.lib.util.CheesyDriveHelper;
import com.team254.lib.util.CrashTracker;

import java.util.function.Function;

public class HIDController {
	private static HIDController mInstance = null;
	public static HIDController getInstance(Function<Void, AutoModeExecutor> getAutoModeExecutor) {
		if (mInstance == null)
			mInstance = new HIDController(getAutoModeExecutor);

		return mInstance;
	}

	private final CKDashJoystick driveJoystick = Controllers.getInstance().getDriveJoystick();
	private final CKDashJoystick armControlJoystick = Controllers.getInstance().getArmControlJoystick();
	private final CKDashJoystick buttonBox1 = Controllers.getInstance().getButtonBox1();
	private final CKDashJoystick buttonBox2 = Controllers.getInstance().getButtonBox2();

	private CheesyDriveHelper mCheesyDriveHelper = new CheesyDriveHelper();
	private Drive mDrive = Drive.getInstance();

	private Thread controlThread = null;
	private boolean runThread = true;

	private final Function<Void, AutoModeExecutor> mGetAutoModeExecutor;

	private static final int HID_RATE_CONTROL = 10;

	private HIDController(Function<Void, AutoModeExecutor> getAutoModeExecutor) {
		mGetAutoModeExecutor = getAutoModeExecutor;
	}

	public void start() {
		if (controlThread == null || !controlThread.isAlive()) {
			setRunState(true);
			controlThread = new Thread(() -> {
				ThreadRateControl threadRateControl = new ThreadRateControl();
				threadRateControl.start();
				while (runThread) {
					try {
						AutoModeExecutor autoModeExecutor = mGetAutoModeExecutor.apply(null);
						if (autoModeExecutor != null && autoModeExecutor.isRunning()) {
							if (driveJoystick.isAxisInputActive())
								autoModeExecutor.stop();
						} else {
							//User Control Interface code here

							double throttle = -driveJoystick.getNormalizedAxis(1, 0.04);
							double turn = driveJoystick.getNormalizedAxis(4, 0.04);
							boolean quickTurn = driveJoystick.getRawButton(5);

							if (Elevator.getInstance().getPosition() > Constants.kElevatorLowSensitivityThreshold) {
								throttle *= Constants.kLowSensitivityFactor;
								turn *= Constants.kLowSensitivityFactor;
							}
							mDrive.setOpenLoop(mCheesyDriveHelper.cheesyDrive(throttle, turn, quickTurn, mDrive.isHighGear()));

							if (driveJoystick.getRisingEdgeButton(1)) {
//								Drive.getInstance().setPTO(true);
							}
							else if (driveJoystick.getRisingEdgeButton(2)) {
//								Drive.getInstance().setPTO(false);
							}
						}
					} catch (Throwable t) {
						CrashTracker.logThrowableCrash(t);
						throw t;
					}
					threadRateControl.doRateControl(HID_RATE_CONTROL);
				}
			});
			controlThread.setPriority(Constants.kRobotThreadPriority);
			controlThread.start();
		}
	}

	public void stop() {
		setRunState(false);
		try {
			controlThread.join(100);
		} catch (Exception ignored) {

		} finally {
			controlThread = null;
		}
	}

	private synchronized void setRunState(boolean run) {
		runThread = run;
	}
}