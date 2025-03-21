#include "game.hpp"

namespace Game
{
    Game::Game()
    {
        const auto metadataResult = SDL_SetAppMetadata("Chess", "1.0.0", "com.example.chess");

        if (!metadataResult)
        {
            throw std::runtime_error(SDL_GetError());
        }

        const auto initResult = SDL_Init(SDL_INIT_VIDEO);

        if (!initResult)
        {
            throw std::runtime_error(SDL_GetError());
        }

        constexpr auto windowFlags = (SDL_WindowFlags)(SDL_WINDOW_RESIZABLE);

        m_window = SDL_CreateWindow(
                WINDOW_TITLE.data(),
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                windowFlags
        );

        if (m_window == nullptr)
        {
            throw std::runtime_error(SDL_GetError());
        }

        m_renderer = SDL_CreateRenderer(m_window,nullptr);

        if (m_renderer == nullptr)
        {
            throw std::runtime_error(SDL_GetError());
        }
    }

    Game::~Game()
    {
        SDL_DestroyRenderer(m_renderer);
        m_renderer = nullptr;

        SDL_DestroyWindow(m_window);
        m_window = nullptr;

        SDL_Quit();
    }

    void Game::render()
    {
        SDL_SetRenderDrawColor(m_renderer, 0xFF, 0xFF, 0xFF, 0xFF);
        SDL_RenderClear(m_renderer);

        SDL_SetRenderDrawColor(m_renderer, 0x90, 0x90, 0x90, 0xFF);
        SDL_FRect rect{0,0,64,64};
        SDL_RenderFillRect(m_renderer,&rect);

        SDL_RenderPresent(m_renderer);
    }

    void Game::run()
    {
        auto board = Board();
        auto input = Input();
        auto eventQueue = EventQueue();
        auto eventDispatcher = EventDispatcher();

        bool done = false;

        SDL_Event event;

//        uint64_t start = 0;
//        uint64_t last = 0;

        while(!done)
        {
//            start = SDL_GetTicks();

            while(SDL_PollEvent(&event))
            {
                if(event.type == SDL_EVENT_QUIT)
                {
                    done = true;
                }
                else if(event.type == SDL_EVENT_KEY_DOWN)
                {
                    if(event.key.key == SDLK_ESCAPE)
                    {
                        done = true;
                    }
                    switch (event.key.key)
                    {
                        case SDLK_ESCAPE:
                            eventQueue.push(std::make_unique<Event>(EventType::EscapeKeyPressed));
                            break;
                    }
                }
                else if(event.type == SDL_EVENT_MOUSE_BUTTON_DOWN)
                {
                    eventQueue.push(std::make_unique<MouseButtonPressedEvent>(EventType::MouseButtonPressed, 0, 0));
                }
                else if(event.type == SDL_EVENT_MOUSE_BUTTON_UP)
                {
                    eventQueue.push(std::make_unique<MouseButtonReleasedEvent>(EventType::MouseButtonReleased, 0, 0));
                }
            }

            while(!eventQueue.empty())
            {
                auto next = eventQueue.pop();
                eventDispatcher.dispatch(next);
            }

            render();

//            double fps = 1000.0 / static_cast<double>(last - start);
//            std::cout << "FPS: " << fps << std::endl;
        }
    }
}
