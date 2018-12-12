package org.sert2521.bunnybots

// Joysticks
const val CONTROLLER_PORT = 0
const val PRIMARY_STICK_PORT = 0
const val SECONDARY_STICK_PORT = 1

// Talon IDs
const val LEFT_FRONT_MOTOR = 11
const val LEFT_REAR_MOTOR = 12
const val RIGHT_FRONT_MOTOR = 13
const val RIGHT_REAR_MOTOR = 14

const val INTAKE_MOTOR = 15
const val OUTTAKE_BELT_MOTOR = 16
const val ARM_MOTOR = 17

// Servo IDs
const val LEFT_DROPPER_SERVO = 0
const val MIDDLE_DROPPER_SERVO = 1
const val RIGHT_DROPPER_SERVO = 2

const val OUTTAKE_AUTO_SERVO = 3
const val OUTTAKE_TELEOP_SERVO = 4

// Auto
const val WHEEL_DIAMETER = 4
const val WHEELBASE_WIDTH = 0.7
const val ENCODER_TICKS_PER_REVOLUTION = 4096
const val ENCODER_TICKS_PER_METER = ENCODER_TICKS_PER_REVOLUTION / (WHEEL_DIAMETER * Math.PI)

// Other
const val UDP_PORT = 5800
