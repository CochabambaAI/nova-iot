import RPi.GPIO as GPIO
from flask import Flask

app = Flask(__name__)

LED=17
GPIO.setmode(GPIO.BCM)
GPIO.setup(LED, GPIO.OUT)

@app.route("/light/on")
def lightOn():
    GPIO.output(LED, 1)
    return "on"

@app.route("/light/off")
def lightOff():
    GPIO.output(LED, 0)
    return "off"

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=3000, debug=False)
