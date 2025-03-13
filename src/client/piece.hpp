#ifndef CHESS_PIECE_HPP
#define CHESS_PIECE_HPP

namespace Game
{
    enum Color : bool
    {
        WHITE = false,
        BLACK = true
    };

    class Piece
    {
    public:
        Piece(Color color, int x, int y);
        virtual ~Piece() = default;
        virtual void getLegalMoves() = 0;
    private:
        Color m_color;
        int m_x;
        int m_y;
    };

}

#endif //CHESS_PIECE_HPP
