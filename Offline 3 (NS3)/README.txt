- Install ns3-dev following the link https://www.nsnam.org/docs/release/3.39/installation/html/quick-start.html
- Download the files from the repository
- Place the 1905095.cc, 1905095.sh and 1905095.plt files in the scratch folder of ns3-dev
- Place the 1905095_tcp-adaptive-reno.cc and 1905095_tcp-adaptive-reno.h files in the src/internet/model folder of ns3-dev
- Include 1905095_tcp-adaptive-reno.cc and 1905095_tcp-adaptive-reno.h files in src\internet\CMakeLists.txt like the following:
    set(source_files
    ......................
    model/1905095_tcp-adaptive-reno.cc
   ....................... 
    set(header_files
    .......................
    model/1905095_tcp-adaptive-reno.h
   .......................
- Run the 1905095.sh file 
