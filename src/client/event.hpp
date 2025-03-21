//
// Created by adam on 3/20/25.
//

#ifndef CHESS_EVENT_HPP
#define CHESS_EVENT_HPP

namespace Game
{
    enum class EventType
    {
        MouseButtonPressed,
        MouseButtonReleased,
        EscapeKeyPressed
    };

    struct Event
    {
        EventType type;
    };

    struct EscapeKeyPressedEvent : Event {};

    struct MouseButtonPressedEvent : Event
    {
        int x;
        int y;

        MouseButtonPressedEvent(EventType type, int px, int py) : Event(type)
        {
            x = px;
            y = py;
        }
    };

    struct MouseButtonReleasedEvent : Event
    {
        int x;
        int y;

        MouseButtonReleasedEvent(EventType type, int px, int py) : Event(type)
        {
            x = px;
            y = py;
        }
    };
}

#endif //CHESS_EVENT_HPP
