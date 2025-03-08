#include "main.hpp"

int main(int argc, char** argv)
{
    for(int i = 0; i < argc; i++)
    {
        std::cout << argv[i] << std::endl;
    }

    int status;
    addrinfo hints{};
    addrinfo *serverInfo = nullptr;

    std::memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    status = getaddrinfo(nullptr, PORT, &hints, &serverInfo);

    if(status != 0)
    {
        std::cerr << "getaddrinfo error: " << gai_strerror(status) << std::endl;
        return 1;
    }

    printAddresses(serverInfo);

    int socketFD = socket(serverInfo->ai_family, serverInfo->ai_socktype, serverInfo->ai_protocol);

    if(socketFD == -1)
    {
        std::cerr << "socket error: " << errno << std::endl;
        return 1;
    }

    status = bind(socketFD, serverInfo->ai_addr, serverInfo->ai_addrlen);

    if(status == -1)
    {
        std::cerr << "bind error: " << errno << std::endl;
        return 1;
    }

    status = listen(socketFD, BACKLOG);

    if(status == -1)
    {
        std::cerr << "listen error: " << errno << std::endl;
        return 1;
    }

    freeaddrinfo(serverInfo);

    return 0;
}

void printAddresses(addrinfo* info)
{
    char ipstr[INET6_ADDRSTRLEN];
    addrinfo* curr = info;
    while(curr != nullptr)
    {
        void *addr;

        if(curr->ai_family == AF_INET)
        {
            auto *ip = (sockaddr_in*) curr->ai_addr;
            addr = &(ip->sin_addr);
        }
        else
        {
            auto *ip = (sockaddr_in6*) curr->ai_addr;
            addr = &(ip->sin6_addr);
        }

        // convert the IP to a string and print it:
        inet_ntop(curr->ai_family, addr, ipstr, sizeof(ipstr));
        std::cout << ipstr << std::endl;

        curr = curr->ai_next;
    }
}