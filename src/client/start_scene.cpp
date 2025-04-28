#include "start_scene.hpp"

namespace Game
{
    StartScene::StartScene(Game& game) : m_game(game), m_clickables(), m_drawables(), m_textures() {}

    auto StartScene::enter() -> void
    {
        auto escape = [&game = this->m_game](const auto& event)
        {
            std::cout << typeid(event).name() << std::endl;
            game.finish();
        };

        m_escapeHandle = m_game.getEventDispatcher().subscribe<EscapePressedEvent>(escape);

        auto press = [&mousePress = this->m_mousePress](const auto& event)
        {
            mousePress = event;
        };

        m_pressHandle = m_game.getEventDispatcher().subscribe<MousePressedEvent>(press);

        const std::string path = "../../assets/chess.png";
        m_textures[path] = m_game.loadTexture(path);

        auto playButton = std::make_shared<PlayButton>(0,0,m_textures[path],m_game.getRenderer());
        m_drawables.push_back(playButton);
        m_clickables.push_back(playButton);
    }

    auto StartScene::update() -> std::optional<std::unique_ptr<Scene>>
    {
        if(m_mousePress.has_value())
        {
            for(const auto& clickable : m_clickables)
            {
                if(clickable->isWithinBounds(m_mousePress->x, m_mousePress->y)) clickable->press(m_mousePress.value());
            }
        }

        if(m_next.has_value()) return std::move(m_next.value());

        return std::nullopt;
    }

    auto StartScene::render() -> void
    {
        auto renderer = m_game.getRenderer();
        SDL_SetRenderDrawColor(renderer, 0xFF, 0xFF, 0xFF, 0xFF);
        SDL_RenderClear(renderer);

        for(const auto& drawable : m_drawables)
        {
            drawable->draw();
        }

        SDL_SetRenderDrawColor(renderer, 0xFF, 0x0, 0x0, 0xFF);
        SDL_FRect rect{100,100,100,100};
        SDL_RenderFillRect(renderer, &rect);

        SDL_RenderPresent(renderer);
    }

    StartScene::~StartScene()
    {
        auto& ref = m_game.getEventDispatcher();
        ref.unsubscribe<MousePressedEvent>(m_escapeHandle);
        ref.unsubscribe<MouseReleasedEvent>(m_pressHandle);
    }
} // Game