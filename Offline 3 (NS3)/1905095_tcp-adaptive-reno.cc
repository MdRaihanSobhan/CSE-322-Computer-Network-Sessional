#include "1905095_tcp-adaptive-reno.h"

#include "rtt-estimator.h"
#include "tcp-socket-base.h"
#include "ns3/log.h"
#include "ns3/simulator.h"


namespace ns3
{

NS_OBJECT_ENSURE_REGISTERED(TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId(void)
{
    static TypeId tid =
        TypeId("ns3::TcpAdaptiveReno")
            .SetParent<TcpNewReno>()
            .SetGroupName("Internet")
            .AddConstructor<TcpAdaptiveReno>()
            .AddAttribute(
                "FilterType",
                "The type of filter to use",
                EnumValue(TcpAdaptiveReno::TUSTIN),
                MakeEnumAccessor(&TcpAdaptiveReno::m_fType),
                MakeEnumChecker(TcpAdaptiveReno::NONE, "None", TcpAdaptiveReno::TUSTIN, "Tustin"))
            .AddTraceSource("EstimatedBW",
                            "The estimated bandwidth",
                            MakeTraceSourceAccessor(&TcpAdaptiveReno::m_currentBW),
                            "ns3::TracedValueCallback::Double");
    return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno(void)
    : TcpWestwoodPlus(),
      m_minRtt(Time(0)),
      m_currentRtt(Time(0)),
      m_jPacketLRtt(Time(0)),
      m_conjRtt(Time(0)),
      m_prevConjRtt(Time(0)),
      m_incWnd(0),
      m_baseWnd(0),
      m_probeWnd(0)
{
}

TcpAdaptiveReno::TcpAdaptiveReno(const TcpAdaptiveReno& sock)
    : TcpWestwoodPlus(sock),
      m_minRtt(Time(0)),
      m_currentRtt(Time(0)),
      m_jPacketLRtt(Time(0)),
      m_conjRtt(Time(0)),
      m_prevConjRtt(Time(0)),
      m_incWnd(0),
      m_baseWnd(0),
      m_probeWnd(0)
{
}

TcpAdaptiveReno::~TcpAdaptiveReno(void)
{
}


void
TcpAdaptiveReno::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{

    if (rtt.IsZero())
    {
        return;
    }


    if (m_minRtt.IsZero())
    {
        m_minRtt = rtt;
    }
    else if (rtt <= m_minRtt)
    {
        m_minRtt = rtt;
    }

    m_currentRtt = rtt;
    m_ackedSegments += packetsAcked;


    TcpWestwoodPlus::EstimateBW(rtt, tcb);
}


double
TcpAdaptiveReno::EstimateCongestionLevel()
{
    float alpha = 0.85; 
    if (m_prevConjRtt < m_minRtt)
    {
        alpha = 0; 
    }

    double a = alpha * m_prevConjRtt.GetSeconds(); 
    double b = (1 - alpha) * m_minRtt.GetSeconds();
    double conjRtt = a + b; 
    m_conjRtt = Seconds(conjRtt);       
    double c = (m_currentRtt.GetSeconds() - m_minRtt.GetSeconds()); 
    double d = (conjRtt - m_minRtt.GetSeconds());
    double conjRttDiff =  c / d;                      
    double returnLevel = std::min(conjRttDiff,1.0);
    return returnLevel;
}

void
TcpAdaptiveReno::EstimateIncWnd(Ptr<TcpSocketState> tcb)
{

    double a = static_cast<double>(m_currentBW.Get().GetBitRate() / 1000); 
    double b = static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize);
    double m_maxIncWnd = a*b;

    double alpha = 10;
    double c = 2 * m_maxIncWnd;
    double d = (1 / alpha); 
    double e =  ((1 / alpha + 1) / (std::exp(alpha)));

    double beta = c * (d - e);

    double f = ((1 / alpha + 0.5) / (std::exp(alpha))); 
    double gamma = 1 - (c* (d - f));

    double congestion = EstimateCongestionLevel();

    double g = (m_maxIncWnd / std::exp(alpha * congestion)); 
    double h = (beta * congestion) + gamma; 
    m_incWnd = (int)( g + h);
}

void
TcpAdaptiveReno::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    if (segmentsAcked > 0)
    {
        EstimateIncWnd(tcb);
        double a = static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize);
        double takesum = a / tcb->m_cWnd.Get();
        takesum = std::max(1.0, takesum);
        double b = static_cast<uint32_t>(takesum);
        m_baseWnd = m_baseWnd + b;
        double c = (m_probeWnd + m_incWnd / (int)tcb->m_cWnd.Get()); 
        m_probeWnd = std::max((double)c, (double)0);

        tcb->m_cWnd = m_baseWnd + m_probeWnd;

    }
}

uint32_t
TcpAdaptiveReno::GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight)
{
    m_prevConjRtt = m_conjRtt; 
    m_jPacketLRtt = m_currentRtt; 

    double congestion = EstimateCongestionLevel();
    double a = 2 * tcb->m_segmentSize;  
    double b = (uint32_t)(tcb->m_cWnd / (1.0 + congestion)); 
    uint32_t ssthresh = std::max(a, b );

    m_baseWnd = ssthresh;
    m_probeWnd = 0;

    return ssthresh;
}



Ptr<TcpCongestionOps>
TcpAdaptiveReno::Fork()
{
    return CreateObject<TcpAdaptiveReno>(*this);
}

}
