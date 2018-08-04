#!/usr/bin/python           # This is server.py file
import time
import socket               # Import socket module
import nlp
import sys

def writeToClient(string):
   sys.stdout.write(string + "\n")
   sys.stdout.flush()


s = socket.socket()         # Create a socket object
bound = False
host = socket.gethostname() # Get local machine name
port = 37492                # Reserve a port for your service.
while (not bound):
    try:
        s.bind((host, port))
        bound = True
    except socket.error as (errNum,errString):
        if (errNum == 10048 or errNum == 98) :
            port += 1
            continue
        else:
            sys.exit(1);
writeToClient(str(port))
s.listen(5)                 # Now wait for client connection.
c, addr = s.accept()     # Establish connection with client.
processor = nlp.NLP()
writeToClient("Ready")
while True:
    data = c.recv(4096)
    print ("ok")
    if not data :
        print '\nDisconnected from server'
        break
    else :
        print ("Got")
        result = processor.extract(data)
        if (result == None):
            c.send("-1:Failed to find Keywords\n")
            print "failed"
        else :        
            c.send("0:" + result + "\n")
    time.sleep(.500)
c.close()
s.close();
