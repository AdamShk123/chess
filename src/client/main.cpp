#include "main.hpp"

int main(int argc, char** argv)
{
    if(argc != 3)
    {
        std::cerr << "Incorrect number of command-line arguments. Should be exactly 2." << std::endl;
        return 1;
    }

    std::string address = argv[1];
    std::string port = argv[2];

    if(!validateAddress(address))
    {
        std::cerr << "Invalid Address!" << std::endl;
        return 1;
    }

    if(!validatePort(port))
    {
        std::cerr << "Invalid Port!" << std::endl;
        return 1;
    }

    int status;
    addrinfo hints{};
    addrinfo *serverInfo = nullptr;

    std::memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    status = getaddrinfo(address.c_str(), port.c_str(), &hints, &serverInfo);

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

    status = connect(socketFD, serverInfo->ai_addr, serverInfo->ai_addrlen);

    if(status == -1)
    {
        std::cerr << "connect error: " << errno << std::endl;
        return 1;
    }

    freeaddrinfo(serverInfo);

    //    auto game = Game::Game();

    //    game.run();

    return 0;
}

bool validateAddress(const std::string& address)
{
    if(address == "localhost") return true;

    const std::regex pattern("^[a-zA-Z0-9-]{1,63}\\.[a-zA-Z]{2,6}$|^localhost$");

    return std::regex_match(address, pattern);
}

bool validatePort(const std::string& port)
{
    try
    {
        int converted = std::stoi(port);
        if(converted < 1024) return false;
    }
    catch (std::invalid_argument const& ex)
    {
        std::cout << "std::invalid_argument::what(): " << ex.what() << '\n';
        return false;
    }
    catch (std::out_of_range const& ex)
    {
        std::cout << "std::out_of_range::what(): " << ex.what() << '\n';
        return false;
    }

    for(char ch : port)
    {
        if(ch < 0x30 || ch > 0x39)
        {
            return false;
        }
    }

    return true;
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