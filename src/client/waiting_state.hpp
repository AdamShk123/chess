#ifndef CHESS_WAITING_STATE_HPP
#define CHESS_WAITING_STATE_HPP

#include "scene.hpp"
#include "event.hpp"
#include "game.hpp"
#include "interface_drawable.hpp"
#include "interface_clickable.hpp"

#include <exception>

namespace Game
{
    class Game;

    class WaitingState : public Scene
    {
    public:
        explicit WaitingState(Game& game);
        ~WaitingState() override;

        auto enter() -> void override;
        auto update() -> std::optional<std::unique_ptr<Scene>> override;
        auto render() -> void override;
    private:
        Game& m_game;

        EventDispatcher::Handle m_escapeHandle;
        EventDispatcher::Handle m_pressHandle;

        std::optional<MousePressedEvent> m_mousePress;

        std::optional<std::unique_ptr<Scene>> m_next;

        std::vector<std::unique_ptr<Clickable>> m_clickables;
        std::vector<std::unique_ptr<Drawable>> m_drawables;
    };

} // Game

#endif //CHESS_WAITING_STATE_HPP
