' Fleshserver Microcontroller code
' Carlos Castellanos
' 2006

option explicit

' net triggers
const netPin as byte = 5 'first "hit"
const netPin1 as byte = 6

' dc motors
const dcMotorPin1 as byte = 7
'dim DACcounter1 as byte
dim dcStackVar(1 to 40) as byte

' leds
const ledPin1 as byte = 8
const ledPin as byte = 9
dim ledStackVar(1 to 40) as byte

sub main()
	' wait half a second after powerup:
	call delay(0.5)

	' start the dc motors task
	debug.print "dc motors..."
	CallTask "DCTask", dcStackVar

	' start the led task
	debug.print "leds..."
	CallTask "LEDTask", ledStackVar

	' MAIN LOOP
	do
		' trigger motor and "hit" led if it's the first "hit"
		if (getPin(netPin) = 1) then
			' send a trigger out to all the hit leds and motor

			' leds
			call putPin(ledPin,1)

			' motor
			'call DACPin(dcMotorPin1, 153, DACcounter1)
			call putPin(dcMotorPin1, 1)
			
			call delay(0.01)

			' motor again
			'call DACPin(dcMotorPin1, 153, DACcounter1)

			' turn off led
			call putPin(ledPin,0)

			' turn off DC motor pin
			call putPin(dcMotorPin1,0)
		end if

		call sleep(0.0)
	loop
end sub

sub DCTask()
	dim dc1Flag as byte
	dc1Flag = 0

	do
		' deliberately not debouncing
		if ((getPin(netPin1) = 1) and (dc1Flag = 0)) then
			'call DACPin(dcMotorPin1, 153, DACcounter1)
			'call DACPin(dcMotorPin1, 153, DACcounter1)
			call putPin(dcMotorPin1,1)
			call delay(0.01)
			call putPin(dcMotorPin1,0)
			dc1Flag = 1
		elseif (getPin(netPin1) = 0) then
			dc1Flag = 0
		end if

		' turn off DC motor pin
		call putPin(dcMotorPin1,0)

		call sleep(0.0)
	loop
end sub

sub LEDTask()
	do
		if (getPin(netPin1) = 1) then
			call putPin(ledPin1,1)
		else
			call putPin(ledPin1,0)
		end if

		call sleep(0.0)
	loop
end sub
