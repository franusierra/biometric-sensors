import json
import os
import datetime 

from influxdb import InfluxDBClient


def handle(req):
    """Handles the data published to the temperature topic
    Args:
        req (str): mqtt message body 
    """
    # Get the name of the clinic
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

    bo_aggregation=fetchAggregation(influx_client_local,'blood-oxygen',current_time,clinic_name)
    hb_aggregation=fetchAggregation(influx_client_local,'heartbeat',current_time,clinic_name)
    t_aggregation=fetchAggregation(influx_client_local,'temperature',current_time,clinic_name)

    # Create the influxdb cloud aggregation client
    influx_client_cloud = InfluxDBClient(influx_host_cloud, influx_port_cloud, influx_user_cloud, influx_pass_cloud, influx_db_cloud)
    
    # Finally, write the point to the temperature measurement
    bo_res=influx_client_cloud.write_points(bo_aggregation)
    hb_res=influx_client_cloud.write_points(hb_aggregation)
    t_res=influx_client_cloud.write_points(t_aggregation)
    if bo_res and hb_res and t_res:
        return "Succesfull aggregation"
    else:
        return "Aggregation failed"

def get_file(path):
    v = ""
    with open(path) as f:
        v = f.read()
        f.close()
    return v.strip()
def transformAggregation(point,current_time,clinic_name,sensor_type):
    fields=dict(zip(point['columns'],point['values'][0]))
    fields.pop('time',None)
    return {
        "measurement":sensor_type,
        "tags":{
            "patient-id":point["tags"]["patient-id"],
            "clinic":clinic_name
        },
        "time":current_time,
        "fields" : fields
    }

def fetchAggregation(influx_client_local,sensor_type,current_time,clinic_name):
    # Count the ammount of blood-oxygen alarms thrown in the last 30 minutes
    rs=influx_client_local.query(' '.join(('SELECT mean("value"), max("value"), min("value")',
                                        'FROM "{}"'.format(sensor_type),
                                        'WHERE time > \'{}\' - 30m'.format(current_time),
                                        'GROUP BY "patient-id"')))
    json_results=rs.raw
    series=json_results["series"]
    formatted_points=list()
    for p in series:
        formatted_points.append(transformAggregation(p,current_time,clinic_name,sensor_type))
    return formatted_points


