
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fstream>
#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-layout-module.h"
#include "ns3/applications-module.h"
#include "ns3/stats-module.h"
#include "ns3/callback.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/csma-module.h"

using namespace ns3;


// ===========================================================================
//
//              s0---                       -----r0
//                     10 Mbps - 1 Gbps
//   senders -              x --- y                 -receivers
//                       RTT =  100ms
//              s1---                        -----r1
//
// ===========================================================================
class TutorialApp : public Application
{
public:
  TutorialApp ();
  virtual ~TutorialApp ();
  static TypeId GetTypeId (void);
  void Setup (Ptr<Socket> socket, Address address, uint32_t packetSize, DataRate dataRate, uint32_t simultime);

private:
  virtual void StartApplication (void);
  virtual void StopApplication (void);

  void ScheduleTx (void);
  void SendPacket (void);

  Address         m_peer;
  uint32_t        m_packetSize;
  Ptr<Socket>     m_socket;
  DataRate        m_dataRate;
  EventId         m_sendEvent;
  bool            m_running;
  uint32_t        m_packetsSent;
  uint32_t        m_simultime;
};

TutorialApp::TutorialApp ()
  : m_peer (),
    m_packetSize (0),
    m_socket (0),
    m_dataRate (0),
    m_sendEvent (),
    m_running (false),
    m_packetsSent (0),
    m_simultime (0)
{
}

TutorialApp::~TutorialApp ()
{
  m_socket = 0;
}

TypeId TutorialApp::GetTypeId (void)
{
  static TypeId tid = TypeId ("TutorialApp")
    .SetParent<Application> ()
    .SetGroupName ("Tutorial")
    .AddConstructor<TutorialApp> ()
    ;
  return tid;
}

void
TutorialApp::Setup (Ptr<Socket> socket, Address address, uint32_t packetSize, DataRate dataRate, uint32_t simultime)
{
  m_socket = socket;
  m_peer = address;
  m_packetSize = packetSize;
  m_dataRate = dataRate;
  m_simultime = simultime;
}

void
TutorialApp::StartApplication (void)
{
  m_running = true;
  m_packetsSent = 0;
    if (InetSocketAddress::IsMatchingType (m_peer))
    {
      m_socket->Bind ();
    }
  else
    {
      m_socket->Bind6 ();
    }
  m_socket->Connect (m_peer);
  SendPacket ();
}

void
TutorialApp::StopApplication (void)
{
  m_running = false;

  if (m_sendEvent.IsRunning ())
    {
      Simulator::Cancel (m_sendEvent);
    }

  if (m_socket)
    {
      m_socket->Close ();
    }
}

void
TutorialApp::SendPacket (void)
{
  Ptr<Packet> packet = Create<Packet> (m_packetSize);
  m_socket->Send (packet);

  if(Simulator::Now().GetSeconds() < m_simultime) ScheduleTx();
}

void
TutorialApp::ScheduleTx (void)
{
  if (m_running)
    {
      Time tNext (Seconds (m_packetSize * 8 / static_cast<double> (m_dataRate.GetBitRate ())));
      m_sendEvent = Simulator::Schedule (tNext, &TutorialApp::SendPacket, this);
    }
}

static void
CwndChange (Ptr<OutputStreamWrapper> stream, uint32_t oldCwnd, uint32_t newCwnd)
{
  *stream->GetStream () << Simulator::Now ().GetSeconds () << " " << newCwnd << std::endl;
}

int bottle_neck_rate = 50;
int nLeaf = 2;
int nFlows = 2;
int bottle_neck_delay = 100; 
int packet_loss_exponent = 6;
std::string senderDataRate = "1Gbps";
std::string senderDelay = "1ms";
std::string directory_path = "scratch/";
int simul_time = 60;
uint32_t plot = 0; 
uint32_t tcp_type = 0; 

