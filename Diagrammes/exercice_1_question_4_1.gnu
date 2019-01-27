#!/usr/local/bin/gnuplot

set terminal 'qt'
set title "Temps moyen passé par chaque processus dans l'état requesting"
set xlabel 'Charge du Réseau'
set ylabel 'Temps'
set logscale x 10
plot [0.0001:100] [0:55000] 'exercice_1_question_4_1.dat' title 'temps' with lines linewidth 1.5
