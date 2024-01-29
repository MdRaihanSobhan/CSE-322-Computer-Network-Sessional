/*
 *
 * Network topology:
 *
 * S0-------------R1-------------R2--------------Rec
 *      10Mbps          1 Mbps          10Mbps
 *        2ms             5ms             2ms
 * Calculate throughput for this network at 5ms interval and plot a throughput vs time graph
 */
#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/stats-module.h"
#include <fstream>

NS_LOG_COMPONENT_DEFINE("wifi-practice");

using namespace ns3;

Ptr<PacketSink> sink;     //!< Pointer to the packet sink application
uint64_t lastTotalRx = 0; //!< The value of the last total received bytes
Ptr<OutputStreamWrapper> stream;
/**
 * Calculate the throughput
 */
void
CalculateThroughput()
{
    Time now = Simulator::Now(); /* Return the simulator's virtual time. */
    double cur = (sink->GetTotalRx() - lastTotalRx) * 8.0 /
                 1e6 / 0.5; /* Convert Application RX Packets to MBits. */
    // std::cout << now.GetSeconds() << "s: \t" << cur << " Mbit/s" << std::endl;
    NS_LOG_UNCOND(now.GetSeconds() << "s: \t" << cur << " Mbit/s");
    *stream->GetStream() << now.GetSeconds() << "\t" << cur  << std::endl;
    lastTotalRx = sink->GetTotalRx();
    // std::cout << "Sink total received : " << sink->GetTotalRx() << std::endl;
    Simulator::Schedule(MilliSeconds(500), &CalculateThroughput);
}

int
main(int argc, char* argv[])
{
    uint32_t payloadSize = 1472;           /* Transport layer payload size in bytes. */
    std::string dataRate = "100Mbps";      /* Application layer datarate. */
    double simulationTime = 10;            /* Simulation time in seconds. */
    
    AsciiTraceHelper asciiTraceHelper;
    stream  = asciiTraceHelper.CreateFileStream("practice.dat");
    // changing error_p for increasing error rate in error model - drop more packets - Mashiat
    // double error_p = 0.0001;
    /* Command line argument parser setup. */
    CommandLine cmd(__FILE__);

    cmd.Parse(argc, argv);


    /* Configure TCP Options */
    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(payloadSize));

  

    // Create gateways, sources, and sinks
    
    NodeContainer senders;
    senders.Create (2);
    NodeContainer receivers;
    receivers.Create (2);

    NodeContainer gateway;
    gateway.Add( senders.Get(1) );
    gateway.Add( receivers.Get(0) );

    // Ptr<RateErrorModel> em = CreateObject<RateErrorModel>();
    // em->SetAttribute("ErrorRate", DoubleValue(0.00001));

    PointToPointHelper bottleneck;
    bottleneck.SetDeviceAttribute ("DataRate", 
    StringValue ("1Mbps"));
    bottleneck.SetChannelAttribute ("Delay", 
    StringValue ("5ms"));
    // bottleneck.SetDeviceAttribute ("ReceiveErrorModel", PointerValue(em));


    // Hold the PointToPointNetDevices created
    NetDeviceContainer bottleneckDevices;
    bottleneckDevices = bottleneck.Install(gateway);

    PointToPointHelper p2p;
    p2p.SetDeviceAttribute ("DataRate", 
    StringValue ("10Mbps"));
    p2p.SetChannelAttribute ("Delay", 
    StringValue ("2ms"));

    NetDeviceContainer senderDevices, receiverDevices;
    senderDevices = p2p.Install(senders);
    receiverDevices = p2p.Install(receivers);

    InternetStackHelper stack;
    stack.Install(senders);
    stack.Install(receivers);

    Ipv4AddressHelper address;
    address.SetBase("10.1.1.0", "255.255.255.0"); 
    Ipv4InterfaceContainer sender_interfaces = address.Assign(senderDevices);

    address.SetBase("10.1.2.0", "255.255.255.0"); 
    Ipv4InterfaceContainer bottleneck_interfaces = address.Assign(bottleneckDevices); 

    address.SetBase("10.1.3.0", "255.255.255.0"); 
    Ipv4InterfaceContainer receiver_interfaces = address.Assign(receiverDevices);


    /* Populate routing table */
    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    /* Install TCP Receiver on the access point */
    PacketSinkHelper sinkHelper("ns3::TcpSocketFactory",
                                InetSocketAddress(Ipv4Address::GetAny(), 9));
    ApplicationContainer sinkApp = sinkHelper.Install(receivers.Get(1));
    sink = StaticCast<PacketSink>(sinkApp.Get(0));

    /* Install TCP/UDP Transmitter on the station */
    OnOffHelper sender_helper("ns3::TcpSocketFactory", 
            (InetSocketAddress(
                receiver_interfaces.GetAddress(1), 9)));
    sender_helper.SetAttribute("PacketSize", 
        UintegerValue(payloadSize));
    sender_helper.SetAttribute("OnTime", StringValue(
        "ns3::ConstantRandomVariable[Constant=1]"));
    sender_helper.SetAttribute("OffTime", StringValue(
        "ns3::ConstantRandomVariable[Constant=0]"));
    sender_helper.SetAttribute("DataRate", DataRateValue(DataRate(dataRate)));
    ApplicationContainer senderApp = sender_helper.Install(
        senders.Get(0));

    /* Start Applications */
    sinkApp.Start(Seconds(0.0));
    senderApp.Start(Seconds(1.0));
    Simulator::Schedule(Seconds(1.5), &CalculateThroughput);
  

    /* Start Simulation */
    Simulator::Stop(Seconds(simulationTime + 1));
    Simulator::Run();

    double averageThroughput = ((sink->GetTotalRx() * 8) / (1e6 * simulationTime));

    Simulator::Destroy();
    std::cout << "\nAverage throughput: " << averageThroughput << " Mbit/s" << std::endl;
    return 0;
}

// gnuplot> set terminal png size 640,480
// gnuplot> set output "throughput_practice.png"
// gnuplot> plot "practice.dat" using 1:2 title 'Time VS Throughput' with linespoints
// gnuplot> exit

