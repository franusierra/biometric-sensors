import json
import os
import datetime 

from influxdb import InfluxDBClient


def handle(req):
    """Handles the data published to the temperature topic
    Args:
        req (str): mqtt message body 
    """

    clinic_name = os.getenv("clinic_name")

    # Get influxdb local host and credentials
    influx_host_local = os.getenv("influx_host_local")
    influx_port_local = os.getenv("influx_port_local")
    influx_db_local = get_file("/var/openfaas/secrets/influxdb-database")
    influx_user_local = get_file("/var/openfaas/secrets/influxdb-username")
    influx_pass_local = get_file("/var/openfaas/secrets/influxdb-password")
    
    # Get influxdb cloud host and credentials
    influx_host_cloud = os.getenv("influx_host_cloud")
    influx_port_cloud = os.getenv("influx_port_cloud")
    influx_db_cloud = get_file("/var/openfaas/secrets/influxdb-cloud-database")
    influx_user_cloud = get_file("/var/openfaas/secrets/influxdb-cloud-username")
    influx_pass_cloud = get_file("/var/openfaas/secrets/influxdb-cloud-password")
    
    
    # Get current time formatted for influxDB
    current_time= datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")

    # Create the influxdb local client
    influx_client_local = InfluxDBClient(influx_host_local, influx_port_local, influx_user_local, influx_pass_local, influx_db_local)

    # Count the ammount of heartbeat alarms thrown in the last 30 minutes
    rs=influx_client_local.query('SELECT * FROM "alarm" WHERE time > \'{}\' - 30m'.format(current_time))
    points=list(map(transformEventPoint,list(rs.get_points())))
    
    # Create the influxdb cloud aggregation client
    influx_client_cloud = InfluxDBClient(influx_host_cloud, influx_port_cloud, influx_user_cloud, influx_pass_cloud, influx_db_cloud)
    
    # Finally, write the point to the temperature measurement
    res=influx_client_cloud.write_points(points)
    if res:
        return "{} alarms uploaded succesfully".format()
    return res

def get_file(path):
    v = ""
    with open(path) as f:
        v = f.read()
        f.close()
    return v.strip()

def transformEventPoint(data):
    return {
        "measurement":"alarm",
        "tags":{
           "patient-id":data["patient-id"],
           "clinic":data["clinic"]
        },
        "time": data["time"],
        "fields":{
            "type":data["type"],
            "meassured-value":float(data["meassured-value"])
        }
    }
