#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>

#include <cstring>
#include <cerrno>

#include <iostream>
#include <vector>

const char* PORT = "3490";
constexpr int BACKLOG = 10;

void *get_in_addr(sockaddr *sa);