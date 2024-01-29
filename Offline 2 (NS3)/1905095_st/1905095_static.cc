/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/flow-monitor-module.h" // Include FlowMonitor module
#include <fstream>


#include<iostream>
#include<vector>
#include<algorithm>

// Default Network Topology
//
//  s0              r0
//  s1              r1
//   .    x-----y   .
//   .              .
//  sn              rnLAN 10.1.2.0

using std::vector;
using namespace ns3;

NS_LOG_COMPONENT_DEFINE("ThirdScriptExample");

void
CourseChange(std::string context, Ptr<const MobilityModel> model)
{
    Vector position = model->GetPosition();
    NS_LOG_UNCOND(context <<
    " x = " << position.x << ", y = " << position.y);
}


int
main(int argc, char* argv[])
{
    bool verbose = false;
    const int tx_range = 5;
    uint32_t nNodes = 20; // 10 nodes

    uint32_t nCArea=1;

    uint32_t nFlows = 100;
    double nPackets = 100.0;
    uint32_t plot = 0; 
    bool tracing = false ;

    CommandLine cmd(__FILE__);
    cmd.AddValue("nNodes", "Number of left side wifi STA devices", nNodes);
    cmd.AddValue("nFlows", "Number of Flows", nFlows);
    cmd.AddValue("nPackets", "Number of packets to send", nPackets);
    cmd.AddValue("tracing", "Flag to enable/disable tracing", tracing);
    cmd.AddValue("verbose", "Tell echo applications to log if true", verbose);
    cmd.AddValue("nCArea", "Coverage Area", nCArea);
    cmd.AddValue("plot", "Flag to enable/disable plot", plot);
    cmd.Parse(argc, argv);

    Time::SetResolution(Time::NS);

    uint32_t nLeftNodes= nNodes/2;
    uint32_t nRightNodes = nNodes - nLeftNodes;
    uint32_t coverageArea = nCArea*tx_range; 


    if (verbose)
    {
        LogComponentEnable("UdpEchoClientApplication", LOG_INFO);
        LogComponentEnable("UdpEchoServerApplication", LOG_INFO);
    }

    NodeContainer p2pNodes;
    p2pNodes.Create(2);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("2Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));

    NetDeviceContainer p2pDevices;
    p2pDevices = pointToPoint.Install(p2pNodes);


    NodeContainer leftNodes;
    leftNodes.Create(nLeftNodes);
    NodeContainer leftApNodes = p2pNodes.Get(0);


    NodeContainer rightNodes;
    rightNodes.Create(nRightNodes);
    NodeContainer rightApNodes = p2pNodes.Get(1);

    // Physical Layer
    // YANS model - Yet Another Network Simulator
    YansWifiChannelHelper channel1 = YansWifiChannelHelper::Default();
    channel1.AddPropagationLoss("ns3::RangePropagationLossModel", "MaxRange", DoubleValue(coverageArea));
    YansWifiPhyHelper phy1;
    phy1.SetChannel(channel1.Create()); // share the same wireless medium 

    YansWifiChannelHelper channel2 = YansWifiChannelHelper::Default();
    channel2.AddPropagationLoss("ns3::RangePropagationLossModel", "MaxRange", DoubleValue(coverageArea));
    YansWifiPhyHelper phy2;
    phy2.SetChannel(channel2.Create()); // share the same wireless medium

    // Data Link Layer
    // SSid used to set the "ssid" Attribute in the mac layer implementation
    // The (SSID) is the network name used to logically 
    // identify the wireless network. 
    // Each network will have a single SSID that identifies the network, 
    // and this name will be used by clients to connect to the network.
    WifiMacHelper mac1, mac2;
    Ssid ssid1 = Ssid("ns-3-ssid1"); // creates an 802.11 service set identifier (SSID) 
    Ssid ssid2 = Ssid("ns-3-ssid2"); // creates an 802.11 service set identifier (SSID)

    WifiHelper wifi;
    // ActiveProbing false -  probe requests will not be sent by MACs created by this
    // helper, and stations will listen for AP beacons.
    NetDeviceContainer leftDevices;
    mac1.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssid1), "ActiveProbing", BooleanValue(false));
    leftDevices = wifi.Install(phy1, mac1, leftNodes)    ;

    NetDeviceContainer rightDevices;
    mac2.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssid2), "ActiveProbing", BooleanValue(false));
    rightDevices = wifi.Install(phy2, mac2, rightNodes);


    NetDeviceContainer leftApDevices;
    mac1.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssid1));
    leftApDevices = wifi.Install(phy1, mac1, leftApNodes);

    NetDeviceContainer rightApDevices;
    mac2.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssid2));
    rightApDevices = wifi.Install(phy2, mac2, rightApNodes);

    MobilityHelper mobility;

    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX",
                                  DoubleValue(0.0),
                                  "MinY",
                                  DoubleValue(0.0),
                                  "DeltaX",
                                  DoubleValue(1.0),
                                  "DeltaY",
                                  DoubleValue(1.0),
                                  "GridWidth",
                                  UintegerValue(10),
                                  "LayoutType",
                                  StringValue("RowFirst"));

    
        // mobility.SetMobilityModel("ns3::RandomWalk2dMobilityModel",
        //                       "Bounds",
        //                       RectangleValue(Rectangle(-(5*nodecnt),(5*nodecnt), -5*nodecnt,5*nodecnt)),
        //                      "Speed", StringValue ("ns3::ConstantRandomVariable[Constant="+std::to_string(nSpeed)+"]"));

    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    
    mobility.Install(leftApNodes);
    mobility.Install(leftNodes);

    mobility.Install(rightApNodes); 
    mobility.Install(rightNodes);

    InternetStackHelper stack;
    stack.Install(leftApNodes);
    stack.Install(leftNodes);
    stack.Install(rightApNodes);
    stack.Install(rightNodes);

    Ipv4AddressHelper address;

    address.SetBase("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer p2pInterfaces;
    p2pInterfaces = address.Assign(p2pDevices);


    address.SetBase("10.1.2.0", "255.255.255.0");
    Ipv4InterfaceContainer rightInterfaces =  address.Assign(rightDevices);
    Ipv4InterfaceContainer rightApInterfaces = address.Assign(rightApDevices);

    address.SetBase("10.1.3.0", "255.255.255.0"); 
    Ipv4InterfaceContainer leftInterfaces = address.Assign(leftDevices);
    Ipv4InterfaceContainer leftApInterfaces = address.Assign(leftApDevices);

    UdpEchoServerHelper echoServer(9);

    ApplicationContainer serverApps = echoServer.Install(rightNodes);
    serverApps.Start(Seconds(1.0));
    serverApps.Stop(Seconds(10.0));

    vector<UdpEchoClientHelper> echoClients; 

    for(int i=0; i<nRightNodes; i++)
    {
        echoClients.push_back(UdpEchoClientHelper(rightInterfaces.GetAddress(i), 9));
        // echoClients[i].SetAttribute("MaxPackets", UintegerValue(2));
        echoClients[i].SetAttribute("Interval", TimeValue(Seconds(1.0/nPackets)));
        echoClients[i].SetAttribute("PacketSize", UintegerValue(1024));
    }

    int flowPerNode;
    if(nFlows%nLeftNodes==0){
        flowPerNode = nFlows/nLeftNodes;
    }
    else{
        flowPerNode = nFlows/nLeftNodes + 1;
    }
    int flowcnt = 0; 
    for(int i=0; i<nLeftNodes; i++){
        for(int j=0; j<flowPerNode; j++){
            int randsv = random()%nRightNodes;
            ApplicationContainer clientApps = echoClients[randsv].Install(leftNodes.Get(i));
            clientApps.Start(Seconds(2.0));
            clientApps.Stop(Seconds(10.0));
            flowcnt++; 
        }
        if(flowcnt==nFlows) break; 
    }



    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    Simulator::Stop(Seconds(10.0));

    if (tracing)
    {
        phy1.SetPcapDataLinkType(WifiPhyHelper::DLT_IEEE802_11_RADIO);
        phy2.SetPcapDataLinkType(WifiPhyHelper::DLT_IEEE802_11_RADIO);

        pointToPoint.EnablePcapAll("scratch/1905095_st/1905095_static");
        phy1.EnablePcap("scratch/1905095_st/1905095_static", leftApDevices.Get(0));
        phy2.EnablePcap("scratch/1905095_st/1905095_static", rightApDevices.Get(0));

    }

    FlowMonitorHelper flowMonitor;
    Ptr<FlowMonitor> monitor = flowMonitor.InstallAll ();

    Simulator::Run();

    monitor->CheckForLostPackets();
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    double tot_r_bits=0.0;
    int tot_pckt_sent=0;
    int tot_pckt_received=0;
    for(FlowMonitor::FlowStatsContainer::const_iterator it = stats.begin(); it!=stats.end(); ++it){
        tot_r_bits += it->second.rxBytes*8.0;
        tot_pckt_sent += it->second.txPackets;
        tot_pckt_received += it->second.rxPackets;
    }
    double throughput= tot_r_bits/(1e3*10.0);
    std::cout<<"Throughput "<<throughput<<" kbps"<<std::endl; 
    
    double pd_ratio = static_cast<double>(tot_pckt_received)/static_cast<double>(tot_pckt_sent);
    std::cout<<"Packet Delivery Ratio = "<<pd_ratio<<std::endl;
    pd_ratio*=100.0;
    if(plot==1){
        std::ofstream outputFile("scratch/1905095_st/stnode.txt", std::ios::app);
        outputFile<<nNodes<<"\t"<<throughput<<"\t"<<pd_ratio<<"\n";
        outputFile.close();
    }
    else if(plot==2){
        std::ofstream outputFile("scratch/1905095_st/stflow.txt", std::ios::app);
        outputFile<<nFlows<<"\t"<<throughput<<"\t"<<pd_ratio<<"\n";
        outputFile.close();
    }
    else if(plot==3){
        std::ofstream outputFile("scratch/1905095_st/stpacket.txt", std::ios::app);
        outputFile<<nPackets<<"\t"<<throughput<<"\t"<<pd_ratio<<"\n";
        outputFile.close();
    }
    else if(plot==4){
        std::ofstream outputFile("scratch/1905095_st/coverage.txt", std::ios::app);
        outputFile<<nCArea<<"\t"<<throughput<<"\t"<<pd_ratio<<"\n";
        outputFile.close();
    }
    else if(plot==5){
        // std::ofstream outputFile("scratch/1905095_st/speed.txt", std::ios::app);
        // outputFile<<nSpeed<<"\t"<<throughput<<"\t"<<pd_ratio<<"\n";
        // outputFile.close(); 
    }


    Simulator::Destroy();
    return 0;
}