#include <csignal>
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

    std::cout << "Starting server..." << std::endl;

    const auto addrInfo = loadInfo();

    if(!addrInfo.has_value())
    {
        std::cerr << "getaddrinfo error: " << gai_strerror(addrInfo.error()) << std::endl;
        return 1;
    }

    const auto socketFD = createSocket(addrInfo.value());

    if(!socketFD.has_value())
    {
        switch (socketFD.error())
        {
            case CREATE:
                std::cerr << "CREATE: " << strerror(errno) << std::endl;
                return 1;
            case CONNECT:
                std::cerr << "CONNECT: " << strerror(errno) << std::endl;
                return 1;
        }
    }

    if (send(socketFD.value(), "Hello, world!", 13, 0) == -1)
    {
        std::cerr << "send error" << std::endl;
    }
    else
    {
        std::cout << "sent message" << std::endl;
    }

    close(socketFD.value());

    auto game = Game::Game();

    game.run();

    return 0;
}

void *get_in_addr(struct sockaddr *sa)
{
    return sa->sa_family == AF_INET
           ? (void *) &(((struct sockaddr_in*)sa)->sin_addr)
           : (void *) &(((struct sockaddr_in6*)sa)->sin6_addr);
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

auto loadInfo() -> std::expected<std::unique_ptr<addrinfo,AddrInfoDeleter>,loadInfoError>
{
    addrinfo hints{};
    addrinfo *addrInfo = nullptr;

    std::memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    int status = getaddrinfo(ADDRESS, PORT, &hints, &addrInfo);

    if(status != 0)
    {
        return std::unexpected(static_cast<loadInfoError>(status));
    }

    return std::unique_ptr<addrinfo,AddrInfoDeleter>(addrInfo);
}

auto createSocket(const std::unique_ptr<addrinfo,AddrInfoDeleter>& info) -> std::expected<int,socketError>
{
    addrinfo* curr = info.get();
    int socketFD;

    std::optional<socketError> last = std::nullopt;

    while(curr != nullptr)
    {
        socketFD = socket(curr->ai_family, curr->ai_socktype, curr->ai_protocol);

        if(socketFD == -1)
        {
            curr = curr->ai_next;
            last = socketError::CREATE;
            continue;
        }

        int status = connect(socketFD, curr->ai_addr, curr->ai_addrlen);

        if(status == -1)
        {
            close(socketFD);
            curr = curr->ai_next;
            last = socketError::CONNECT;
            continue;
        }

        break;
    }

    if(curr == nullptr)
    {
        return last.has_value() ? std::unexpected(last.value()) : std::unexpected(socketError::CREATE);
    }

    return socketFD;
}