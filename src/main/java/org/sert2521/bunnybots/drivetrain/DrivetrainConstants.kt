package org.sert2521.bunnybots.drivetrain

internal const val GYRO_CORRECTION_P = 0.008
internal const val GYRO_CORRECTION_I = 0.001
internal const val GYRO_CORRECTION_I_DECAY = 1.0

internal const val LIDAR_CORRECTION_P = 0.05
internal const val LIDAR_CORRECTION_I = 0.0015
internal const val LIDAR_CORRECTION_I_DECAY = 1.0

internal const val LIDAR_SETPOINT = 5.0

internal const val LEFT_FEED_FORWARD_COEFFICIENT = 0.10
internal const val LEFT_FEED_FORWARD_OFFSET = 0.02

internal const val RIGHT_FEED_FORWARD_COEFFICIENT = 0.10
internal const val RIGHT_FEED_FORWARD_OFFSET = 0.02

internal const val TURNING_FEED_FORWARD = 0.035
