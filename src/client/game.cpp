#include "game.hpp"

namespace Game
{
    Game::Game() : m_eventDispatcher(), m_scene(std::make_unique<StartScene>(*this))
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

    auto Game::run() -> void
    {
        auto board = Board();

        auto sceneChange = [&scene = m_scene](const SceneChangedEvent& event)
        {
            scene = std::unique_ptr<Scene>(event.scene.get());
            scene->enter();
        };

        m_eventDispatcher.subscribe<SceneChangedEvent>(sceneChange);

        SceneChangedEvent new_scene{};
        new_scene.scene = std::make_unique<WaitingScene>(*this);
        m_eventDispatcher.dispatch<SceneChangedEvent>(&new_scene);

        SDL_Event event;

//        uint64_t start = 0;
//        uint64_t last = 0;

        m_scene->enter();

        while(!m_done)
        {
//            start = SDL_GetTicks();

            while(SDL_PollEvent(&event))
            {
                if(event.type == SDL_EVENT_QUIT)
                {
                    finish();
                }
                else if(event.type == SDL_EVENT_KEY_DOWN)
                {
                    if(event.key.key == SDLK_ESCAPE)
                    {
                        EscapePressedEvent temp{};
                        m_eventDispatcher.dispatch<EscapePressedEvent>(&temp);
                    }
                }
                else if(event.type == SDL_EVENT_MOUSE_BUTTON_DOWN)
                {
                    if(event.button.button == SDL_BUTTON_LEFT)
                    {
                        MousePressedEvent temp{static_cast<int>(event.button.x), static_cast<int>(event.button.y)};
                        m_eventDispatcher.dispatch<MousePressedEvent>(&temp);
                    }
                }
                else if(event.type == SDL_EVENT_MOUSE_BUTTON_UP)
                {
                    if(event.button.button == SDL_BUTTON_LEFT)
                    {
                        MouseReleasedEvent temp{static_cast<int>(event.button.x), static_cast<int>(event.button.y)};
                        m_eventDispatcher.dispatch<MouseReleasedEvent>(&temp);
                    }
                }

            }

            auto next = m_scene->update();

            m_scene->render();

            if(next.has_value())
            {
                m_scene = std::move(next.value());
                m_scene->enter();
            }

//            double fps = 1000.0 / static_cast<double>(last - start);
//            std::cout << "FPS: " << fps << std::endl;
        }
    }

    auto Game::getEventDispatcher() -> EventDispatcher&
    {
        return m_eventDispatcher;
    }

    auto Game::finish() -> void
    {
        m_done = true;
    }

    auto Game::loadTexture(const std::string &path) -> Texture
    {
        return Texture(IMG_LoadTexture(m_renderer,path.c_str()));
    }

    auto Game::getRenderer() -> SDL_Renderer*
    {
        return m_renderer;
    }
}
