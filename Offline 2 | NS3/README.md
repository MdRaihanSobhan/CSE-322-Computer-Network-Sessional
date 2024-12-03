# Building Topologies and Measuring Different Metrics in NS3

## Assignment Specifications 
- Have a look at the [specifications](/Offline%202%20%7C%20NS3/specs%20offline%202.pdf)

## How to run the code

Follow the steps below to run the code:
- ## Install NS-3
    - Install ns3-dev following the link https://www.nsnam.org/docs/release/3.39/installation/html/quick-start.html
    - Download the files from the repository
- ## Place the solution files in the ns3-dev/scratch directory
    - Place the folders 1905095 and 1905095_st in the Scratch folder of ns3-dev (ns3-dev/scratch) and run the sh files
        - 1905095 is the mobile version 
        - 1905095_st is the stationary version
- ## Run the code
    - Run the sh files in the respective folders. It will do all the necessary tasks for this assignment for you. 
    - For the mobile version, run the following commands:
        ```sh
            cd ns3-dev/scratch/1905095
            bash 1905095_run.sh
        ```
    - For the stationary version, run the following commands:
        ```sh
            cd ns3-dev/scratch/1905095_st
            bash 1905095_run.sh
        ```

## Learn NS-3 Basics

- You can follow the [YouTube playlist](https://youtube.com/playlist?list=PLN2AD3KJEDbxRixrS56f4qE2o6ENjRprQ&si=A3CP6fDDb4hBYzzp) to learn the basics of NS-3.

- You can read the [NS-3 Basics PDF](Offline%202%20NS3/NS3%20Basics/NS3%20Basics.pdf) to learn the basics of NS-3.

- You can also practice these [codes](Offline%202%20NS3/NS3%20Basics/first_sixth_comment/).



## CAUTION!!!
    I used Flow Monitor in this offline as I didn't notice that it wasn't allowed to use in offline 2, btw, it was allowed in offline 3. 
    The tasks should have been done by using trace sources instead of using Flow Monitor
