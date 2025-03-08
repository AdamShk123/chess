#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>


#include <cstring>

#include <iostream>

const char* PORT = "3490";
constexpr int BACKLOG = 10;

void printAddresses(addrinfo* info);

