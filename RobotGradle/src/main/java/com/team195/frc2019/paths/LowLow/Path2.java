package com.team195.frc2019.paths.LowLow;

import com.team195.lib.util.TrajectoryFollowingMotion.Path;
import com.team195.lib.util.TrajectoryFollowingMotion.PathBuilder;
import com.team195.lib.util.TrajectoryFollowingMotion.PathBuilder.Waypoint;
import com.team195.lib.util.TrajectoryFollowingMotion.PathContainer;
import com.team195.lib.util.TrajectoryFollowingMotion.RigidTransform2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;

import java.util.ArrayList;

public class Path2 implements PathContainer {

	@Override
	public Path buildPath() {
		ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
		sWaypoints.add(new Waypoint(270,115,0,0));
		sWaypoints.add(new Waypoint(212,115,30,100));
		sWaypoints.add(new Waypoint(157,32,30,100));
		sWaypoints.add(new Waypoint(24,32,0,80));

		return PathBuilder.buildPathFromWaypoints(sWaypoints);
	}

	@Override
	public RigidTransform2d getStartPose() {
		return new RigidTransform2d(new Translation2d(270, 115), Rotation2d.fromDegrees(180));
	}

	@Override
	public boolean isReversed() {
		return true;
	}
}