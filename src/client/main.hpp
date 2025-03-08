#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#include "game.hpp"

#include <cstring>
#include <cerrno>

#include <iostream>

#include <regex>

const char* PORT = "3490";
constexpr int BACKLOG = 10;

void printAddresses(addrinfo* info);

bool validateAddress(const std::string& address);
bool validatePort(const std::string& port);