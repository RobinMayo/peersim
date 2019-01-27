#!/usr/local/bin/gnuplot

set terminal 'qt'
set title "Temps passé par le jeton dans chacun de ses états"
set xlabel 'Charge du Réseau'
set ylabel 'Pourcentage de temps que le jeton passe dans chaque état'
set logscale x 10
plot [0.0001:1] [-1:110] 'exercice_1_question_4_3.dat' u 1:2 title 'token inutilisé' with lines linewidth 1.5, "" u 1:3 title 'token utilisé' with lines linewidth 1.5, "" u 1:4 title 'token en cours de transmission' with lines linewidth 1.5
