#include "waiting_scene.hpp"

namespace Game
{
    WaitingScene::WaitingScene(Game &game) : m_game(game), m_clickables(), m_drawables() {}

    auto WaitingScene::enter() -> void
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
    }

    auto WaitingScene::update() -> std::optional<std::unique_ptr<Scene>>
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

    auto WaitingScene::render() -> void
    {
        for(const auto& drawable : m_drawables)
        {
            drawable->draw();
        }
    }

    WaitingScene::~WaitingScene()
    {
        m_game.getEventDispatcher().unsubscribe<MousePressedEvent>(m_escapeHandle);
        m_game.getEventDispatcher().unsubscribe<MouseReleasedEvent>(m_pressHandle);
    }
} // Game