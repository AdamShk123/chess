#ifndef GAME_HPP
#define GAME_HPP

#include <iostream>

#include <SDL3/SDL.h>

#include "board.hpp"
#include "input.hpp"
#include "event_queue.hpp"
#include "event_dispatcher.hpp"

namespace Game
{
    constexpr std::string_view WINDOW_TITLE = "Chess";
    constexpr unsigned int WINDOW_WIDTH = 800;
    constexpr unsigned int WINDOW_HEIGHT = 600;

    class Game
    {
    public:
        Game();
        ~Game();

        void run();
    private:
        SDL_Window* m_window = nullptr;
        SDL_Renderer* m_renderer = nullptr;

        void render();
    };

} // Game

#endif // GAME_HPP
