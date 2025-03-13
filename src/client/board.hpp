#ifndef CHESS_BOARD_HPP
#define CHESS_BOARD_HPP

#include <vector>
#include <array>
#include <memory>

#include "pawn.hpp"

namespace Game
{
    constexpr int BOARD_WIDTH = 8;
    constexpr int BOARD_HEIGHT = 8;

    class Board
    {
    public:
        Board();
        ~Board() = default;
    private:
        std::array<std::array<std::unique_ptr<Piece>,8>,8> board{};
    };
}

#endif //CHESS_BOARD_HPP
