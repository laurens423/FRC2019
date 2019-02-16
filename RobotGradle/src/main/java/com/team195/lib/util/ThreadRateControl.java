package com.team195.lib.util;

import com.team195.frc2019.reporters.ConsoleReporter;
import com.team195.frc2019.reporters.MessageLevel;
import com.team254.lib.util.MovingAverage;
import edu.wpi.first.hal.NotifierJNI;
import edu.wpi.first.wpilibj.Timer;

public class ThreadRateControl {
	private double startTime;
	private double endTime;
	private int elapsedTimeMS;
	private boolean started;
	private double mPrevStartTime;
	private double mLoopTimeMS;
	private MovingAverage mAverageLoopTime;

	private final int m_notifier = NotifierJNI.initializeNotifier();

	private double prevDtCalcTime;


	public ThreadRateControl() {
		startTime = 0;
		mPrevStartTime = 0;
		mLoopTimeMS = 0;
		endTime = 0;
		elapsedTimeMS = 0;
		prevDtCalcTime = 0;
		started = false;
		mAverageLoopTime = new MovingAverage(20);
	}

	@Override
	@SuppressWarnings("NoFinalizer")
	protected void finalize() {
		NotifierJNI.stopNotifier(m_notifier);
		NotifierJNI.cleanNotifier(m_notifier);
	}

	public synchronized void start(boolean resetStart) {
		if (resetStart)
			started = false;
		start();
	}

	public synchronized void start() {
		if (!started) {
			startTime = Timer.getFPGATimestamp();
			mPrevStartTime = startTime;
			getDt();
			started = true;
		} else {
			ConsoleReporter.report("Thread rate control start called too many times!", MessageLevel.ERROR);
		}
	}

	/**
	 * Do rate control for loops
	 * @param minLoopTime Time in ms
	 */
	public synchronized void doRateControl(int minLoopTime) {
		mLoopTimeMS = (startTime - mPrevStartTime) * 1000;
		mAverageLoopTime.addNumber(mLoopTimeMS);
		if (startTime != 0) {
			do {
				endTime = Timer.getFPGATimestamp();
				elapsedTimeMS = (int) ((endTime - startTime) * 1000);
				if (elapsedTimeMS < minLoopTime) {
					try {
						NotifierJNI.updateNotifierAlarm(m_notifier, (long) ((minLoopTime - elapsedTimeMS) * 1000));
						NotifierJNI.waitForNotifierAlarm(m_notifier);
					} catch (Exception ex) {
						ConsoleReporter.report(ex);
					}
				}
			} while (elapsedTimeMS < minLoopTime);
		} else {
			ConsoleReporter.report("Thread rate control called without setting a start time! Check your loops!", MessageLevel.ERROR);
		}
		mPrevStartTime = startTime;
		startTime = Timer.getFPGATimestamp();
	}

	public double getLoopTime() {
		return mLoopTimeMS;
	}

	public double getAverageLoopTime() {
		return mAverageLoopTime.getAverage();
	}

	public synchronized double getDt() {
		double currDtCalcTime = Timer.getFPGATimestamp();
		double dt = currDtCalcTime - prevDtCalcTime;
		prevDtCalcTime = currDtCalcTime;
		return dt;
	}
}
