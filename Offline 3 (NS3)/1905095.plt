set terminal pdf
set output "scratch/graph1.pdf"

set title "Graph (Bottleneck Rate vs {Througputput1 } for TCP newReno vs TCP Highspeed)"
set xlabel "Bottleneck Rate"
set ylabel "Throughput"
plot "scratch/tcphs_btnk_rate.txt" using 1:2 with lines title "Throughput1","scratch/tcphs_btnk_rate.txt" using 1:3 with lines title "Throughput2" 

set title "Graph ( Packet Loss Exponent vs {Througputput1 and Throughput2} for TCP newReno vs TCP Highspeed)"
set xlabel "Packet Loss Exponent"
set ylabel "Throughput"
plot "scratch/tcphs_packet_loss_exponent.txt" using 1:2 with lines title "Throughput1","scratch/tcphs_packet_loss_exponent.txt" using 1:3 with lines title "Throughput2" 

set title "Graph (Bottleneck Rate vs {Througputput1 and Throughput2} for TCP newReno vs TCP Westwoodplus)"
set xlabel "Bottleneck Rate"
set ylabel "Throughput"
plot "scratch/tcpwwp_btnk_rate.txt" using 1:2 with lines title "Throughput1","scratch/tcpwwp_btnk_rate.txt" using 1:3 with lines title "Throughput2" 

set title "Graph ( Packet Loss Exponent vs {Througputput1 and Throughput2} for TCP newReno vs TCP Westwoodplus)"
set xlabel "Packet Loss Exponent"
set ylabel "Throughput"
plot "scratch/tcpwwp_packet_loss_exponent.txt" using 1:2 with lines title "Throughput1","scratch/tcpwwp_packet_loss_exponent.txt" using 1:3 with lines title "Throughput2" 


set title "Graph conjestion window"
set xlabel "Simulation Time"
set ylabel "Conjestion Window"
plot "scratch/flow1.cwnd" using 1:2 with lines title "Throughput1"
