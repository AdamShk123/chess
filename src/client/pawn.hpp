#include "piece.hpp"

#ifndef CHESS_PAWN_HPP
#define CHESS_PAWN_HPP
namespace Game
{
    class Pawn : public Piece
    {
    public:
        Pawn(Color color, int x, int y);
        ~Pawn() = default;
        void getLegalMoves() override;
    private:
    };
}
#endif //CHESS_PAWN_HPP
