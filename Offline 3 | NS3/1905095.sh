
rm "scratch/tcphs_btnk_rate.txt"
for i in {1..6}
do
    j=50
    ./ns3 run "scratch/1905095.cc --bottle_neck_rate=$((j*i)) --plot=1 --tcp_type=0"
done

rm "scratch/tcphs_packet_loss_exponent.txt"
for i in {2..6}
do
    ./ns3 run "scratch/1905095.cc --plossRate=$i --plot=2 --tcp_type=0"
done

rm "scratch/tcpwwp_btnk_rate.txt"
for i in {1..6}
do
    j=50
    ./ns3 run "scratch/1905095.cc --bottle_neck_rate=$((j*i)) --plot=1 --tcp_type=2"
done


rm "scratch/tcpwwp_packet_loss_exponent.txt"
for i in {2..6}
do
    ./ns3 run "scratch/1905095.cc --plossRate=$i --plot=2 --tcp_type=2"
done

gnuplot "scratch/1905095.plt"

rm "scratch/tcpar_btnk_rate.txt"
for i in {1..6}
do
    j=50
    ./ns3 run "scratch/1905095.cc --bottle_neck_rate=$((j*i)) --plot=1 --tcp_type=1"
done


rm "scratch/tcpar_packet_loss_exponent.txt"
for i in {2..6}
do
    ./ns3 run "scratch/1905095.cc --plossRate=$i --plot=2 --tcp_type=1"
done