int main(int argc, char *argv[]){
    uint32_t payloadSize = 1472;
    std::string transport_protocol1 = "ns3::TcpNewReno"; // TcpNewReno
    std::string transport_protocol2 = "ns3::TcpHighSpeed"; //TcpAdaptiveReno TcpWestwood, TcpHighSpeed
    int exponents = 1;
    CommandLine cmd (__FILE__);
    cmd.AddValue ("nLeaf","Number of left and right side leaf nodes", nLeaf);
    cmd.AddValue ("bottle_neck_rate","Max Packets allowed in the device queue", bottle_neck_rate);
    cmd.AddValue ("plossRate", "Packet loss rate", packet_loss_exponent);
    cmd.AddValue ("exponents","1 for bttlnck, 2 for packet loss rate", exponents);
    cmd.AddValue ("simTime","Simulation time in seconds", simul_time);
    cmd.AddValue("plot", "Flag to enable/disable plot", plot);
    cmd.AddValue("tcp_type", "Flag to enable/disable plot", tcp_type);

    cmd.Parse (argc,argv);

    if(tcp_type==0){
        transport_protocol2 = "ns3::TcpHighSpeed"; 
    }
    else if(tcp_type==1){
        transport_protocol2 = "ns3::TcpAdaptiveReno";
    }
    else if(tcp_type==2){
        transport_protocol2 = "ns3::TcpWestwoodPlus";
    }

    if(nFlows != nLeaf){
        nFlows = nLeaf;
    }
    double packet_loss_rate = (1.0 / std::pow(10, packet_loss_exponent));
    std::string bottleNeckDataRate = std::to_string(bottle_neck_rate) + "Mbps";
    std::string bottleNeckDelay = std::to_string(bottle_neck_delay) + "ms";

    Config::SetDefault ("ns3::TcpSocket::SegmentSize", UintegerValue (payloadSize));

    PointToPointHelper bottleNeckLink;

    bottleNeckLink.SetDeviceAttribute  ("DataRate", StringValue (bottleNeckDataRate));
    bottleNeckLink.SetChannelAttribute ("Delay", StringValue (bottleNeckDelay));


    PointToPointHelper pointToPointLeaf;
    pointToPointLeaf.SetDeviceAttribute  ("DataRate", StringValue (senderDataRate));
    pointToPointLeaf.SetChannelAttribute ("Delay", StringValue (senderDelay));
    pointToPointLeaf.SetQueue ("ns3::DropTailQueue", "MaxSize", StringValue (std::to_string (bottle_neck_delay * bottle_neck_rate) + "p"));

    PointToPointDumbbellHelper d (nLeaf, pointToPointLeaf, nLeaf, pointToPointLeaf, bottleNeckLink);

    Ptr<RateErrorModel> em = CreateObject<RateErrorModel> ();
    em->SetAttribute ("ErrorRate", DoubleValue (packet_loss_rate));
    d.m_routerDevices.Get(1)->SetAttribute ("ReceiveErrorModel", PointerValue (em)); 

    Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue (transport_protocol1));
    InternetStackHelper stack1;
    stack1.Install (d.GetLeft (0)); // left leaf
    stack1.Install (d.GetRight (0)); // right leaves

    stack1.Install (d.GetLeft ()); // left routers
    stack1.Install (d.GetRight ()); // right routers

    Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue (transport_protocol2));
    InternetStackHelper stack2;
            
    stack2.Install (d.GetLeft (1)); // left leaf
    stack2.Install (d.GetRight (1)); // right leaf

    d.AssignIpv4Addresses (Ipv4AddressHelper ("10.1.1.0", "255.255.255.0"), // left nodes
                          Ipv4AddressHelper ("10.2.1.0", "255.255.255.0"),  // right nodes
                          Ipv4AddressHelper ("10.3.1.0", "255.255.255.0")); // routers 
    Ipv4GlobalRoutingHelper::PopulateRoutingTables (); // populate routing table
    
    FlowMonitorHelper flowmonitor;
    flowmonitor.SetMonitorAttribute("MaxPerHopDelay", TimeValue(Seconds(2.0)));
    Ptr<FlowMonitor> monitor = flowmonitor.InstallAll ();

    uint16_t sp = 8080;
    Address sinkAddress (InetSocketAddress (d.GetRightIpv4Address (0), sp));
    PacketSinkHelper packetSinkHelper ("ns3::TcpSocketFactory", InetSocketAddress (Ipv4Address::GetAny(), sp));
    ApplicationContainer sinkApps = packetSinkHelper.Install (d.GetRight (0));
    sinkApps.Start (Seconds (0));
    sinkApps.Stop (Seconds (simul_time+2.0));

    Ptr<Socket> ns3TcpSocket = Socket::CreateSocket (d.GetLeft (0), TcpSocketFactory::GetTypeId ());
    Ptr<TutorialApp> app = CreateObject<TutorialApp> ();
    app->Setup (ns3TcpSocket, sinkAddress, payloadSize, DataRate (senderDataRate), simul_time);
    d.GetLeft (0)->AddApplication (app);
    app->SetStartTime (Seconds (1));
    app->SetStopTime (Seconds (simul_time));

    for(int i=0;i<1; i++){
        std::ostringstream oss;
        if(tcp_type==0){
            if(i==0){
              oss << directory_path << "tcpnr_hs" <<  ".cwnd";
            }
            else{
              oss << directory_path << "tcphs"  <<  ".cwnd";
            }
        }
        else if(tcp_type==1){
            if(i==0){
              oss << directory_path << "tcpnr_ar"  <<  ".cwnd";
            }
            else{
              oss << directory_path << "tcpar"  <<  ".cwnd";
            }
        }
        else if(tcp_type==2){
            if(i==0){
              oss << directory_path << "tcpnr_wwp" <<  ".cwnd";
            }
            else{
              oss << directory_path << "tcpwwp" <<  ".cwnd";
            }
        }

        AsciiTraceHelper asciiTraceHelper;
        Ptr<OutputStreamWrapper> stream = asciiTraceHelper.CreateFileStream (oss.str());
        ns3TcpSocket->TraceConnectWithoutContext ("CongestionWindow", MakeBoundCallback (&CwndChange, stream));
    }

    Address sinkAddress (InetSocketAddress (d.GetRightIpv4Address (1), sp));
    PacketSinkHelper packetSinkHelper ("ns3::TcpSocketFactory", InetSocketAddress (Ipv4Address::GetAny(), sp));
    ApplicationContainer sinkApps = packetSinkHelper.Install (d.GetRight (1));
    sinkApps.Start (Seconds (0));
    sinkApps.Stop (Seconds (simul_time+2.0));

    Ptr<Socket> ns3TcpSocket = Socket::CreateSocket (d.GetLeft (1), TcpSocketFactory::GetTypeId ());
    Ptr<TutorialApp> app = CreateObject<TutorialApp> ();
    app->Setup (ns3TcpSocket, sinkAddress, payloadSize, DataRate (senderDataRate), simul_time);
    d.GetLeft (1)->AddApplication (app);
    app->SetStartTime (Seconds (1));
    app->SetStopTime (Seconds (simul_time));
        
        
    for(int i=0;i<1; i++){
        std::ostringstream oss;
        if(tcp_type==0){
            if(i==0){
              oss << directory_path << "tcpnr_hs" <<  ".cwnd";
            }
            else{
              oss << directory_path << "tcphs"  <<  ".cwnd";
            }
        }
        else if(tcp_type==1){
            if(i==0){
              oss << directory_path << "tcpnr_ar"  <<  ".cwnd";
            }
            else{
              oss << directory_path << "tcpar"  <<  ".cwnd";
            }
        }
        else if(tcp_type==2){
            if(i==0){
              oss << directory_path << "tcpnr_wwp" <<  ".cwnd";
            }
            else{
              oss << directory_path << "tcpwwp" <<  ".cwnd";
            }
        }

        AsciiTraceHelper asciiTraceHelper;
        Ptr<OutputStreamWrapper> stream = asciiTraceHelper.CreateFileStream (oss.str());
        ns3TcpSocket->TraceConnectWithoutContext ("CongestionWindow", MakeBoundCallback (&CwndChange, stream));
    }
  
    Simulator::Stop (Seconds (simul_time+2.0));
    Simulator::Run ();

    int j = 0;
    double CurThroughputArr[] = {0, 0};



    uint32_t SentPackets = 0;
    uint32_t ReceivedPackets = 0;
    uint32_t LostPackets = 0;

    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier> (flowmonitor.GetClassifier ());
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats ();

    int totalhopcount= 0; 
    for (auto iter = stats.begin (); iter != stats.end (); ++iter) {
      if(j%2 == 0) { CurThroughputArr[0] += iter->second.rxBytes; }
      if(j%2 == 1) { CurThroughputArr[1] += iter->second.rxBytes; }

      SentPackets = SentPackets +(iter->second.txPackets);
      ReceivedPackets = ReceivedPackets + (iter->second.rxPackets);
      totalhopcount+= (iter->second.timesForwarded);
      LostPackets = LostPackets + (iter->second.lostPackets);
      j = j + 1;
    }


    CurThroughputArr[0] /= ((simul_time + 2.0)*1000);
    CurThroughputArr[1] /= ((simul_time + 2.0)*1000);
    // AvgThroughput = CurThroughputArr[0] + CurThroughputArr[1];

    double throughput1 = CurThroughputArr[0];
    double throughput2 = CurThroughputArr[1];
    double averageHopCount = (double)totalhopcount / (double)ReceivedPackets;
    std::ofstream outputFile("scratch/hopcount.txt", std::ios::app);
    outputFile<<averageHopCount<<"\n";
    outputFile.close();
    

    if(plot==1 && tcp_type==0){
        std::ofstream outputFile("scratch/tcphs_btnk_rate.txt", std::ios::app);
        outputFile<<bottle_neck_rate<<"\t"<<throughput1<<"\t"<<throughput2<<"\n";
        outputFile.close();
    }
    else if(plot ==1 && tcp_type==1){
        std::ofstream outputFile("scratch/tcpar_btnk_rate.txt", std::ios::app);
        outputFile<<bottle_neck_rate<<"\t"<<throughput1<<"\t"<<throughput2<<"\n";
        outputFile.close();
    }
    else if(plot == 1 && tcp_type==2){
        std::ofstream outputFile("scratch/tcpwwp_btnk_rate.txt", std::ios::app);
        outputFile<<bottle_neck_rate<<"\t"<<throughput1<<"\t"<<throughput2<<"\n";
        outputFile.close();
    }
    else if(plot == 2 && tcp_type==0){
        std::ofstream outputFile("scratch/tcphs_packet_loss_exponent.txt", std::ios::app);
        outputFile<<packet_loss_exponent<<"\t"<<throughput1<<"\t"<<throughput2<<"\n";
        outputFile.close();
    }
    else if(plot == 2 && tcp_type==1){
        std::ofstream outputFile("scratch/tcpar_packet_loss_exponent.txt", std::ios::app);
        outputFile<<packet_loss_exponent<<"\t"<<throughput1<<"\t"<<throughput2<<"\n";
        outputFile.close();
    }
    else if(plot == 2 && tcp_type==2){
        std::ofstream outputFile("scratch/tcpwwp_packet_loss_exponent.txt", std::ios::app);
        outputFile<<packet_loss_exponent<<"\t"<<throughput1<<"\t"<<throughput2<<"\n";
        outputFile.close();
    }
    Simulator::Destroy ();
    return 0;
}