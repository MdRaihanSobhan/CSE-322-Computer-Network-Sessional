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

#include "ns3/object.h"
#include "ns3/simulator.h"
#include "ns3/trace-source-accessor.h"
#include "ns3/traced-value.h"
#include "ns3/uinteger.h"

#include <iostream>

using namespace ns3;

/**
 * Tutorial 4 - a simple Object to show how to hook a trace.
 */
class MyObject : public Object
{
  public:
    /**
     * Register this type.
     * \return The TypeId.
     */
    static TypeId GetTypeId()
    {
        // .AddTraceSource provides the “hooks” used for connecting the 
        // trace source to the outside world through the Config system.
        // arguments : 1. name - visible to the config system
        //             2. help string
        //             3. the traced value - a data member of the class
        //             4.  name of the typedef - used to generate documentation for the correct callback signature
        static TypeId tid = TypeId("MyObject")
                                .SetParent<Object>()
                                .SetGroupName("Tutorial")
                                .AddConstructor<MyObject>()
                                .AddTraceSource("MyInteger",
                                                "An integer value to trace.",
                                                MakeTraceSourceAccessor(&MyObject::m_myInt),
                                                "ns3::TracedValueCallback::Int32");
        return tid;
    }

    MyObject()
    {
    }
    // TracedValue<> declaration provides the infrastructure that drives 
    // the callback process. Any time the underlying value is changed 
    // the TracedValue mechanism will provide both the 
    // old and the new value of that variable, in this case an int32_t value

    // TracedValue<> overloads the assignment operator to call the callbacks
    TracedValue<int32_t> m_myInt; //!< The traced variable.
};

void
IntTrace(int32_t oldValue, int32_t newValue)
{
    std::cout << "Traced " << oldValue << " to " << newValue << std::endl;
}

void
NewValTrace(int32_t oldValue, int32_t newValue)
{
    std::cout << "  NEW VALUE : " << newValue << std::endl;
}


void
IntTrace2(std::string context, int32_t oldValue, int32_t newValue)
{
    std::cout << context << " : Traced " << oldValue << " to " << newValue << std::endl;
}


int
main(int argc, char* argv[])
{
    Ptr<MyObject> myObject = CreateObject<MyObject>();
    myObject->TraceConnectWithoutContext("MyInteger", MakeCallback(&IntTrace));
    // myObject->TraceConnectWithoutContext("MyInteger", MakeCallback(&NewValTrace));
    // myObject->TraceConnect("MyInteger", "Context", MakeCallback(&IntTrace2));

    // 
    myObject->m_myInt = 1234;


    return 0;
}
