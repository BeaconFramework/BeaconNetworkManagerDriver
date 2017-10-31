# BeaconNetworkManagerDriver

The BEACON-Network-Manager-Driver [BNMD], is a component designed to provide functionality to BNM as interface versus Openstacks cloud, via JCLOUDS Adapter, and for the reconstruction of the Network tables that will be sent to target BNA.
All the interaction between BNMD and BNM, and between BNMD and BNAs are driven via http modeled as REST WS or REST WS invocation. 
At the same time the BNMD have to be connected to the same MongoDB database used by BB.

This component base its interaction with Openstack cloud via V2.0 API.

This component interacts with other modules of the Beacon Architecture (BNM and BNA) via REST WS and, at the same time, expose WS for BNM.

To instantiate it, the deployer have to create a directory in the path: "/home/beacon/beaconConf/" with the configuration file listed below according with the template provided in the template folder:

-> configuration_bigDataPlugin.xml


Web serices exposed by BNMD are:

i) http://[BNMD_BASE_PATH]//fednet/eastBr/FA_Management

ii) http://[BNMD_BASE_PATH]//fednet/eastBr/network

iii) http://[BNMD_BASE_PATH]//fednet/eastBr/user
