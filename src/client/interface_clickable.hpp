#ifndef CHESS_INTERFACE_CLICKABLE_HPP
#define CHESS_INTERFACE_CLICKABLE_HPP

#include "event.hpp"

namespace Game
{
    class Clickable
    {
    public:
        virtual ~Clickable() = default;
        virtual bool isWithinBounds(int x, int y) = 0;
        virtual void press(const MousePressedEvent& event) = 0;
    };
}

#endif //CHESS_INTERFACE_CLICKABLE_HPP
