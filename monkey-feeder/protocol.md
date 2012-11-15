# Monkey Feeder communication protocol

All requests and responses end with "`\n`".

## request

- `conn_data <host> <port>`

	Establish data channel connection to given address.

- `start`

	Start sending eye tracking data. Expect `tracking_started`.

- `stop`

	Stop sending eye tracking data. Expect `tracking_stopped`.

- `bye`

	Disconnect control channel. Expect `bye`.

## response

- `msg <message>`

	Send a message that meaned to be displayed to the user.

- `ready`

	Indicate that eye tracker is connected and ready.

- `not_connected`

	Indicate that eye tracker is not connected.

- `tracking_started`

	Indicate that eye tracking has started.

- `tracking_stopped`

	Indicate that eye tracking has stopped.

- `bye`

	Disconnect control channel.

- `error [message]`

	Something went wrong.

## data

- `hover <enter|move|exit> <x> <y>`

	Ask Monkey to inject corresponding event to the system.

## calibration request

- `calib_start`

	Start eye tracker calibration. Expect `calib_started`.

- `calib_add <x> <y>`

	Add a calibration point. Expect `calib_added`.

- `calib_compute`

	Compute and finish calibration. Expect `calib_done` and `calib_stopped`.

- `calib_abort`

	Cancel calibration. Expect `calib_stopped`.

## calibration response

- `calib_started`

	Indicate that eye tracker entered calibration state.

- `calib_added`

	Indicate that a calibration point has added.

- `calib_done`

	Indicate that the calibration is computed and set to eye tracker.

- `calib_stopped`

	Indicate that eye tracker is no longer in calibration state.
