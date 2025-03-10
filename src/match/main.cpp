#include "main.hpp"

int main(int argc, char** argv)
{
    int pid = getpid();

    if(argc != 5)
    {
        std::cerr << "[match - " << pid << "] incorrect number of arguments (needs to be exactly 5)" << std::endl;
        return 1;
    }

    int fd = std::stoi(argv[1]);


    std::cout << "[match - " << pid << "] starting match..." << std::endl;

    std::cout << "[match - " << pid << "] ending match..." << std::endl;

    return 0;
}