# Monkey Feeder communication protocol

All requests and responses end with "`\n`".

## request

- `set <param> <value>`

	Set a configuration parameter to new value.
	`param` is `monkey_host|monkey_port|display_width|display_height`.

- `status`

	Request for eye tracker status. Expect `status`.

- `start`

	Start sending eye tracking data. Expect `tracking_started`.

- `stop`

	Stop sending eye tracking data. Expect `tracking_stopped`.

## response

- `msg <message>`

	Send a message that meant to be displayed to the user.

- `status <disconnected|ready|tracking|calibrating>`

	Report the current status of eye tracker.

- `tracking_started`

	Indicate that eye tracking has started.

- `tracking_stopped`

	Indicate that eye tracking has stopped.

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
