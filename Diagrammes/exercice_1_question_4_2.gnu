#!/usr/local/bin/gnuplot

set terminal 'qt'
set title "Nombre de messages applicatifs envoyés"
set xlabel 'Charge du Réseau'
set ylabel 'Nombre de messages'
plot [0.01:0.035] [0:5] 'exercice_1_question_4_2.dat' u 1:2  title 'token', "" u 1:3 title 'messages' with lines linewidth 1.5
