#!/usr/bin/python
import socket
import win32api
import win32gui
import win32con
import time

TCP_IP = '127.0.0.1'
TCP_PORT = 1080
BUFFER_SIZE = 1024

F_RATE = 10

EMULATOR_NAME = '5554:Hacked_ICS'

OFFSET_X = 30
OFFSET_Y = 52

MAX_WIDTH = 480
MAX_HEIGHT = 800

def getCursorPostion():
	rect = win32gui.GetWindowRect(win32gui.FindWindow(None, EMULATOR_NAME))
	x, y = win32gui.GetCursorPos()
	return x - OFFSET_X - rect[0], y - OFFSET_Y - rect[1]

def isMouseButtonDown():
	leftbutton = win32api.GetAsyncKeyState(win32con.VK_LBUTTON)
	rightbutton = win32api.GetAsyncKeyState(win32con.VK_RBUTTON)
	return leftbutton < 0 or rightbutton < 0

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))

entered = False
x = None
y = None

def sendCommand(command):
	s.send(command + "\n")
	print "Sent: " + command
	#data = s.recv(BUFFER_SIZE)
	#print "Received: ", data

print "Frame rate: " + str(F_RATE) + "Hz";

while (True):
	lastx = x
	lasty = y
	x, y = getCursorPostion()

	'''
	# mouse button is down
	if (isMouseButtonDown()):
		continue
	'''
	# mouse is not moved
	if (x == lastx and y == lasty):
		time.sleep(1.0 / F_RATE)
		continue

	if (x > 0 and x < MAX_WIDTH and y > 0 and y < MAX_HEIGHT):
		sendCommand("hover " + ("move " if entered else "enter ") + str(x) + " " + str(y))
		entered = True

	elif (entered):
		sendCommand("hover move " + str(x) + " " + str(y))
		sendCommand("hover exit " + str(x) + " " + str(y))
		entered = False
	time.sleep(1.0 / F_RATE)

s.close()
