import json
import os
import time 

import paho.mqtt.client as mqtt
from influxdb import InfluxDBClient


def handle(req):
    """Handles the data published to the blood-oxygen topic
    Args:
        req (str): mqtt message body 
    """
    timestamps=list()
    data = json.loads(req)
    
    
    # Get the name of the clinic
    clinic_name = os.getenv("clinic_name")
    
    # Get the duration of the data for the points
    retention_policy= os.getenv("retention_policy")

    # Get influxdb host and credentials
    influx_host = os.getenv("influx_host")
    influx_port = os.getenv("influx_port")
    influx_db = get_file("/var/openfaas/secrets/influxdb-database")
    influx_user = get_file("/var/openfaas/secrets/influxdb-username")
    influx_pass = get_file("/var/openfaas/secrets/influxdb-password")
    
    # Get broker address and the limit to throw an alarm
    broker_address= os.getenv("mosquitto_broker")
    alarm_limit = os.getenv("alarm_lower_limit")
    
    # Create the influxdb client
    influx_client = InfluxDBClient(influx_host, influx_port, influx_user, influx_pass, influx_db)

    # Get the current time in seconds
    current_time_seconds=time.time()
    
    # Get current time formatted for influxDB
    current_time=time.ctime(current_time_seconds)

    # Get current time formatted for mqtt
    current_time_milis=int(round(current_time_seconds*1000))

    if float(data["measured-value"])<float(alarm_limit):
        # Send the alarm through mqtt
        sendAlarmMQTT(broker_address,data,current_time_milis,alarm_limit)
        # Write the event point to the alarm events measurement
        influx_client.write_points([createAlarmEventPoint(data,current_time,clinic_name)])

    # Finally, write the point to the blood oxygen measurement
    res=influx_client.write_points([createBloodOxygenPoint(data,current_time,clinic_name)])

    return json.dumps(res)

def get_file(path):
    v = ""
    with open(path) as f:
        v = f.read()
        f.close()
    return v.strip()

def sendAlarmMQTT(broker_address,data,current_time,lower_limit):
    mqtt_client = mqtt.Client("blood-oxygen-function")
    mqtt_client.connect(broker_address)
    mqtt_message=json.dumps(
        {
            "patient-id" : data["patient-id"],
            "measured-value": data["measured-value"],
            "lower-limit": lower_limit,
            "alarm-time":current_time
        }
    )
    rc=mqtt_client.publish("clinic/alarms/blood-oxygen",mqtt_message)
    rc.wait_for_publish()

def createBloodOxygenPoint(data,current_time,clinic_name):
    return {
        "measurement":"blood-oxygen",
        "tags":{
            "patient-id":data["patient-id"],
            "clinic":clinic_name
        },
        "time":current_time,
        "fields" :{
            "value":float(data["measured-value"])
        }
    }

def createAlarmEventPoint(data,current_time,clinic_name):
    return {
        "measurement":"alarm",
        "tags":{
            "patient-id":data["patient-id"],
            "clinic":clinic_name
        },
        "time":current_time,
        "fields":{
            "type":"blood-oxygen",
            "meassured-value":float(data["measured-value"])
        }
    }
