#! bin/python3
# -*- coding: utf-8 -*-

import os
import subprocess
import numpy as np


def readFile(name):
	file = open(name)
	if file is None:
		print("ERREOR, file dosen't exist.")
	else:
		s = file.read()
		strTab = s.split("\n")
		file.close()
		return strTab

def writeInFile(seed, timeCS, timeBetweenCS):
	fileOut = open("bin/projetara/config_exo1_q4.txt", "w", encoding='utf-8')
	out = "random.seed "+str(seed)+"\n\
network.size 50\n\
simulation.endtime 100000000\n\
\n\
protocol.reliableTransport UniformRandomTransport\n\
protocol.reliableTransport.mindelay 2\n\
protocol.reliableTransport.maxdelay 2\n\
\n\
protocol.application projetara.application.Application\n\
protocol.application.timeCS "+str(timeCS)+"\n\
protocol.application.timeBetweenCS "+str(timeBetweenCS)+"\n\
protocol.application.transport reliableTransport\n\
\n\
init.constantes Constantes\n\
init.constantes.loglevel WARNING\n\
\n\
control.endControler projetara.application.EndControler\n\
control.endControler.application application\n\
control.endControler.at -1\n\
control.endControler.FINAL"
	fileOut.write(out)
	fileOut.close()

def analyseFiles(nbPoints, nbTests, listCharge):
	prefixDir = "Logs/"
	listD = os.listdir(prefixDir)
	indice = 0
	strFile = ""
	tabToken = np.ndarray(shape=(nbTests), dtype=float)
	tabMessages = np.ndarray(shape=(nbTests), dtype=float)

	for i in range(0,nbPoints):
		for j in range(0,nbTests):
			indice = (i * nbTests) + j
			if listD[indice][0] == "L":
				strFile = readFile(prefixDir+listD[indice])
				firstLine = strFile[0].split(" ")
				#print(firstLine)
				tabToken[j] = float(firstLine[0])
				tabMessages[j] = float(firstLine[1])
		print(str(listCharge[i])+"\t"+str(np.average(tabToken))+"\t"+\
			  str(np.average(tabMessages))+"\t"+\
			  str(np.std(tabToken))+"\t"+str(np.std(tabMessages)))

def nbMessages():
	nbPoints = 35
	nbTests = 8
	
	absolutePath = os.path.dirname(os.path.realpath(__file__))
	subprocess.call(absolutePath+"/javac.sh", shell=True)
	cmd = "java -cp bin/:lib/*: peersim.Simulator bin/projetara/config_exo1_q4.txt"
	min = 1000
	max = 100000
	step = 2000
	timeCS = min
	timeBetweenCS = max
	listCharge = []
	for i in range(0,nbPoints):
		for j in range(0,nbTests):
			writeInFile(j, timeCS, timeBetweenCS)
			subprocess.call(cmd, shell=True)
		timeBetweenCS -= step
		print("timeBetweenCS : "+str(timeBetweenCS))
		listCharge.append(timeCS/timeBetweenCS)
	analyseFiles(nbPoints, nbTests, listCharge)

def main():
	nbMessages()

main()
