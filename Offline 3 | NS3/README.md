## Implementation of a Congestion Control Algorithm - TCP Adaptive Reno in NS-3

# How to run the code
- Install ns3-dev following the guidelines from the link https://www.nsnam.org/docs/release/3.39/installation/html/quick-start.html 
- Download the files from the repository
- Place the [1905095.cc](/Offline%203%20|%20NS3/1905095.cc), [1905095.sh](/Offline%203%20|%20NS3/1905095.sh) and [1905095.plt](/Offline%203%20|%20NS3/1905095.plt) files in the "scratch" folder of "ns3-dev". (Directory: ns3-dev/scratch)
- Place the [1905095_tcp-adaptive-reno.cc](/Offline%203%20|%20NS3/1905095_tcp-adaptive-reno.cc) and [1905095_tcp-adaptive-reno.h](/Offline%203%20|%20NS3/1905095_tcp-adaptive-reno.h) files in the src/internet/model folder of ns3-dev (Directory: ns3-dev/src/internet/model)
- Include 1905095_tcp-adaptive-reno.cc and 1905095_tcp-adaptive-reno.h files in src\internet\CMakeLists.txt like the following:

```txt
    set(source_files
    ......................
    model/1905095_tcp-adaptive-reno.cc
    ....................... 
    set(header_files
    .......................
    model/1905095_tcp-adaptive-reno.h
    .......................
- Run the 1905095.sh file . It will do all the necessary tasks for this assignment for you. 
```sh
    bash 1905095.sh
