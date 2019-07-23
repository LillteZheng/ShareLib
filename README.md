# ShareLib

## 1、config port
```
NetConfig.udpPort(PORT)
        .broadcastPort(BROADCAST_PORT)
        .tcpPort(TCP_PORT)
        .broadcastIp(BROADCAST_IP)
        .build();
```
## 2、how to use
```
//config server
TransServiceManager.useSocket()
        .createServer()
        .addServerListener(this);
UdpManager.startProvider();

//config client
 UdpManager.startSearcher()
                .addDeviceListener(this);
                
TransServiceManager.useSocket()
                   .createClient(device.ip)
                   .addClientListener(this);
```