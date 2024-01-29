set terminal pdf
set output "scratch/1905095_st/static_graph.pdf"

set title "Graph (Throughput vs Number of Nodes) \nNumber of Flows = 100 \nNumber of Packets Per Second = 100 \nPacket Size = 1024 byte\nCoverage Area = 1"
set xlabel "Number of Nodes"
set ylabel "Throughput (Kbps)"
plot "scratch/1905095_st/stnode.txt" using 1:2 with lines title "Throughput"

set title "Graph (Delivery Ratio vs Number of Nodes) \nNumber of Flows = 100 \nNumber of Packets Per Second = 100 \nPacket Size = 1024 byte\nCoverage Area = 1"
set xlabel "Number of Nodes"
set ylabel "Delivery Ratio"
plot "scratch/1905095_st/stnode.txt" using 1:3 with lines title "Delivery Ratio"


set title "Graph ( Throughput vs Number of Flows) \nNumber of Nodes = 50 \nNumber of Packets Per Second = 100 \nPacket Size = 1024 byte\nCoverage Area = 1"
set xlabel "Number of Flows"
set ylabel "Throughput (Kbps)"
plot "scratch/1905095_st/stflow.txt" using 1:2 with lines title "Throughput"


set title "Graph (Delivery Ratio vs Number of Flows) \nNumber of Nodes = 50 \nNumber of Packets Per Second = 100 \nPacket Size = 1024 byte\nCoverage Area = 1"
set xlabel "Number of Flows"
set ylabel "Delivery Ratio"
plot "scratch/1905095_st/stflow.txt" using 1:3 with lines title "Delivery Ratio"


set title "Graph ( Throughput vs Number of Packets per second) \nNumber of Nodes = 50 \nNumber of Flows = 100 \nPacket Size = 1024 byte\nCoverage Area = 1"
set xlabel "Number of Packet Per Second"
set ylabel "Throughput (Kbps)"
plot "scratch/1905095_st/stpacket.txt" using 1:2 with lines title "Throughput"

set title "Graph ( Delivery Ratio vs Number of Packets per second) \nNumber of Nodes = 50 \nNumber of Flows = 100 \nPacket Size = 1024 byte\nCoverage Area = 1"
set xlabel "Number of Packet Per Second"
set ylabel "Delivery Ratio"
plot "scratch/1905095_st/stpacket.txt" using 1:3 with lines title "Delivery Ratio"

set title "Graph (Throughput vs Number of Nodes) \n Number of Nodes=50\n Number of Flows = 100 \nNumber of Packets Per Second = 100 \nPacket Size = 1024 byte\n"
set xlabel "Coverage Area"
set ylabel "Throughput (Kbps)"
plot "scratch/1905095_st/coverage.txt" using 1:2 with lines title "Throughput"


set title "Graph (Throughput vs Number of Nodes) \n Number of Nodes=50\n Number of Flows = 100 \nNumber of Packets Per Second = 100 \nPacket Size = 1024 byte\n"
set xlabel "Coverage Area"
set ylabel "Delivery Ratio"
plot "scratch/1905095_st/coverage.txt" using 1:3 with lines title "Delivery Ratio"
