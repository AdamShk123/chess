#include "board.hpp"

namespace Game
{
    Board::Board()
    {
        for(int i = 0; i < BOARD_WIDTH; i++)
        {
            board[1][i] = std::make_unique<Pawn>(Color::BLACK, 1, i);
        }

        for(int i = 0; i < BOARD_WIDTH; i++)
        {
            board[7][i] = std::make_unique<Pawn>(Color::WHITE, 7, i);
        }
    }
}