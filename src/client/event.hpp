//
// Created by adam on 3/23/25.
//

#ifndef CHESS_EVENT_HPP
#define CHESS_EVENT_HPP

#include "scene.hpp"

namespace Game
{
    struct MousePressedEvent
    {
        int x;
        int y;
    };

    struct MouseReleasedEvent
    {
        int x;
        int y;
    };

    struct EscapePressedEvent
    {

    };

    struct SceneChangedEvent
    {
        std::unique_ptr<Scene> scene;
    };
}

#endif //CHESS_EVENT_HPP
