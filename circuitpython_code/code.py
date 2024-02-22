import json
import supervisor
import time
import board
import neopixel
from digitalio import DigitalInOut, Pull

DEBUG = True
if DEBUG:
    import usb_cdc

led = neopixel.NeoPixel(board.NEOPIXEL, 1, brightness=0.3, auto_write=True)
led[0] = 0x000000

io_pins = {}
read_pin_states = {}

LAST_PRINT_TIME = 0

WAIT_TIME = 3.0

def LOG(message):
    if DEBUG:
        print(message)
        usb_cdc.data.write(bytes("{}\r\n".format(message), "utf-8"))


def convert_color_str(color_str):
    color_str = color_str.replace("(", "")
    color_str = color_str.replace(")", "")
    parts = color_str.split(",")
    color_tuple = tuple(int(s) for s in parts)
    return color_tuple


def turn_red():
    led[0] = (255, 0, 0)


def turn_blue():
    led[0] = (0, 0, 255)


def turn_green():
    led[0] = (0, 255, 0)


def turn_green():
    led[0] = (0, 255, 0)


def turn_color(color):
    led[0] = convert_color_str(color)


def argument_test(arg1, arg2):
    print("arg1: {} | arg2: {}".format(arg1, arg2))


def pin_command(pin_name, command):

    """
    example command:
    pin D5 1
    pin D5 0

    pin D4 R

    :param pin_name:
    :param command:
    :return:
    """
    LOG("inside pin command")

    if pin_name in dir(board):
        if command in ["1", "0", "R"]:


            reading = False
            value = None
            if command == "1":
                value = True
            elif command == "0":
                value = False
            elif command in ["r", "R"]:
                reading = True
            LOG("value: {}  reading: {}".format(value, reading))
            if pin_name in io_pins:
                if not reading:
                    io_pins[pin_name].value = value
                else:
                    if pin_name not in read_pin_states.keys():
                        io_pins[pin_name].switch_to_input(pull=Pull.DOWN)
                        read_pin_states[pin_name] = io_pins[pin_name].value

            else:
                if not reading:
                    io_pins[pin_name] = DigitalInOut(getattr(board, pin_name))
                    io_pins[pin_name].switch_to_output(value=value)
                else:
                    io_pins[pin_name] = DigitalInOut(getattr(board, pin_name))
                    io_pins[pin_name].switch_to_input(pull=Pull.DOWN)
                    read_pin_states[pin_name] = None
        else:
            LOG("Invalid pin command: {}. Must be '1', '0', or 'R'".format(command))
    else:
        LOG("Pin {} does not exist".format(pin_name))


COMMAND_MAP = {
    "red": turn_red,
    "blue": turn_blue,
    "green": turn_green,
    "color": turn_color,
    "test": argument_test,
    "pin": pin_command
}


def serial_read():
    if supervisor.runtime.serial_bytes_available:
        #LOG("serial bytes were available")
        value = input()
        LOG("you sent: {}".format(value))
        parts = value.split(" ")
        if parts[0] in COMMAND_MAP.keys():
            try:
                if len(parts) > 1:
                    args = parts[1:]
                    COMMAND_MAP[parts[0]](*args)
                else:
                    COMMAND_MAP[parts[0]]()
            except Exception as e:
                LOG("failed: %s" % value)
                LOG(e)
        else:
            LOG("Invalid Command: {}. Must be one of: {}".format(parts[0], COMMAND_MAP.keys()))


def send_pin_readings():
    values_to_send = {}
    for pin_name in read_pin_states.keys():
        cur_value = io_pins[pin_name].value
        if cur_value != read_pin_states[pin_name]:
            read_pin_states[pin_name] = cur_value
            values_to_send[pin_name] = cur_value

    if values_to_send:
        LOG("VALUES|{}".format(json.dumps(values_to_send)))
        #LOG("after send values")
        return True
    return False


while True:
    serial_read()

    if send_pin_readings():
        pass
        #LOG("after send pin readings")

    cur_time = time.monotonic()

    if LAST_PRINT_TIME + WAIT_TIME <= cur_time:
        #LOG("tick tock:  {}".format(time.monotonic()))
        LAST_PRINT_TIME = cur_time
