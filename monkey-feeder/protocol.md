# Monkey Feeder communication protocol

All requests and responses end with "`\n`".

## request

- `conn_data <host> <port>`

	Establish data channel connection to given address.

- `start`

	Start sending eye tracking data.

- `stop`

	Stop sending eye tracking data.

- `bye`

	Disconnect control channel.

## response

- `msg <message>`

	Send a message that meaned to be displayed to the user.

- `ready`

	Indicate that eye tracking is connected and ready.

- `not_connected`

	Indicate that eye tracking is not connected.

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
