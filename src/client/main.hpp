#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#include "game.hpp"

#include <cstring>
#include <cerrno>
#include <expected>
#include <iostream>

#include <regex>

const char* ADDRESS = "localhost";
const char* PORT = "3490";
constexpr int BACKLOG = 10;
constexpr int MAX_DATA_SIZE = 100;

void *get_in_addr(struct sockaddr *sa);

bool validateAddress(const std::string& address);
bool validatePort(const std::string& port);

struct AddrInfoDeleter
{
    void operator()(addrinfo* info)
    {
        freeaddrinfo(info);
    }
};

enum loadInfoError : int
{
    LOAD_EAI_ADDRFAMILY = EAI_ADDRFAMILY,
    LOAD_EAI_AGAIN = EAI_AGAIN,
    LOAD_EAI_BADFLAGS = EAI_BADFLAGS,
    LOAD_EAI_FAIL = EAI_FAIL,
    LOAD_EAI_FAMILY = EAI_FAMILY,
    LOAD_EAI_MEMORY = EAI_MEMORY,
    LOAD_EAI_NODATA = EAI_NODATA,
    LOAD_EAI_NONAME = EAI_NONAME,
    LOAD_EAI_SERVICE = EAI_SERVICE,
    LOAD_EAI_SOCKTYPE = EAI_SOCKTYPE,
    LOAD_EAI_SYSTEM = EAI_SYSTEM
};

/**
 * Creates a linked list of potential IP addresses to use
 *
 * @return pointer to the beginning of the list
 * or an error code if the function failed
 */
auto loadInfo() -> std::expected<std::unique_ptr<addrinfo,AddrInfoDeleter>,loadInfoError>;

enum socketError : int
{
    CREATE = 0x0,
    CONNECT = 0x1,
};
/**
 * Creates a file descriptor for the listening socket,
 * binds it to a specific port, and enables listening
 *
 * @param addrinfo* linked list of ip addresses
 * @return file descriptor of the created socket
 * or an error code if the function failed
 */
auto createSocket(const std::unique_ptr<addrinfo,AddrInfoDeleter>& info) -> std::expected<int,socketError>;