package org.sert2521.bunnybots.util

import kotlin.math.PI

// Joysticks
const val CONTROLLER_PORT = 0
const val SECONDARY_STICK_PORT = 1

// Talon IDs
const val LEFT_FRONT_MOTOR = -1
const val LEFT_REAR_MOTOR = -1
const val RIGHT_FRONT_MOTOR = -1
const val RIGHT_REAR_MOTOR = -1

// Auto
// TODO: Update these with real values
const val WHEEL_DIAMETER = 0.15
const val WHEELBASE_WIDTH = 0.7
const val ENCODER_TICKS_PER_REVOLUTION = 8192
const val ENCODER_TICKS_PER_METER = ENCODER_TICKS_PER_REVOLUTION / (WHEEL_DIAMETER * PI)

// Other
const val UDP_PORT = 5800
