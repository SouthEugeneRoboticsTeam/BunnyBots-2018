package org.sert2521.bunnybots.util

// Joysticks
const val CONTROLLER_PORT = 0
const val PRIMARY_STICK_PORT = 0
const val SECONDARY_STICK_PORT = 1

// Talon IDs
const val LEFT_FRONT_MOTOR = -1
const val LEFT_REAR_MOTOR = -1
const val RIGHT_FRONT_MOTOR = -1
const val RIGHT_REAR_MOTOR = -1

const val INTAKE_MOTOR = -1
const val ARM_MOTOR = -1
const val OUTTAKE_BELT_MOTOR = -1

// Servo IDs
const val OUTTAKE_FLAP_SERVO = -1

const val LEFT_DROPPER_SERVO = -1
const val MIDDLE_DROPPER_SERVO = -1
const val RIGHT_DROPPER_SERVO = -1

// Auto
const val WHEEL_DIAMETER = 6
const val WHEELBASE_WIDTH = 0.7
const val ENCODER_TICKS_PER_REVOLUTION = 8192
const val ENCODER_TICKS_PER_METER = ENCODER_TICKS_PER_REVOLUTION / (WHEEL_DIAMETER * Math.PI)

// Other
const val UDP_PORT = 5800
