#!/usr/bin/python3
import paho.mqtt.client as paho
import json
import time
import threading
import signal
import sys
import argparse
from collections import defaultdict 
import matplotlib.pyplot as plt
from collections import Counter
import pandas as pd
import random



# Codigo para capturar la señal de salida
exit_event = threading.Event()
def signal_handler(signum, frame):
    exit_event.set()
signal.signal(signal.SIGINT, signal_handler)

def createPublishers(broker,port,npubs,prefix,verbose):
    if verbose:
        print("Comenzando conexión de publishers")
    publishers=dict()
    for i in range(1,npubs+1):
        botname="test-bot-{:03d}".format(i)
        if verbose:
            print(f"Conectando publisher: {botname}")
        # Crear cliente
        pub_client= paho.Client(botname)
        if port is not None:
            pub_client.connect(broker,port) 
        else:
            pub_client.connect(broker)
        publishers[botname]=pub_client
    if verbose:
        print("========================================================================")
        
    return publishers

def resourceTestingThread(client,duration,rate,patient_id,verbose):
    counter=1
    iterations=duration/rate
    while counter<=iterations:
        msg={
            "patient-id":patient_id,
            "measured-value":random.randint(95,99)
        }
        if (counter%20)==5:
            msg["measured-value"]=83
        ret= client.publish("clinic/sensors/oxymeter",json.dumps(msg))     
        msg={
            "patient-id":patient_id,
            "measured-value":random.randint(40,100)
        }
        if (counter%20)==10:
            msg["measured-value"]=130
        ret= client.publish("clinic/sensors/heartbeat",json.dumps(msg))  
        msg={
        "patient-id":patient_id,
        "measured-value": random.uniform(35.5,38.0)
        }
        if (counter%20)==15:
            msg["measured-value"]=39.5
        
        ret= client.publish("clinic/sensors/temperature",json.dumps(msg))  
        if verbose:
            print(f"Publicados mensajes paciente {patient_id} Hora: {time.ctime(time.time())}")

        counter+=1
        time.sleep(rate)
        if exit_event.is_set():
            break

    print(f"Desconectando cliente {patient_id} Hora: {time.ctime(time.time())}")
    client.disconnect()

def resourceTesting(pubs,duration,rate,verbose):
    counter=1
    total_pubs=len(pubs)
    for k,v in pubs.items():
        client=v
        patient_id=k
        t=threading.Thread(target=resourceTestingThread,args=(client,duration,rate,patient_id,verbose))
        t.start()    
        time.sleep(rate/total_pubs)


if __name__ == "__main__":
    # Parsear los parametros
    parser= argparse.ArgumentParser(description="Resource testing para clinica mqtt")
    parser.add_argument('broker',metavar="broker",help="Dirección del broker para el resource testing")
    parser.add_argument('port',metavar="port",nargs="?",help="Puerto para el broker del resource testing",default=1883)
    parser.add_argument('-p',dest="publishers",help="Cantidad de publicadores para los topics",type=int,default=1)
    parser.add_argument('-r',dest="rate",help="Latencia en segundos de los mensajes de cada cliente",type=int,default=30)
    parser.add_argument('-d',dest="duration",help="Duracion del resource testing",type=int,default=600)
    parser.add_argument('-w',dest="wait",help="Tiempo de espera al final del resource testing por las ultimas alarmas",type=int,default=60)
    parser.add_argument('--publisher-prefix',dest="prefix_pub",help="Set the prefix for the pub bot name and patient-ids",default="test-bot-")
    parser.add_argument('-v',dest="verbose",action="store_true",help="Add more debugging messages",default=False)
    args=parser.parse_args()
    print("========================================================================")
    print(f'Iniciando resource testing de {args.duration} segundos en {args.broker}:{args.port}...')
    print("========================================================================")
    print(f'Cantidad de publishers: {args.publishers}')
    print(f'Latencia de los mensajes: Cada {args.rate} segundos')
    print("========================================================================")
     
    broker=args.broker
    port=args.port

    pubs=createPublishers(args.broker,args.port,args.publishers,args.prefix_pub,args.verbose)
    resourceTesting(pubs,args.duration,args.rate,args.verbose)
   
    
    
    

