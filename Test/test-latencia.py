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



# Codigo para capturar la señal de salida
exit_event = threading.Event()
def signal_handler(signum, frame):
    exit_event.set()
signal.signal(signal.SIGINT, signal_handler)

# Variable global con la barrera que determina que los publishers han terminado 
ended_pubs_lock = threading.Lock()
ended_pubs = 0

# Colecciones globales necesarias
pubs=dict()
responsetimestamps=defaultdict(dict)



def createPublishers(broker,port,npubs,prefix,verbose):
    if verbose:
        print("Comenzando conexión de publishers")
    publishers=pubs
    for i in range(1,npubs+1):
        botname=f"{prefix}{i}"
        if verbose:
            print(f"Conectando publisher: {botname}")
        # Crear cliente
        pub_client= paho.Client(botname)
        if port is not None:
            pub_client.connect(broker,port) 
        else:
            pub_client.connect(broker)
        publishers[botname]={
            "client":pub_client,
            "timestamps":dict()
        }
    if verbose:
        print("========================================================================")
        
    return publishers

def loadTestingThread(client,timestamps,duration,rate,patient_id,verbose):
    counter=1
    iterations=duration/rate
    while counter<=iterations:
        msg={
        "patient-id":patient_id,
        "measured-value":counter
        }
        ret= client.publish("clinic/sensors/oxymeter",json.dumps(msg))     
        timestamp=time.time()
        timestamps[msg["measured-value"]]=timestamp
        if verbose:
            print(f"Publicado mensaje paciente {patient_id} Hora: {time.ctime(time.time())}",msg)

        counter+=1
        time.sleep(rate)
        if exit_event.is_set():
            break

    print(f"Desconectando cliente {patient_id} Hora: {time.ctime(time.time())}")
    client.disconnect()
    global ended_pubs_lock
    global ended_pubs
    with ended_pubs_lock:
        if ended_pubs is not None:
            ended_pubs +=1
        else:
            ended_pubs=0

def loadTesting(pubs,duration,rate,verbose):
    counter=1
    total_pubs=len(pubs)
    for k,v in pubs.items():
        client=v["client"]
        timestamps=v["timestamps"]
        patient_id=k
        t=threading.Thread(target=loadTestingThread,args=(client,timestamps,duration,rate,patient_id,verbose))
        t.start()    
        v["thread"]=t
        time.sleep(rate/total_pubs)

def alarmaRecibida(client, userdata, message):
    result=json.loads(message.payload.decode("utf-8"))
    current_timestamps=responsetimestamps[result["patient-id"]]
    current_message=int(result["measured-value"])
    current_timestamps[current_message]=time.time()
    print("Recibido: ",result)

def subscriberThread(sub,delay):
    while True:
        sub.loop()
        time.sleep(0.1)
        if exit_event.is_set() or (ended_pubs >= len(pubs)):
            time_left=delay
            while time_left>0:
                print(f"Esperando {time_left}s por las alarmas pendientes")
                time.sleep(1)
                time_left=time_left-1
                sub.loop()
            break
    sub.disconnect()
    latencias_finales=dict()
    print("========================================================================")
    for patient,timestamps in responsetimestamps.items():
        enviados=pubs[patient]["timestamps"]
        recibidos=timestamps
        print(f'Mensajes enviados paciente {patient}: {len(enviados)}')        
        print(f'Mensajes recibidos paciente {patient}: {len(recibidos)}')       
        print(f'Alarmas perdidas: {len(enviados)-len(recibidos)}') 
        latencias=list()
        latencias_finales[patient]=latencias
        for k,v in timestamps.items():
            latencias.append(v-enviados[k])
        print(f'Latencias del paciente: {latencias}')
        print(f'Latencia media: {round(sum(latencias)/len(latencias),3)}s')
        print("========================================================================")
    df = pd.DataFrame(latencias_finales.values())
    answer = dict(df.mean())
    plt.plot(answer.values())
    plt.ylim(0,0.2)
    labels = range(len(answer.values()))
    plt.xticks(labels)

    plt.savefig(f"results-{time.ctime(time.time())}.png")
    
    print(answer)
    
def createSubscriber(broker,port,topic,delay):
    sub=paho.Client("test")
    sub.connect(broker,port)
    sub.on_message=alarmaRecibida
    sub.subscribe(topic)
    t=threading.Thread(target=subscriberThread,args=(sub,delay))
    t.start()

if __name__ == "__main__":
    # Parsear los parametros
    parser= argparse.ArgumentParser(description="Load testing para clinica mqtt")
    parser.add_argument('broker',metavar="broker",help="Dirección del broker para el load testing")
    parser.add_argument('port',metavar="port",nargs="?",help="Puerto para el broker del load testing",default=1883)
    #parser.add_argument('-s',dest="suscribers",help="Cantidad de subscriptores para los topics",type=int,default=1)
    parser.add_argument('-p',dest="publishers",help="Cantidad de publicadores para los topics",type=int,default=1)
    parser.add_argument('-r',dest="rate",help="Latencia en segundos de los mensajes de cada cliente",type=int,default=30)
    parser.add_argument('-d',dest="duration",help="Duracion del load testing",type=int,default=600)
    parser.add_argument('-w',dest="wait",help="Tiempo de espera al final del load testing por las ultimas alarmas",type=int,default=60)
    #parser.add_argument('-a',dest="alarm_rate",help="Frecuencia de mensajes con alarmas en ellos",type=int,default=15)
    #parser.add_argument('--subscriber-prefix',dest="prefix_sub",help="Set the prefix for the sub bot name",default="test-bot-sub-")
    parser.add_argument('--publisher-prefix',dest="prefix_pub",help="Set the prefix for the pub bot name and patient-ids",default="test-bot-")
    parser.add_argument('-v',dest="verbose",action="store_true",help="Add more debugging messages",default=False)
    args=parser.parse_args()
    print("========================================================================")
    print(f'Iniciando load testing de {args.duration} segundos en {args.broker}:{args.port}...')
    print("========================================================================")
    #print(f'Cantidad de suscriptores: {args.suscribers}')
    print(f'Cantidad de publishers: {args.publishers}')
    print(f'Latencia de los mensajes: Cada {args.rate} segundos')
    print("========================================================================")
    
    # Extraer direccion y puerto (opcional) del endpoint
    
    broker=args.broker
    port=args.port
    
    
    createSubscriber(broker,port,"clinic/alarms/blood-oxygen",args.rate)
    pubs=createPublishers(args.broker,args.port,args.publishers,args.prefix_pub,args.verbose)
    loadTesting(pubs,args.duration,args.rate,args.verbose)
   
    
    
    

