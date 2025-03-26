#ifndef GAME_HPP
#define GAME_HPP

#include <iostream>

#include <SDL3/SDL.h>

#include "board.hpp"
#include "event_dispatcher.hpp"
#include "start_scene.hpp"
#include "scene.hpp"

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
        EventDispatcher& getEventDispatcher();
        void finish();
    private:
        SDL_Window* m_window = nullptr;
        SDL_Renderer* m_renderer = nullptr;

        EventDispatcher m_eventDispatcher;
        std::unique_ptr<Scene> m_scene;

        bool m_done = false;

        void render();
    };

} // Game

#endif // GAME_HPP
