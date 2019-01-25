#! bin/python3
# -*- coding: utf-8 -*-

import os
import subprocess


def writeInFile(seed, timeCS, timeBetweenCS):
	fileOut = open("bin/projetara/config_exo1_q4.txt", "w", encoding='utf-8')
	out = "random.seed "+str(seed)+"\n\
network.size 50\n\
simulation.endtime 20000000\n\
\n\
protocol.reliableTransport UniformRandomTransport\n\
protocol.reliableTransport.mindelay 5\n\
protocol.reliableTransport.maxdelay 15\n\
\n\
protocol.basicbroadcast projetara.application.Application\n\
protocol.basicbroadcast.timeCS "+str(timeCS)+"\n\
protocol.basicbroadcast.timeBetweenCS "+str(timeBetweenCS)+"\n\
protocol.basicbroadcast.transport reliableTransport\n\
\n\
init.constantes Constantes\n\
init.constantes.loglevel WARNING\n\
\n\
control.endControler projetara.application.EndControler\n\
control.endControler.at -1\n\
control.endControler.FINAL"
	fileOut.write(out)
	fileOut.close()

def main():
	absolutePath = os.path.dirname(os.path.realpath(__file__))
	subprocess.call(absolutePath+"/javac.sh", shell=True)
	cmd = "java -cp bin/:lib/*: peersim.Simulator bin/projetara/config_exo1_q4.txt"
	timeCS = 2000
	timeBetweenCS = 1000
	for i in range(0,10):
		for j in range(0,10):
			writeInFile(j, timeCS, timeBetweenCS)
			subprocess.call(cmd, shell=True)
		timeCS -= 100
		timeBetweenCS += 100

main()
