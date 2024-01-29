#ifndef TCP_ADAPTIVERENO_H
#define TCP_ADAPTIVERENO_H

#include "tcp-congestion-ops.h"
#include "tcp-westwood-plus.h"

#include "ns3/event-id.h"
#include "ns3/sequence-number.h"
#include "ns3/tcp-recovery-ops.h"
#include "ns3/traced-value.h"

namespace ns3
{

class Packet;
class TcpHeader;
class Time;
class EventId;


class TcpAdaptiveReno : public TcpWestwoodPlus
{
  public:

    virtual uint32_t GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight);

    virtual void PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt);

    virtual Ptr<TcpCongestionOps> Fork();

    static TypeId GetTypeId(void);

    TcpAdaptiveReno(void);

    TcpAdaptiveReno(const TcpAdaptiveReno& sock);
    virtual ~TcpAdaptiveReno(void);

    enum FilterType
    {
        NONE,
        TUSTIN
    };

  private:

    void EstimateBW(const Time& rtt, Ptr<TcpSocketState> tcb);

    double EstimateCongestionLevel();

    void EstimateIncWnd(Ptr<TcpSocketState> tcb);

  protected:
    virtual void CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked);



    int32_t m_incWnd;   
    uint32_t m_baseWnd; 
    int32_t m_probeWnd; 
    Time m_minRtt;      
    Time m_currentRtt;  
    Time m_jPacketLRtt; 
    Time m_conjRtt;     
    Time m_prevConjRtt; 
};

} // namespace ns3

#endif /* TCP_ADAPTIVE_RENO_H */
