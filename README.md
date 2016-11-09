# CI 40 onboarding app
![](docs/img.png)
---
 'Onboarding-app' is a mobile Android application that helps user to 
 easily perform Ci40 onboarding and clickers provisioning. 
 
 It is a part of a bigger system consisting of CI 40 tooling (provisioning demon & scripts),
 constraint devices (running provisioning library), Device Server (DS)
 as illustrated in the following diagram:
  
 ![](docs/components.png) 
 
 What it really means is that using secure communication channel
 mobile client is able to prepare Ci40 and constraint devices to interact with the Device Server.
 
 To achieve that mobile client have to: 
 
  * provide Ci40 configuration necessary to download and store certificate from Device Server,    
  * introduce Ci40 with user-friendly client name (board will be visible on developer console with that name), 
  * provide configuration for 'awa client'
  
  and this is known as a "Ci40 onboardin process". 
  
  For constraint devices "provisioning process" requires mobile client to:
   
  * provide network configuration
  * and configuration to obtain PSK from the Device Server   
  

# Components responsibilities
 
 * provisioning demon:
    - application that runs on Ci40 board,
    - ensures secure connection between constraint devices,
    - exchange crypto keys and many more
    - can be found [here] (https://gitlab.flowcloud.systems/creator/ci40-provisioning-daemon)
 
 * scripts:
    - provides entry point for communication provisioning demon and LUCI web scripts
    - exposes utility API available via JSON-RPC and uBus
    - can be found [here] (https://gitlab.flowcloud.systems/creator/ci40-onboarding-scripts)
 
 * provisioning library:
    - delivers PSK and other configurations data to provision constraint device 
    - can be found [here](https://gitlab.flowcloud.systems/creator/contiki-provisioning-library) 
 
 * device server:
    - LWM2M management server,
    - exposes secured REST API/HTTPs used by the mobile app  
 
# Communication with Device Server via REST API
 
 Communication with Device Server requires user to be authenticated.
 To acquire access keys, that will be used to obtain DS access token, 
 the user has to login first with their CreatorID (points (1)-(4) on the following diagram).
 Having 'token_id' from the Identity Provider (IP) client now may obtain access keys (key and secret)
 performing simple HTTP request (points (5) and (6)) to the 'Developer Account Service.

![](docs/authorization.png) 

 (1) launch system browser for signing into identity provider
 
 (2) login/sign-up using system browser
 
 (3) Identity Provider (IDP) issues an authorization code to the redirect URI
 
 (4) application receives authorization intent with token_id
 
 (5) - (6) requesting access keys
  
 Having access keys we can easily login to Device Server and request for 
 a list of connected clients.
    
# Onboarding

## Dependencies
  
 To run properly following list of dependencies must installed on CI40:
    
 ```curl luci-mod-rpc libubox-lua avahi luci-ssl uhttpd-mod-tls```
 
## Prerequisites
 - Developer has account on creator portal
 - Ci40 has configured network connection
 - Ci40 is running mDNS, 
 - mDNS responds to search _onboarding._tcp service type

## Resolving Ci40 IP address
 
 Mobile application is designed to work in local area network. To obtain 
 Ci40 IP address mobile client uses Multicast DNS service discovery.
 Following diagram shows this procedure: 
 
 ![](docs/resolve_ip.png) 
 
## Ci40 configuration
  
 Having board IP address resolved mobile client can send proper configuration 
 to the Ci40. This can be achieved via LuCI through JSON-RPC API. 
 Following sequence diagram shows this procedure. 
 
 ![](docs/onboarding.png)
  
 If 'onboarding' operation succeeds new device should be visible on the 
 developer console and on clients list in the application.
 
## RPC API
  Authorization and onbarding are two remote procedures that can be executed.
  In addition to these there are other methods that can:
  - remove configuration
  - get basic info about luci configuration
  - check if Ci40 is configured
  
# Constraint devices provisioning
  
  To be done...
  
# License
 Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies.
 All rights reserved.
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 following disclaimer in the documentation and/or other materials provided with the distribution.
 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
